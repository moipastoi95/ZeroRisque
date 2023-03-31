package org.tovivi.agent;

import org.tovivi.environment.*;
import org.tovivi.environment.action.*;
import org.tovivi.environment.action.exceptions.IllegalActionException;
import org.tovivi.environment.action.exceptions.SimulationRunningException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class Node {
    private Game game;

    private ArrayList<Card> deck;
    private int N; //Nombre de visite de ce noeud
    private int score;
    private String player;
    private boolean noMoreChild = false;
    private String phase;
    private HashMap<String, ArrayList<String>> frontier;
    private Node parent;
    private HashMap<Actuator, HashMap<Node, Double>> childs = new HashMap<Actuator, HashMap<Node, Double>>();

    /**Create a node / Not sure if the deck of card have any interest here*/
    public Node(Game game, int N, Node parent, ArrayList<Card> deck, Agent player, String phase) throws IOException, URISyntaxException {
        this.game = game;
        this.N = N;
        this.parent = parent;
        this.score = 0;
        this.deck = deck;
        this.player = player.getColor();
        this.setFront();
        this.setPhase(phase);
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game, Agent player) throws IOException, URISyntaxException {
        this.game = game;
        this.deck = player.getDeck();
        this.childs.clear();
        this.player = player.getColor();
        this.setFront();
        this.noMoreChild = false;
    }

    public void setNoMoreChild(){this.noMoreChild = true;}

    public boolean getNoMoreChild() {
        return this.noMoreChild;
    }

    /** Generate the childs tree for the node for an action made by the player
     * @param action The action from where to generate childs
     * */
    public void generateChilds(Actuator action) {
        Agent player = this.getPlayer();
        HashMap<Node, Double> childs = new HashMap<>();
        try{
            if(action instanceof MultiDeploy){

                Game next = new Game(this.game);
                for(Deployment dep: ((MultiDeploy) action).getDeploys()){
                    if(dep instanceof Deploy){
                        ((Deploy) dep).setTile(next.getTiles().get(dep.getTiles().get(0).getName()));
                    }
                    if(dep instanceof PlayCards){
                        ((PlayCards) dep).setPlayer(next.getPlayers().get(this.player));
                    }
                    dep.perform(next.getPlayers().get(this.player));
                }
                childs.put(new Node(next, 0,  this, next.getPlayers().get(this.player).getDeck(), player, "Attack"), 1.0);
                this.getChilds().put(action, childs);
            }

            else if(action instanceof Attack){
                Attack att = (Attack) action;
                double prob = player.getProba(att.getFromTile().getNumTroops()%50, att.getToTile().getNumTroops()%50);

                Game nextLoose = new Game(this.game);
                nextLoose.getTiles().get(att.getFromTile().getName()).setNumTroops(1);
                childs.put(new Node(nextLoose, 0, this, null, player, "Attack"), 1 - prob);

                Game nextWin = new Game(nextLoose);
                int troopMoved = (int) ((att.getFromTile().getNumTroops()-1)*prob);
                if(troopMoved == 0) troopMoved = 1;
                nextWin.getTiles().get(att.getFromTile().getName()).setNumTroops(att.getFromTile().getNumTroops()-troopMoved);
                nextWin.getTiles().get(att.getToTile().getName()).setOccupier(player,troopMoved);
                childs.put(new Node(nextWin, 0, this, null, player, "Attack"), prob);

                this.getChilds().put(action, childs);
            }

        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException | IOException | URISyntaxException | SimulationRunningException |
                 IllegalActionException e) {
            throw new RuntimeException(e);
        }
    }

    public int getN() {
        return N;
    }

    public void setN(int n) {
        this.N = n;
    }

    public void setPhase(String phase){this.phase = phase;}

    public String getPhase(){return phase;}

    public ArrayList<Card> getDeck(){return this.deck;}

    public int getScore(){return this.score;}

    public void setScore(int score){this.score = score;}

    public void resetScore(){this.score = 0;}

    public void addScore(int value){this.score += value;}

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Agent getPlayer() {
        return this.getGame().getPlayers().get(this.player);
    }

    public void setPlayer(Agent player) {
        this.player = player.getColor();
    }

    /**Return the HashMap of the childs of the node
     * @return The HashMap of the childs*/
    public HashMap<Actuator, HashMap<Node, Double>> getChilds(){return this.childs;}

    public boolean isChild(Actuator action){return this.childs.containsKey(action);}

    /**@return Node - One of the childs node of the node taking into account probabilities*/
    public Node getNextNode(Actuator act) {
        double p = Math.random();
        HashMap<Node, Double> set = this.getChilds().get(act);
        double tresh = 0;
        for(Node key: set.keySet()){
            tresh += set.get(key);
            if(p < tresh){
                return key;
            }
        }
        return null;
    }

    /**Return the front for the agent in the game of the node n, I should probably add player as an parameter
     * @return The frontier as an HashMap(Tile, List(Tile))*/
    public void setFront() throws IOException, URISyntaxException {
        // get every tile next to an opponent tile, and retrieve opponent's tile next to them
        Agent player = this.getGame().getPlayers().get(this.player);
        Game g = this.game;
        HashMap<String, ArrayList<String>> front = new HashMap<>();
        for (Tile t : g.getTiles().values()) {
            if(t.getOccupier().getColor() == player.getColor()) {
                boolean flag = false;
                ArrayList<String> opponentTiles = new ArrayList<>();
                if(t.getNumTroops() > 1) {
                    for (Tile neighbor : t.getNeighbors()) {
                        if (neighbor.getOccupier().getColor() != player.getColor()) {
                            flag = true;
                            // add the real ref of tiles
                            opponentTiles.add(g.getTiles().get(neighbor.getName()).getName());
                        }
                    }
                }
                if (flag) {
                    front.put(t.getName(), opponentTiles);
                }
            }
        }
        this.frontier  = front;
    }

    public ArrayList<Actuator> getActions(){
        if(this.phase == "Deploy"){
            return this.getDeployActions();
        }
        else if(this.phase == "Attack"){
            return this.getAttackActions();
        }
        else return null;
    }

    private ArrayList<Actuator> getDeployActions() {
        Agent player = this.getPlayer();
        ArrayList<Actuator> possible_actions= new ArrayList<>();

        int num_to_deploy = this.getPlayer().getNumDeploy();
        // if cards owned, use them
        ArrayList<Deployment> depL = new ArrayList<>();
        ArrayList<Card> goodCards = Card.chooseCards(this.getDeck(), player);
        int goodCardsValue = Card.count(goodCards, player);
        if (goodCardsValue > 0) {
            PlayCards pc = new PlayCards(goodCards, player);
            depL.add(pc);
            depL.addAll(pc.autoDeploy());
            num_to_deploy += Card.countOnlyCombo(goodCards, player);
        }

        for(String tileKey: this.frontier.keySet()){
            depL.add(new Deploy(num_to_deploy, this.getTile(tileKey)));
            //System.out.println(depL);
            possible_actions.add(new MultiDeploy(new ArrayList<>(depL)));
            depL.remove(depL.size() - 1);
        }

        return possible_actions;
    }

    private ArrayList<Actuator> getAttackActions() {
        Agent player = this.getPlayer();
        ArrayList<Actuator> actions = new ArrayList<>();

        for(String tileKey: frontier.keySet()){
            Tile tile = this.getTile(tileKey);
            for(String oppTileKey: frontier.get(tileKey)){
                if(tile.getNumTroops()-1 != 0){
                    actions.add(new Attack(tile, this.getTile(oppTileKey), tile.getNumTroops()-1, null, null));
                }
            }
        }
        return actions;
    }

    public Tile getTile(String key){return this.getGame().getTiles().get(key);}
}
