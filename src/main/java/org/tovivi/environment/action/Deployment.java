package org.tovivi.environment.action;

import org.tovivi.agent.Agent;
import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;

public class Deployment extends Actuator {

    private ArrayList<Deploy> deploys;

    /**
     * Deploy a certain amount of troop on the tile
     * @param deploys ArrayList of deploy, that represent a deployment on 1 tile
     */
    public Deployment(ArrayList<Deploy> deploys) {
        this.deploys = deploys;
    }

    /**
     * No deployment
     */
    public Deployment() {
        this(new ArrayList<Deploy>());
    }

    public ArrayList<Deploy> getDeploys() {
        return deploys;
    }

    @Override
    public boolean isMoveLegal(Agent player) {
        int countTroops = 0;
        for(Deploy dep : deploys) {
            if (!dep.stopDeploy()) {
                countTroops += dep.getNumTroops();
                // check if the deployment is on the player tile
                if (dep.getTile().getOccupier() != player) {
                    return false;
                }
            }
        }
        // make sure to deploy a limited number of troops
        if (countTroops > player.getNumDeploy()) {
            return false;
        }
        return true;
    }

    @Override
    public boolean perform(Agent player) {
        if (!super.perform(player)) {
            return false;
        }

        if (!isMoveLegal(player)) {
            return false;
        }

        // perform the deployment
        for(Deploy dep : deploys) {
            if (!dep.stopDeploy()) {
                dep.getTile().setNumTroops(dep.getTile().getNumTroops() + dep.getNumTroops());
            }
        }
        return true;
    }

    @Override
    public boolean doSimulation(Game game) {
        if (!super.doSimulation(game)) {
            return false;
        }
        // perform it
        for(Deploy dep : deploys) {
            if (!dep.stopDeploy()) {
                Tile tile = game.getTiles().get(dep.getTile().getName());
                tile.setNumTroops(tile.getNumTroops() + dep.getNumTroops());
            }
        }
        return true;
    }

    @Override
    public boolean undoSimulation(Game game) {
        if (!super.undoSimulation(game)) {
            return false;
        }

        // no order issues
        for(Deploy dep : deploys) {
            if (!dep.stopDeploy()) {
                Tile tile = game.getTiles().get(dep.getTile().getName());
                tile.setNumTroops(tile.getNumTroops() - dep.getNumTroops());
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String msg = "";
        for(Deploy dep : deploys) {
            msg += dep; // + "\n";
        }
        return msg;
    }

}
