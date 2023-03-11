package org.tovivi.environment.action;

import org.tovivi.agent.Agent;
import org.tovivi.environment.*;

import java.util.ArrayList;

public class Deploy extends Deployment {
    private int numTroops = 0;
    private Tile tile;

    /**
     * Deploy a certain amount of troop on the tile
     * @param numTroops the number of troops
     * @param tile the tile where to deploy
     */
    public Deploy(int numTroops, Tile tile) {
        this.numTroops = numTroops;
        this.tile = tile;
    }

    /**
     * Deploy 0 units
     */
    public Deploy() {
        this(0, null);
    }

    public int getNumTroops() {
        return numTroops;
    }

    @Override
    public ArrayList<Tile> getTiles() {
        ArrayList<Tile> tiles = new ArrayList<Tile>();
        tiles.add(tile);
        return tiles;
    }

    /**
     * Specify if the player doesn't want to deploy troops
     * @return
     */
    public boolean stopDeploy() {
        return numTroops == 0 && tile == null;
    }

    @Override
    public String toString() {
        if (stopDeploy()) {
            return "[Deploy:null -> +0]";
        }
        return "[Deploy:" + tile.getName() + " -> +" + numTroops + "]";
    }

    @Override
    boolean isMoveLegal(Agent player) {
        return tile.getOccupier().equals(player) && player.getNumDeploy() >= numTroops;
    }

    @Override
    public boolean perform(Agent player) {
        if (!super.perform(player)) {
            return false;
        }

        if (!isMoveLegal(player)) {
            return false;
        }

        if (!stopDeploy()) {
            tile.setNumTroops(tile.getNumTroops() + getNumTroops());
        }
        return true;
    }

    @Override
    public boolean doSimulation() {
        if (!super.doSimulation()) {
            return false;
        }
        // perform it
        if (!stopDeploy()) {
            tile.setNumTroops(tile.getNumTroops() + getNumTroops());
        }
        return true;
    }

    @Override
    public boolean undoSimulation() {
        if (!super.undoSimulation()) {
            return false;
        }
        // perform it
        if (!stopDeploy()) {
            tile.setNumTroops(tile.getNumTroops() - getNumTroops());
        }
        return true;
    }
}
