package org.tovivi.environment;

import org.tovivi.agent.Agent;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class Tile {

    private Continent continent;
    private String name;
    private Agent occupier;
    private boolean inConflict;
    private PropertyChangeSupport support;
    private int numTroops;
    private ArrayList<Tile> neighbors;

    public Tile(Tile tile) {
        this.continent = null;
        this.name = tile.getName();
        this.occupier = null;
        this.support = new PropertyChangeSupport(this);
        this.numTroops = tile.getNumTroops();
        this.inConflict=false;
        /*for(Tile j : tile.getNeighbors()){
            this.neighbors.add(new Tile(j));
        }*/
        this.neighbors = tile.getNeighbors(); //Not a deep copy
    }

    /**
     * Main constructor. Create an empty tile.
     * @param name : the name of the territory
     * @param name : the continent it belongs to
     */
    public Tile(String name, Continent continent) {
        this.name = name;
        this.occupier = null;
        this.numTroops = 0;
        this.neighbors = new ArrayList<>();
        this.continent = continent;
        support = new PropertyChangeSupport(this);
        addPropertyChangeListener(continent);
    }

    /**
     * Add a tile to the list of neighbors
     * @param tile : the tile to add
     */
    public void addNeighbor(Tile tile) {
        if (!neighbors.contains(tile)) {
            neighbors.add(tile);
        }
    }

    /**
     * Add a list of tiles as neighbors
     * @param tiles : list of tiles
     */
    public void addNeighbors(ArrayList<Tile> tiles) {
        for(Tile item : tiles) {
            addNeighbor(item);
        }
    }

    /**
     * Put a player on a tile. Moreover, give the new player a ref to the tile and remove the ref from the other player.
     * @param p : the player
     * @param numTroops : the number of troops to put on it. Has to be greater or equal to 1
     */
    public void setOccupier(Agent p, int numTroops) {
        if (numTroops >= 1) {
            p.addTile(this);
            if (this.occupier != null) {
                this.occupier.removeTile(this);
            }
            support.firePropertyChange("newOccupier", this.occupier, p);
            this.occupier = p;
            setNumTroops(numTroops);
        }
        else {
            //TODO Faire des exceptions propres pour ce genre de cas... oui c'est chiant je sais
            System.out.println("Invalid number of troops");
        }
    }

    public boolean isInConflict() {
        return inConflict;
    }

    public void setInConflict(boolean inConflict) {
        support.firePropertyChange("inConflictChange", this.inConflict, inConflict);
        this.inConflict = inConflict;
    }

    public String getName() {
        return name;
    }

    public Agent getOccupier() {
        return occupier;
    }

    public int getNumTroops() {
        return numTroops;
    }

    /**
     * Set the num of troops
     * @param numTroops Has to be positive. 0 is allowed
     */
    public void setNumTroops(int numTroops) {
        if (numTroops >= 0) {
            support.firePropertyChange("newNumTroops", getNumTroops(), numTroops);
            this.numTroops = numTroops;
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    public void setContinent(Continent continent) {
        this.continent = continent;
    }

    public Continent getContinent() {return continent;};

    public ArrayList<Tile> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(ArrayList<Tile> neighbors) {
        this.neighbors = neighbors;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tile) {
            Tile t = (Tile) obj;
            return t.name == this.name;
        }
        return false;
    }

    @Override
    public String toString() {
        return "The tile " + this.name + " is occupied by " + this.occupier + " with " + this.numTroops + " troops.";
    }
}
