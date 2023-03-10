package org.tovivi.environment.action;

import org.tovivi.environment.*;

public class Fortify extends Fortification {
    private Tile fromTile;
    private Tile toTile;
    private int numTroops;

    /**
     * Fortify a tile with troops from another tile
     * @param fromTile the tile where to get troops
     * @param toTile the tile where to put troops
     * @param numTroops the number of troops to move
     */
    public Fortify(Tile fromTile, Tile toTile, int numTroops) {
        this.fromTile = fromTile;
        this.toTile = toTile;
        this.numTroops = numTroops;
    }

    public Tile getFromTile() {
        return fromTile;
    }

    public Tile getToTile() {
        return toTile;
    }

    public int getNumTroops() {
        return numTroops;
    }
}
