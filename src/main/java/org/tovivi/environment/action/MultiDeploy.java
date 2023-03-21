package org.tovivi.environment.action;

import org.tovivi.agent.Agent;
import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;
import org.tovivi.environment.action.exceptions.IllegalActionException;
import org.tovivi.environment.action.exceptions.SimulationRunningException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

public class MultiDeploy extends Deployment {

    private ArrayList<Deployment> deploys;

    /**
     * Deploy a certain amount of troop on the tile
     * @param deploys ArrayList of deploy, that represent a deployment on 1 tile
     */
    public MultiDeploy(ArrayList<Deployment> deploys) {
        this.deploys = deploys;
    }

    /**
     * No deployment
     */
    public MultiDeploy() {
        this(new ArrayList<Deployment>());
    }

    public ArrayList<Deployment> getDeploys() {
        return deploys;
    }

    public void setDeploys(ArrayList<Deployment> deploys) {
        this.deploys=deploys;
    }

    @Override
    public boolean isMoveLegal(Agent player) {
        // check every object legal move
        for(Deployment dep : deploys) {
            if (!dep.isMoveLegal(player)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Actuator perform(Agent player) throws SimulationRunningException, IllegalActionException {
        if (!super.isSimulating()) {
            throw new SimulationRunningException();
        }

        // TODO : useful ? a check is already done in the perform function of each deploy/Playcard object
//        if (!isMoveLegal(player)) {
//            return false;
//        }

        // perform the deployment
        if (!stopDeploy()) {
            deploys.get(0).perform(player);
            deploys.remove(0);
        }
        if (!stopDeploy()) {
            return this;
        }
        return null;
    }

    @Override
    public boolean doSimulation() {
        if (!super.doSimulation()) {
            return false;
        }
        // perform it
        for(Deployment dep : deploys) {
            if (!dep.doSimulation()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean undoSimulation() {
        if (!super.undoSimulation()) {
            return false;
        }

        // be careful to the order
        ListIterator<Deployment> it = deploys.listIterator(deploys.size());
        while(it.hasPrevious()) {
            if (!it.previous().undoSimulation()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String msg = "";
        for(Deployment dep : deploys) {
            msg += dep; // + "\n";
        }
        return msg;
    }

    @Override
    public boolean stopDeploy() {
        return deploys.size() == 0;
    }

    @Override
    public ArrayList<Tile> getTiles() {
        ArrayList<Tile> tile = new ArrayList<>();
        for (Deployment dep : deploys) {
            tile.addAll(dep.getTiles());
        }
        return tile;
    }

    @Override
    public int getNumTroops() {
        int count = 0;
        for (Deployment dep : deploys) {
            count += dep.getNumTroops();
        }
        return count;
    }

    @Override
    public boolean isNumTroopsLegal(Agent player) {
        // make sure to deploy a limited number of troops
        return getNumTroops() <= player.getNumDeploy();
    }

    /**
     * count only troops generate from the territories, not bonus from cards
     * @return
     */
    public int getPositiveNumTroops() {
        int count = 0;
        for (Deployment dep : deploys) {
            if (dep.getNumTroops() >= 0) {
                count += dep.getNumTroops();
            }
        }
        return count;
    }
}
