package org.tovivi.environment.action;

import org.tovivi.agent.Agent;
import org.tovivi.environment.Card;
import org.tovivi.environment.Tile;
import org.tovivi.environment.action.exceptions.IllegalActionException;
import org.tovivi.environment.action.exceptions.SimulationRunningException;

import java.io.IOException;
import java.net.URISyntaxException;

public class Actions {
    private Deployment deployment;
    private Offensive firstOffensive;
    private boolean onLiveAction = false;

    /**
     * A following of depoy, attack and fortify moves. It represents the turn of a player
     * @param deployment the deployment phase
     * @param firstOffensive the attack + fortify phase
     */
    public Actions(Deployment deployment, Offensive firstOffensive) {
        this.deployment = deployment;
        this.firstOffensive = firstOffensive;
    }

    public Actions() {
        this(null, null);
        this.onLiveAction = true;
    }

    /**
     * perform 1 deployment operation
     * @param player the player
     * @return tell if there is more deployment operation left
     * @throws IllegalActionException
     * @throws SimulationRunningException
     */
    public boolean performDeployment(Agent player) throws IllegalActionException, SimulationRunningException, IOException, URISyntaxException {
        if (onLiveAction) {
            if(deployment != null) deployment = (Deployment) deployment.perform(player);
            return deployment != null;
        }
        if (deployment != null) {
            deployment = (Deployment) deployment.perform(player);
        }
        return deployment != null;
    }

    /**
     * perform 1 attack operation
     * @param player
     * @return tell if there is more deployment operation left
     * @throws IllegalActionException
     * @throws SimulationRunningException
     */
    public boolean performAttack(Agent player) throws IllegalActionException, SimulationRunningException {
        if (firstOffensive instanceof Fortify) {
            return false;
        }
        if (firstOffensive==null) {
            return false;
        }
        Tile toTile = firstOffensive.getToTile(); Tile fromTile = firstOffensive.getFromTile(); int numTroops = firstOffensive.getNumTroops();
        firstOffensive = (Offensive) firstOffensive.perform(player);
        if (numTroops==0 && toTile.getOccupier().equals(fromTile.getOccupier()) && fromTile.getNumTroops()>1) { // Ask for troops to move if the agent has not specified the number and if its attack succeed
            Fortify localFortify = player.getFortify(fromTile, toTile);
            if (localFortify!=null) localFortify.perform(player);
        }
        if (onLiveAction) {
            firstOffensive = player.getNextAttack();
        }
        return firstOffensive != null && firstOffensive instanceof Attack;
    }

    public void performFortify(Agent player) throws IllegalActionException, SimulationRunningException {
        firstOffensive = (Offensive) firstOffensive.perform(player);
    }

    public Deployment getDeployment(Agent player) throws IOException, URISyntaxException, IllegalActionException, SimulationRunningException {
        if (onLiveAction) {
            if(deployment == null) deployment = player.getNextDeploy();
        }
        return deployment;
    }

    public Offensive getFirstOffensive(Agent player, String phase) {
        if(onLiveAction) {
            if(phase.compareTo("Attacking")==0) firstOffensive = player.getNextAttack();
            else if(phase.compareTo("Fortifying")==0) firstOffensive = player.getFortify();
        }
        return firstOffensive;
    }
}
