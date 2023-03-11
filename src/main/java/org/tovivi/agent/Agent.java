package org.tovivi.agent;

import org.tovivi.environment.*;
import org.tovivi.environment.action.Actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public abstract class Agent {

    private String color;
    private ArrayList<Tile> tiles;
    private Game mGame;

    /**
     * Main constructor
     * @param color : String of the color
     * @param game : ref to the game object
     * */
    public Agent(String color, Game mGame) {
        this.color = color;
        this.tiles = new ArrayList<>();
        this.mGame = mGame;
    }

    /**
     * Add a tile to the list of the player's tiles
     * @param tile : the new tile
     * @author Vincent
     */
    public void addTile(Tile tile) {
        tiles.add(tile);
    }

    public Game getGame() {
        return mGame;
    }

    /**
     * Remove a tile from the list of the player's tiles
     * @param tile : the tile to remove
     */
    public void removeTile(Tile tile) {
        tiles.remove(tile);
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

    /**
     * Get the number of troops the player is able to deploy at each turn
     * @return the number of troops
     */
    public int getNumDeploy() {
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
}
