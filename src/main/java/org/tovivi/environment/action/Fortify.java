package org.tovivi.environment.action;

import org.tovivi.agent.Agent;
import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;
import org.tovivi.environment.action.exceptions.IllegalActionException;
import org.tovivi.environment.action.exceptions.SimulationRunningException;

import java.util.ArrayList;
import java.util.HashMap;

public class Fortify extends Offensive {

    /**
     * Fortify a tile with troops from another tile
     * @param fromTile the tile where to get troops
     * @param toTile the tile where to put troops
     * @param numTroops the number of troops to move
     */
    public Fortify(Tile fromTile, Tile toTile, int numTroops) {
        super(fromTile, toTile, numTroops);
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
        // each tile is owned by the player or the toTile has 0 troops
        if(!fromTile.getOccupier().equals(player) || !toTile.getOccupier().equals(player)) {
            return false;
        }
        // the fromTile has enough troops
        if (numTroops==0 || fromTile.getNumTroops() <= numTroops) {
            return false;
        }
        if (!connexTiles(fromTile).contains(toTile)) {
            return false;
        }
        return true;
    }

    @Override
    public Actuator perform(Agent player) throws SimulationRunningException, IllegalActionException {
        if (!super.isSimulating()) {
            throw new SimulationRunningException();
        }

        if (stopFortification()) {
            return null;
        }

        if (!isMoveLegal(player)) {
            throw new IllegalActionException();
        }

        // proceed to the fortification
        fromTile.setNumTroops(fromTile.getNumTroops()-numTroops);
        toTile.setNumTroops(toTile.getNumTroops()+numTroops);

        return null;
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

    public static ArrayList<Tile> connexTiles(Tile t) {
        ArrayList<Tile> res = new ArrayList<>();
        ArrayList<Tile> front = new ArrayList<>(); front.add(t);
        while (!front.isEmpty()) {
            String color = front.get(0).getOccupier().getColor();
            ArrayList<Tile> neigh = front.get(0).getNeighbors();
            res.add(front.remove(0));
            for (Tile n : neigh) {
                if (!front.contains(n) && !res.contains(n) && n.getOccupier().getColor().compareTo(color)==0) {
                    front.add(n);
                }
            }
        }
        return res;
    }

    @Override
    public String toString() {
        if (stopFortification()) {
            return "[Fortify:null -0-> null]";
        }
        return "[Fortify:" + fromTile.getName() + " -" + numTroops + "-> " + toTile.getName() + "]";
    }
}
