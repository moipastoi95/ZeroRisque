package org.tovivi.environment.action;

import org.tovivi.environment.*;

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

    public int getNumTroops() {
        return numTroops;
    }

    public Tile getTile() {
        return tile;
    }
}
