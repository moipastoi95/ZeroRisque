package org.tovivi.environment.action;

import org.tovivi.environment.Tile;

public abstract class Offensive extends Actuator {
    protected Tile fromTile;
    protected Tile toTile;
    protected int numTroops;

    public Offensive(Tile fromTile, Tile toTile, int numTroops) {
        this.fromTile = fromTile;
        this.toTile = toTile;
        this.numTroops = numTroops;
    }

    public Tile getFromTile() {
        return fromTile;
    }

    public void setFromTile(Tile fromTile) {
        this.fromTile = fromTile;
    }

    public void setToTile(Tile toTile) {
        this.toTile = toTile;
    }

    public Tile getToTile() {
        return toTile;
    }

    public int getNumTroops() {return numTroops;}
}
