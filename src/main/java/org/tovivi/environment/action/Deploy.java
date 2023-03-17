package org.tovivi.environment.action;

import org.tovivi.agent.Agent;
import org.tovivi.environment.*;
import org.tovivi.environment.action.exceptions.IllegalActionException;
import org.tovivi.environment.action.exceptions.SimulationRunningException;

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
    public boolean isNumTroopsLegal(Agent player) {
        return player.getNumDeploy() >= numTroops;
    }

    @Override
    public ArrayList<Tile> getTiles() {
        ArrayList<Tile> tiles = new ArrayList<Tile>();
        tiles.add(tile);
        return tiles;
    }

    public void setTile(Tile tile){this.tile = tile;}

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
        return tile.getOccupier().equals(player);
    }

    @Override
    public Actuator perform(Agent player)  throws SimulationRunningException, IllegalActionException {
        if (!super.isSimulating()) {
            throw new SimulationRunningException();
        }

        if (!isMoveLegal(player)) {
            throw new IllegalActionException();
        }

        if (!stopDeploy()) {
            tile.setNumTroops(tile.getNumTroops() + getNumTroops());
        }
        return null;
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
