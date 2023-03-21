package org.tovivi.agent;

import org.tovivi.environment.Card;
import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;
import org.tovivi.environment.action.*;
import org.tovivi.environment.action.exceptions.IllegalActionException;
import org.tovivi.environment.action.exceptions.SimulationRunningException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.*;

import static java.lang.Math.*;

public class AgentMonteCarlo extends Agent {

    private Node root;

    private Node actual_node;

    private String phase = "Deploy"; //Deploy, Attack, Fortify

    int num_to_deploy = 0;

    private int E = 10;
    private double c = sqrt(2); //Paramètre d'exploration
    private int depth = 0; //Profondeur actuelle de la recherche

    public AgentMonteCarlo(String color, Game game) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        super(color, game);
        this.root = new Node(new Game(game),0, null, this.getDeck(), this);
    }

    public AgentMonteCarlo(String color) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        super(color);
    }

    public AgentMonteCarlo(Agent agent){
        super(agent);
    }

    public void setGame(Game game) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        super.setGame(game);
    }

    /**Update the game copy that is stored in the root node*/
    public void setRoot() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        this.root = new Node(new Game(this.getGame()),0, null, this.getDeck(), this);
    }

    public void setPhase(String phase) {this.phase = phase;}

    public Actions action(){return null;}

    //TODO: while(ressource_left):
    // 0 - Choisir le nouveau noeud à partir duquel explorer (fonction traverse)
    // 1 - A partir de ce noeud parcourir l'arbre jusqu'à un état final (fonction rollout)
    // 2 - Calculer la valeur de l'état final (en fonction de qui à gagné)
    // 3 - Propager la valeur de manière récursive sur les noeuds parents (fonction backpropagate)
    // 4 - Renvoyer le meilleur child
    public Actuator actionTest() throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        this.root.setN(0);
        this.root.resetScore();
        depth = 0;

        try{
            this.root.setGame(new Game(this.getGame()), this);
        } catch(Exception e){
            throw new RuntimeException(e);
        }

        Random rand = new Random();
        Actuator new_action;

        while(depth < 2 && this.phase != "Fortify"){
            depth = 0;
            this.traverse(); //Etape 0

            Agent player = this.actual_node.getGame().getPlayers().get(this.getColor());

            HashMap<Tile, ArrayList<Tile>> front = this.getFront(actual_node.getGame(), this);
            ArrayList<Actuator> possible_actions = new ArrayList<>();

            if(this.phase == "Deploy") {
                if(num_to_deploy == 0) num_to_deploy = this.getNumDeploy();
                possible_actions = this.getDeployActions(front, player);
            }

            else if(this.phase == "Attack" || this.phase == "DepAttack"){
                possible_actions = this.getAttackActions(front, player);
            }

            ArrayList<Actuator> redundant = new ArrayList<>();
            for (Actuator act: actual_node.getChilds().keySet()) {
                for (Actuator newact : possible_actions) {
                    if (act.toString().equals(newact.toString()))
                        redundant.add(newact);
                }
            }
            possible_actions.removeAll(redundant);

            int s = possible_actions.size();
            new_action = possible_actions.get(rand.nextInt(s));
            actual_node.generateChilds(new_action, player);
            Node next_node = actual_node.getNextNode(new_action);
            int Score = this.rollout(next_node, player);
            this.backPropagate(next_node, Score);

            if(this.phase == "DepAttack") this.phase = "Deploy";
        }

        new_action = this.best_child();

        if(this.phase == "Deploy" || this.phase == "DepAttack") this.phase = "Attack";
        else if(this.phase == "Attack") this.phase = "Fortify";
        else if(this.phase == "Fortify") this.phase = "Deploy";

        this.depth = 0;

        return new_action;
    }

    private ArrayList<Actuator> getAttackActions(HashMap<Tile, ArrayList<Tile>> front, Agent player) {
        ArrayList<Actuator> actions = new ArrayList<>();

        for(Tile tile: front.keySet()){
            for(Tile oppTile: front.get(tile)){
                if(tile.getNumTroops()-1 != 0){
                    actions.add(new Attack(tile, oppTile, tile.getNumTroops()-1, null, null));
                }
            }
        }
        return actions;
    }

    private ArrayList<Actuator> getDeployActions(HashMap<Tile, ArrayList<Tile>> front, Agent player) {
        ArrayList<Actuator> possible_actions= new ArrayList<>();
        // if cards owned, use them
        ArrayList<Deployment> depL = new ArrayList<>();
        ArrayList<Card> goodCards = Card.chooseCards(this.actual_node.getDeck(), player);
        int goodCardsValue = Card.count(goodCards, player);
        if (goodCardsValue > 0) {
            PlayCards pc = new PlayCards(goodCards, player);
            depL.add(pc);
            depL.addAll(pc.autoDeploy());
            num_to_deploy += Card.countOnlyCombo(goodCards, player);
        }

        for(Tile tile: front.keySet()){
            depL.add(new Deploy(num_to_deploy, tile));
            //System.out.println(depL);
            possible_actions.add(new MultiDeploy(new ArrayList<>(depL)));
            depL.remove(depL.size() - 1);
        }

        return possible_actions;
    }

    /**
    //TODO: Je pense ya un problème il faut rajouter le joueur avec lequelle on veut jouer dans les paramètres
    public Actions getNewAction(HashMap<Tile, ArrayList<Tile>> front) throws IOException, URISyntaxException {
        Actions act = null;
        Agent player = this.actual_node.getGame().getPlayers().get(this.getColor());
        String OpColor = this.getColor();
        MultiDeploy deployPart = null;
        Offensive offensivePart = null;

        //while(!this.actual_node.getChilds().containsKey(act)) {

        //Deployement on one or two tile of the frontier of the current player
        deployPart = this.createDeployment(front, this.getNumDeploy(), player);
        deployPart.doSimulation();

        offensivePart = this.createAttack(front, 0, 2, player);

        deployPart.undoSimulation();

        if(this.getColor() == "Red") OpColor = "Blue";
        else OpColor = "Red";
        //Alternance entre les deux joueurs
        if(player == this) player = this.getGame().getPlayers().get(OpColor);
        else player = this;
        //}

        this.convertDeploy(deployPart);
        this.convertOffensive(offensivePart);

        return new Actions(deployPart, offensivePart);
    }*/


    /**
    public Offensive createAttack(HashMap<Tile, ArrayList<Tile>> front, int actDepth, int maxDepth, Agent player){
        //System.out.println("Depth : " + actDepth);
        boolean flag;
        if(actDepth != maxDepth) flag = true;
        else {
            return new Fortify();
        }
        Attack offensive = null;
        while(flag) {
            //Choose a random tile from where to attack in the frontier
            ArrayList<Tile> frontKeys = new ArrayList<>(front.keySet());

            Tile fromTile = frontKeys.get((int) (Math.random() * frontKeys.size()));
            // attack a random tile next to the tile chosen
            Tile toTile = front.get(fromTile).get((int) (Math.random() * front.get(fromTile).size()));

            int attackers = fromTile.getNumTroops();
            int defenders = toTile.getNumTroops();
            double prob = this.getProba(attackers-1, defenders-1);
            if(Math.random() < prob){
                //Modification du game en supposant une loose:
                fromTile.setNumTroops(1);
                HashMap<Tile, ArrayList<Tile>> frontLoose = front;

                //Modification du game en cas de win
                toTile.setOccupier(player, (attackers-1)/2);
                HashMap<Tile, ArrayList<Tile>> frontWin = front;

                offensive = new Attack(fromTile, toTile, attackers-1,
                        createAttack(frontWin, actDepth+1, maxDepth, player),
                        createAttack(frontLoose, actDepth+1, maxDepth, player));
                flag = false;
            }
        }
        return offensive;
    }*/

    /**
    private MultiDeploy createDeployment(HashMap<Tile, ArrayList<Tile>> front, int numTroops, Agent player) {
        Random rand =  new Random();
        MultiDeploy deployPart = null;

        // if cards owned, use them
        ArrayList<Deployment> depL = new ArrayList<>();
        ArrayList<Card> goodCards = Card.chooseCards(this.actual_node.getDeck(), player);
        int goodCardsValue = Card.count(goodCards, player);
        if (goodCardsValue > 0) {
            PlayCards pc = new PlayCards(goodCards, player);
            depL.add(pc);
            depL.addAll(pc.autoDeploy());
            numTroops += Card.countOnlyCombo(goodCards, this);
        }

        int x = rand.nextInt(2)+1; //Choose between 1 deployment or two deployment on a frontier tile
        for(int i = 0; i<x;i++) {
            // pick a random tiles from the front list
            ArrayList<Tile> frontKeys = new ArrayList<>(front.keySet());
            Tile fromTile = frontKeys.get((int) (Math.random() * frontKeys.size()));
            int numToDeploy;
            if(numTroops%2 != 0){
                if(i == 0) numToDeploy = numTroops/x;
                else numToDeploy = numTroops/x+1;
            }
            else numToDeploy = numTroops/x;

            depL.add(new Deploy(numToDeploy, fromTile));

        }
        deployPart = new MultiDeploy(depL);
        return deployPart;
    }*/

    @Override
    public Deployment getNextDeploy() {
        return null;
    }

    @Override
    public Attack getNextAttack() {
        return null;
    }

    @Override
    public Fortify getFortify() {
        return null;
    }

    /**Parcours l'arbre actuel jusqu'au meilleur leaf node actuel
     * @return le meilleur noeud feuille
     * */
    public void traverse() throws IOException, URISyntaxException {
        actual_node = this.root;
        Actuator act = null;
        if(Objects.equals(this.phase, "Deploy")){
            if(num_to_deploy == 0) num_to_deploy = this.getNumDeploy();
            if (actual_node.getChilds().size() ==
                    this.getDeployActions(this.getFront(actual_node.getGame(), actual_node.getPlayer()), actual_node.getPlayer()).size()) {
                depth += 1;
                act = this.getBestChild(actual_node);
                actual_node = actual_node.getNextNode(act);
                this.phase = "DepAttack";
            }
            num_to_deploy = 0;
        }
        if(Objects.equals(this.phase, "Attack") || this.phase == "DepAttack") {
            Agent player = this.actual_node.getGame().getPlayers().get(this.getColor());
            while (actual_node.getChilds().size() ==
                    this.getAttackActions(this.getFront(actual_node.getGame(), this), player).size()) {
                depth += 1;
                act = this.getBestChild(actual_node);
                actual_node = actual_node.getNextNode(act);
                player = this.actual_node.getGame().getPlayers().get(this.getColor());
            }
        }
    }

    /**
     * Calcul la valeur UCT d'un Node n en fonction du nombre de fois ou il a été visité et du nombre de fois ou son noeud parent
     * a été visité.
     *
     * @param n       : noeud dont on vaut calculer la valeur
     * @param prob
     * @return La valeur sous forme d'un double
     */
    public double getUCT(Node n, Double prob){
        if(n.getN() == 0){
            return Double.MAX_VALUE;
        }
        else {
            return E*prob*(n.getScore()/n.getN()) + c*sqrt(log(n.getParent().getN())/n.getN());
        }
    }

    /**Renvoie le meilleur enfant de la famille
     * @return Le meilleur noeud
     * */
    public Actuator getBestChild(Node n){
        double max = -Double.MAX_VALUE;
        Actuator res = null;
        for(Actuator act: n.getChilds().keySet()) {
            for (Node child : n.getChilds().get(act).keySet()) {
                //System.out.print("Calcul de luct");
                double i = this.getUCT(child, n.getChilds().get(act).get(child));
                if (i > max && child.getN() > 0) {
                    max = i;
                    res = act;
                }
                //System.out.println(" - " + i + " max = " + max + " - Ca s'est bien passé..." + act);
            }
        }
        //if(res != null) System.out.print(" - " + res);
        return res;
    }

    //TODO: Doit renvoyer la valeur du leaf node atteint à partir du noeud n
    // Pour ce faire, parcours le jeu de manière aléatoire
    // Pour le moment c'est de type très très nul mais oklm
    public int rollout(Node n, Agent player){
        Random rand = new Random();
        int res = 0;
        try{
            Game simu = new Game(n.getGame());
            Agent gamer = simu.getPlayers().get(player.getColor());
            res = simu.score(player);
            for(int i = 0; i < 0; i++){
                HashMap<Tile, ArrayList<Tile>> front = getFront(simu, gamer);
                ArrayList<Actuator> possible_actions = this.getAttackActions(front, gamer);
                int s = possible_actions.size();
                Attack new_action = (Attack) possible_actions.get(rand.nextInt(s));

                new_action.perform(gamer);
                res = simu.score(gamer);
            }
        } catch (IOException | URISyntaxException | ClassNotFoundException | InvocationTargetException |
                 NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalActionException |
                 SimulationRunningException e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
        return res;
    }

    //TODO: Doit propager la valeur du noeud final à tous les noeuds précedents déjà exploré dans l'arbre
    public void backPropagate(Node n, int result){
        if(n.getParent() != null){
            n.addScore(result);
            n.setN(n.getN() + 1);
            backPropagate(n.getParent(), result);
        }
        else{
            n.addScore(result);
            n.setN(n.getN() + 1);
        }
    }

    //TODO: Doit renvoyer le meilleur noeuds, genre l'action que l'algo doit renvoyer en gros
    public Actuator best_child(){
        if(!this.phase.equals("laEnd")) {
            if(this.phase.equals("End")) {
                this.phase = "laEnd";
            }
            return this.getBestChild(this.root);
        }
        else {
            this.phase = "Deploy";
            this.num_to_deploy = 0;
            return null;
        }
    }


    /**Return the front for the agent in the game of the node n, I should probably add player as an parameter
     * @param g The node where we want to find the frontier
     * @return The frontier as an HashMap(Tile, List(Tile))*/
    public HashMap<Tile, ArrayList<Tile>> getFront(Game g, Agent player) throws IOException, URISyntaxException {
        // get every tile next to an opponent tile, and retrieve opponent's tile next to them
        HashMap<Tile, ArrayList<Tile>> front = new HashMap<>();
        for (Tile t : g.getTiles().values()) {
            if(t.getOccupier().getColor() == player.getColor()) {
                boolean flag = false;
                ArrayList<Tile> opponentTiles = new ArrayList<>();
                if(t.getNumTroops() > 1) {
                    for (Tile neighbor : t.getNeighbors()) {
                        if (neighbor.getOccupier().getColor() != player.getColor()) {
                            flag = true;
                            // add the real ref of tiles
                            opponentTiles.add(g.getTiles().get(neighbor.getName()));
                        }
                    }
                }
                if (flag) {
                    front.put(t, opponentTiles);
                }
            }
        }
        return front;
    }
}
