package org.tovivi.environment.action;

import org.tovivi.agent.Agent;
import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Actuator {

    private boolean runningSimulation = false;
    /**
     * Check if the move is allowed
     * @param player the player who plays the move
     * @return the player is able to do this move
     */
    abstract boolean isMoveLegal(Agent player);

    /**
     * Perform the action of the actuator. Should verify if the move is legal (to implement in the function).
     * @return if the action has been well performed (without bugs).
     * For instance, if an attack has failed, it will return true, because it was a legal move.
     */
    public boolean perform(Agent player) {
        if (runningSimulation) {
            System.out.println("Cannot perform the actuator. A simulation is in processing");
        }
        return !runningSimulation;
    }

    /**
     * Simulate the game after moves. Won't check if the move is legal
     * @return tiles : the state of the game before the moves. Be careful, it will be modified
     */
    public boolean doSimulation() {
        if (!runningSimulation) {
            runningSimulation = true;
            return true;
        }
        System.out.println("Simulation is already processing");
        return false;

    }
    public boolean undoSimulation() {
        if (runningSimulation) {
            runningSimulation = false;
            return true;
        }
        System.out.println("Simulation already stopped");
        return false;
    }

}
