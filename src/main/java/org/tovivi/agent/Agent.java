package org.tovivi.agent;

import org.tovivi.environment.*;
import org.tovivi.environment.action.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.function.Function;

public abstract class Agent implements Callable<Actions> {

    private String color;
    private ArrayList<Tile> tiles;
    private Game game;
    private ArrayList<Card> deck;

    /**
     * @param color : String of the color
     * @param game : ref to the game object
     * */
    public Agent(String color, Game game) {
        this.color = color;
        this.tiles = new ArrayList<>();
        this.deck = new ArrayList<>();
        this.game = game;
    }
    public Agent(String color) {
        this(color, null);
    }

    /**
     * Add a tile to the list of the player's tiles
     * @param tile : the new tile
     * @author Vincent
     */
    public void addTile(Tile tile) {
        tiles.add(tile);
    }

    /**
     * Remove a tile from the list of the player's tiles
     * @param tile : the tile to remove
     */
    public void removeTile(Tile tile) {
        tiles.remove(tile);
    }

    /**
     * Remove a tile from the list of the player's tiles
     * @param  : id of the tile to remove
     */
    /*
    public void removeTile(int idTile) {
        boolean found = false;
        Iterator<Tile> i = this.tiles.iterator();
        while (!found && i.hasNext()) {
            if (i.next().getId() == idTile) {
                i.remove();
            }
        }
    }
    */
    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    /**
     * Get a list of tiles owned by the player
     * @return the tiles
     */
    public ArrayList<Tile> getTiles() {
        return this.tiles;
    }

    /**
     * Get the color of the player
     * @return the color (String)
     */
    public String getColor() {
        return this.color;
    }

    public ArrayList<Card> getDeck() {
        return deck;
    }

    /**
     * Get the number of troops the player is able to deploy at each turn
     * @return the number of troops
     */
    public final int getNumDeploy() {
        int total = 0;
        if (getTiles().size() <= 3) {
            total = 3;
        } else {
            total = (int) (getTiles().size()/3);

            // adding bonus for continents
            // retrieve continents where the player owns territories
            HashMap<String, Continent> continents = new HashMap<String, Continent>();
            for (Tile t : getTiles()) {
                if (!continents.containsKey(t.getContinent().getName())) {
                    continents.put(t.getContinent().getName(), t.getContinent());
                }
            }
            for(Continent c : continents.values()) {
                if(c.getOccupier() != null && c.getOccupier().equals(this)) {
                    total += c.getBonus();
                }
            }
        }
        return total;
    }

    public abstract Actions action();

    public abstract Deployment getNextDeploy();
    public abstract Attack getNextAttack();
    public abstract Fortify getFortify();

    @Override
    public String toString() {
        return "Player " + color;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Agent) {
            Agent p = (Agent) obj;
            return p.getColor() == this.getColor();
        }
        return false;
    }

    @Override
    public Actions call() throws Exception {
        return action();
    }
}
