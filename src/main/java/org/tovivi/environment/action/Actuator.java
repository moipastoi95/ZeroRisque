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
     * Perform the action of the actuator
     * @return if the action has been well performed
     */
    public boolean perform(Agent player) {
        if (runningSimulation) {
            System.out.println("Cannot perform the actuator. A simulation is in processing");
        }
        return !runningSimulation;
    }

    /**
     * Simulate the game after moves
     * @return tiles : the state of the game before the moves. Be careful, it will be modified
     */
    public boolean doSimulation(Game game) {
        if (!runningSimulation) {
            runningSimulation = true;
            return true;
        }
        System.out.println("Simulation is already processing");
        return false;

    }
    public boolean undoSimulation(Game game) {
        if (runningSimulation) {
            runningSimulation = false;
            return true;
        }
        System.out.println("Simulation already stopped");
        return false;
    }

}