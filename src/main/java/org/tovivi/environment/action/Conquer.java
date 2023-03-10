package org.tovivi.environment.action;

import org.tovivi.environment.*;
public class Conquer extends Attack {
    private Attack onSucceed;
    private Attack onFailed;
    private Tile fromTile;
    private Tile toTile;
    private int movedTroops;

    /**
     * Conquer a tile
     * @param fromTile the tile containing the army used
     * @param toTile the tile to invade
     * @param movedTroops the number of troops moved to the eventual new territory
     * @param onSucceed the next action to do if the attack has succeeded
     * @param onFailed the nex action to do if the attack has failed
     */
    public Conquer(Tile fromTile, Tile toTile, int movedTroops, Attack onSucceed, Attack onFailed) {
        this.fromTile = fromTile;
        this.toTile = toTile;
        this.movedTroops = movedTroops;
        this.onSucceed = onSucceed;
        this.onFailed = onFailed;
    }

    public Attack getOnSucceed() {
        return onSucceed;
    }

    public Attack getOnFailed() {
        return onFailed;
    }

    public Tile getFromTile() {
        return fromTile;
    }

    public Tile getToTile() {
        return toTile;
    }

    public int getMovedTroops() {
        return movedTroops;
    }
}
