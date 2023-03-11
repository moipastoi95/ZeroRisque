package org.tovivi.environment.action;

import org.tovivi.agent.Agent;
import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;

import java.util.ArrayList;
import java.util.HashMap;

public class Fortify extends Offensive {

    private Tile fromTile;
    private Tile toTile;
    private int numTroops;

    /**
     * Fortify a tile with troops from another tile
     * @param fromTile the tile where to get troops
     * @param toTile the tile where to put troops
     * @param numTroops the number of troops to move
     */
    public Fortify(Tile fromTile, Tile toTile, int numTroops) {
        this.fromTile = fromTile;
        this.toTile = toTile;
        this.numTroops = numTroops;
    }

    /**
     * No fortification
     */
    public Fortify() {
        this(null, null, 0);
    }

    public Tile getFromTile() {
        return fromTile;
    }

    public Tile getToTile() {
        return toTile;
    }

    public int getNumTroops() {
        return numTroops;
    }

    /**
     * Specify if the player play a non fortification move
     * @return yes it does
     */
    public boolean stopFortification() {
        return toTile == null && fromTile == null && numTroops == 0;
    }

    @Override
    public boolean isMoveLegal(Agent player) {
        if (stopFortification()) {
            return true;
        }
        // each tile is owned by the player
        if(!fromTile.getOccupier().equals(player) || !toTile.getOccupier().equals(player)) {
            return false;
        }
        // the fromTile has enough troops
        if (fromTile.getNumTroops() <= numTroops) {
            return false;
        }
        return true;
    }

    @Override
    public boolean perform(Agent player) {
        if (!super.perform(player)) {
            return false;
        }

        if (stopFortification()) {
            return true;
        }

        if (!isMoveLegal(player)) {
            return false;
        }

        // proceed to the fortification
        fromTile.setNumTroops(fromTile.getNumTroops()-numTroops);
        toTile.setNumTroops(toTile.getNumTroops()+numTroops);
        return true;
    }

    @Override
    public boolean doSimulation() {
        if (!super.doSimulation()) {
            return false;
        }

        fromTile.setNumTroops(fromTile.getNumTroops()-numTroops);
        toTile.setNumTroops(toTile.getNumTroops()+numTroops);

        return true;
    }

    @Override
    public boolean undoSimulation() {
        if (!super.undoSimulation()) {
            return false;
        }

        fromTile.setNumTroops(fromTile.getNumTroops()+numTroops);
        toTile.setNumTroops(toTile.getNumTroops()-numTroops);

        return true;
    }

    @Override
    public String toString() {
        if (stopFortification()) {
            return "[Fortify:null -0-> null]";
        }
        return "[Fortify:" + fromTile.getName() + " -" + numTroops + "-> " + toTile.getName() + "]";
    }
}
