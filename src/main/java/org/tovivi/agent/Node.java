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

    final int number_deploy_max = 2;
    private Game game;
    private ArrayList<Card> deck;
    private int N; //Nombre de visite de ce noeud
    private int score;

    private Agent player;
    private Node parent;
    private HashMap<Actuator, HashMap<Node, Double>> childs = new HashMap<Actuator, HashMap<Node, Double>>();

    /**Create a node / Not sure if the deck of card have any interest here*/
    public Node(Game game, int N, Node parent, ArrayList<Card> deck, Agent player){
        this.game = game;
        this.N = N;
        this.parent = parent;
        this.score = 0;
        this.deck = deck;
        this.player = player;
    }

    /** Generate the childs tree for the node for an action made by the player
     * @param action The action from where to generate childs
     * @param player The player that his making the action
     * */
    public void generateChilds(Actuator action, Agent player) {
        HashMap<Node, Double> childs = new HashMap<>();
        try{
            if(action instanceof MultiDeploy){

                Game next = new Game(this.game);
                for(Deployment dep: ((MultiDeploy) action).getDeploys()){
                    if(dep instanceof Deploy){
                        ((Deploy) dep).setTile(next.getTiles().get(dep.getTiles().get(0).getName()));
                    }
                    if(dep instanceof PlayCards){
                        ((PlayCards) dep).setPlayer(next.getPlayers().get(player.getColor()));
                    }
                    dep.perform(next.getPlayers().get(player.getColor()));
                }
                childs.put(new Node(next, 0,  this, next.getPlayers().get(player.getColor()).getDeck(), player), 1.0);
                this.getChilds().put(action, childs);
            }

            else if(action instanceof Attack){
                Attack att = (Attack) action;
                double prob = player.getProba(att.getFromTile().getNumTroops()%50, att.getToTile().getNumTroops()%50);
                Game nextLoose = new Game(this.game);
                nextLoose.getTiles().get(att.getFromTile().getName()).setNumTroops(1);
                childs.put(new Node(nextLoose, 0, this, null, player), 1 - prob);
                Game nextWin = new Game(nextLoose);
                int troopMoved = (int) ((att.getFromTile().getNumTroops()-1)*prob);
                if(troopMoved == 0) troopMoved = 1;
                nextWin.getTiles().get(att.getFromTile().getName()).setNumTroops(att.getFromTile().getNumTroops()-troopMoved);
                nextWin.getTiles().get(att.getToTile().getName()).setOccupier(player,troopMoved);
                childs.put(new Node(nextWin, 0, this, null, player), prob);
                this.getChilds().put(action, childs);
            }

        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException | IOException | URISyntaxException | SimulationRunningException |
                 IllegalActionException e) {
            throw new RuntimeException(e);
        }
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game, Agent player) {
        this.game = game;
        this.deck = player.getDeck();
        this.childs.clear();
        this.player = player;
    }

    public int getN() {
        return N;
    }

    public void setN(int n) {
        this.N = n;
    }

    public ArrayList<Card> getDeck(){return this.deck;}

    public int getScore(){return this.score;}

    public void resetScore(){this.score = 0;}

    public void addScore(int value){this.score += value;}

    public Node getParent() {
        return parent;
    }

    public Agent getPlayer() {
        return player;
    }

    public void setPlayer(Agent player) {
        this.player = player;
    }

    /**Return the HashMap of the childs of the node
     * @return The HashMap of the childs*/
    public HashMap<Actuator, HashMap<Node, Double>> getChilds(){return this.childs;}

    public void addChild(Actuator act, HashMap<Node, Double> val){this.childs.put(act, val);}

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

}
