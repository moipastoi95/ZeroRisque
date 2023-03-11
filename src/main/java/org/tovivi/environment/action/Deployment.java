package org.tovivi.environment.action;

import org.tovivi.agent.Agent;
import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;

import java.util.ArrayList;

public abstract class Deployment extends Actuator {

    /**
     * No deployment
     * @return no there isn't
     */
    public abstract boolean stopDeploy();

    /**
     * Get the tile of the deployment
     * @return an integer
     */
    public abstract ArrayList<Tile> getTiles();

    /**
     * get the number of troop involve in the deployment
     * @return an integer
     */
    public abstract int getNumTroops();
}
