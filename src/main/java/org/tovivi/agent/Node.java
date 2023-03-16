package org.tovivi.agent;

import org.tovivi.environment.*;
import org.tovivi.environment.action.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class Node {

    final int number_deploy_max = 2;
    private Game game;
    private ArrayList<Card> deck;
    private int N; //Nombre de visite de ce noeud
    private int win;
    private Node parent;

    private HashMap<Actions, HashMap<Node, Double>> childs = new HashMap<Actions, HashMap<Node, Double>>();

    public Node(Game game, int N, Node parent, ArrayList<Card> deck){
        this.game = game;
        this.N = N;
        this.parent = parent;
        this.win = 0;
        this.deck = deck;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public int getN() {
        return N;
    }

    public void setN(int n) {
        this.N = n;
    }

    public ArrayList<Card> getDeck(){return this.deck;}

    public int getWin(){return this.win;}

    public void addWin(){this.win = this.win+1;}

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public HashMap<Actions, HashMap<Node, Double>> getChilds(){return this.childs;}

    public void addChild(Actions act, HashMap<Node, Double> val){this.childs.put(act, val);}

    public boolean isChild(Actions action){return this.childs.containsKey(action);}


    public Node getNextNode(Actions act) {
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
