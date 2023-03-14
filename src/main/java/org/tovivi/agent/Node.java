package org.tovivi.agent;

import org.tovivi.environment.*;
public class Node {
    private Game game;
    private int value;
    private Node parent;

    public Node(Game game,int value, Node parent){
        this.game = game;
        this.value = value;
        this.parent = parent;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }
}
