package org.tovivi.environment.action;

import org.tovivi.agent.Agent;
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
        if (onLiveAction) {
            if(firstOffensive != null) firstOffensive = (Offensive) firstOffensive.perform(player);
            System.out.println(firstOffensive);
            return firstOffensive != null;
        }
        firstOffensive = (Offensive) firstOffensive.perform(player);
        return firstOffensive != null && firstOffensive instanceof Attack;
    }

    public boolean performFortify(Agent player) throws IllegalActionException, SimulationRunningException {
        if (onLiveAction) {
            firstOffensive = player.getFortify();
            if(firstOffensive != null) firstOffensive.perform(player);
            return firstOffensive != null;
        }
        firstOffensive = (Offensive) firstOffensive.perform(player);
        return firstOffensive != null;
    }

    public Deployment getDeployment(Agent player) throws IOException, URISyntaxException, IllegalActionException, SimulationRunningException {
        if (onLiveAction) {
            if(deployment == null) deployment = player.getNextDeploy();
        }
        return deployment;
    }

    public Offensive getFirstOffensive(Agent player) {
        if(onLiveAction) {
            if(firstOffensive == null) firstOffensive = player.getNextAttack();
        }
        return firstOffensive;
    }
}
