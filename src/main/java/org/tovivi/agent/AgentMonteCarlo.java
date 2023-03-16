package org.tovivi.agent;

import org.tovivi.environment.Card;
import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;
import org.tovivi.environment.action.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

import static java.lang.Math.*;

public class AgentMonteCarlo extends Agent {

    //TODO: Ajouter les variables nécessaire à l'algo mdrrr
    // Surtout genre comment stocker l'arbre des coups à jouer et leur valeur....

    private Node root;

    private Node actual_node;

    private int E = 1;
    private double c = sqrt(2); //Paramètre d'exploration
    private int depth = 0; //Profondeur actuelle de la recherche
    public AgentMonteCarlo(String color, Game game) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        super(color, game);
        this.root = new Node(new Game(game),0,null, this.getDeck());
    }

    @Override
    //TODO: while(ressource_left):
    // 0 - Choisir le nouveau noeud à partir duquel explorer (fonction traverse)
    // 1 - A partir de ce noeud parcourir l'arbre jusqu'à un état final (fonction rollout)
    // 2 - Calculer la valeur de l'état final (en fonction de qui à gagné)
    // 3 - Propager la valeur de manière récursive sur les noeuds parents (fonction backpropagate)
    // 4 - Renvoyer le meilleur child
    public Actions action() {

        while(depth < 5){
            this.actual_node = this.traverse(); //Etape 0

            //On récupère les tiles correspondant au front, tiles adjacentes à des adversaires
            HashMap<Tile, ArrayList<Tile>> front = this.getFront(actual_node);

            Actions new_action = this.getNewAction(front);

        }





        return null;
    }


    public Actions getNewAction(HashMap<Tile, ArrayList<Tile>> front) {
        Actions act = null;
        Agent player = this;
        String OpColor = "";
        if(this.getColor() == "Red") OpColor = "Blue";
        else OpColor = "Red";

        while(!this.actual_node.getChilds().containsKey(act)) {
            //Alternance entre les deux joueurs
            if(player == this) player = this.getGame().getPlayers().get(OpColor);
            else player = this;

            //Deployement on one or two tile of the frontier of the current player
            MultiDeploy deployPart = this.createDeployment(front, this.getNumDeploy(), player);
            deployPart.doSimulation();

            //Attack phase


        }

        return null;
    }

    private MultiDeploy createDeployment(HashMap<Tile, ArrayList<Tile>> front, int numTroops, Agent player) {
        Random rand =  new Random();
        MultiDeploy deployPart = null;

        // if cards owned, use them
        ArrayList<Deployment> depL = new ArrayList<>();
        ArrayList<Card> goodCards = Card.chooseCards(this.actual_node.getDeck(), player);
        int goodCardsValue = Card.value(goodCards, player);
        if (goodCardsValue > 0) {
            depL.add(new PlayCards(goodCards, player));
        }
        numTroops += goodCardsValue;

        int x = rand.nextInt(2)+1; //Choose between 1 deployment or two deployment on a frontier tile
        for(int i = 0; i<2;i++) {
            // pick a random tiles from the front list
            ArrayList<Tile> frontKeys = new ArrayList<>(front.keySet());
            Tile fromTile = frontKeys.get((int) (Math.random() * frontKeys.size()));
            int numToDeploy;
            if(numTroops%2 != 0){
                if(i == 0) numToDeploy = numTroops/2;
                else numToDeploy = numTroops/2+1;
            }
            else numToDeploy = numTroops/2;

            depL.add(new Deploy(numToDeploy, fromTile));
            deployPart = new MultiDeploy(depL);

        }
        return deployPart;
    }

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
    public Node traverse(){
        Node node = this.root;
        Actions act = null;
        while(node.getChilds().size() == 100){
               act = this.getBestChild(node);
               node = node.getNextNode(act);
        }
        return node;
    }

    /**Calcul la valeur UCT d'un Node n en fonction du nombre de fois ou il a été visité et du nombre de fois ou son noeud parent
     *a été visité.
     * @param n : noeud dont on vaut calculer la valeur
     * @return La valeur sous forme d'un double
     * */
    public double getUCT(Node n){
        if(n.getN() == 0){
            return Double.MAX_VALUE;
        }
        else {
            return E*(n.getWin()/n.getN()) + c*sqrt(log(n.getParent().getN())/n.getN());
        }
    }

    /**Renvoie le meilleur enfant de la famille
     * @return Le meilleur noeud
     * */
    public Actions getBestChild(Node n){
        double max = 0;
        Actions res = null;
        for(Actions act: n.getChilds().keySet()) {
            for (Node child : n.getChilds().get(act).keySet()) {
                double i = this.getUCT(child);
                if (i > max) {
                    max = i;
                    res = act;
                }
            }
        }
        return res;
    }

    //TODO: Doit renvoyer la valeur du leaf node atteint à partir du noeud n
    public int rollout(Node n){
        return 0;
    }

    //TODO: Doit renvoyer le prochain noeud à parcourir (Au départ un noeud random et après on pourra rajouter une genre de
    // heuristic ou le NN pour qu'il explore pas random mais dans un ordre logique)
    public Node rolloutPolicy(Node n){
        return null;
    }

    //TODO: Doit propager la valeur du noeud final à tous les noeuds précedents déjà exploré dans l'arbre
    public void backPropagate(Node n, int result){

    }

    //TODO: Doit renvoyer le meilleur noeuds, genre l'action que l'algo doit renvoyer en gros
    public Action best_child(){
        return null;
    }

    public HashMap<Tile, ArrayList<Tile>> getFront(Node n) {
        // get every tile next to an opponent tile, and retrieve opponent's tile next to them
        HashMap<Tile, ArrayList<Tile>> front = new HashMap<>();
        for (Tile t : n.getGame().getTiles().values()) {
            boolean flag = false;
            ArrayList<Tile> opponentTiles = new ArrayList<>();
            for (Tile neighbor : t.getNeighbors()) {
                if (!neighbor.getOccupier().equals(this)) {
                    flag = true;
                    // add the real ref of tiles
                    opponentTiles.add(getGame().getTiles().get(neighbor.getName()));
                }
            }
            if (flag) {
                front.put(getGame().getTiles().get(t.getName()), opponentTiles);
            }
        }
        return front;
    }
}
