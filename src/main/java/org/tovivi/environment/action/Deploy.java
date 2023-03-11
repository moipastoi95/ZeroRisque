package org.tovivi.environment.action;

import org.tovivi.environment.*;

import java.util.ArrayList;

public class Deploy {
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

    public Tile getTile() {
        return tile;
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
}
