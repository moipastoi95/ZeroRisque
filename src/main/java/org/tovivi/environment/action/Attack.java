package org.tovivi.environment.action;

import org.tovivi.agent.Agent;
import org.tovivi.environment.*;

import java.util.ArrayList;
import java.util.HashMap;

import static java.lang.Math.min;

public class Attack extends Offensive{
    private Offensive onSucceed;
    private Offensive onFailed;
    private Tile fromTile;
    private Tile toTile;
    private int movedTroops;
    private Tile copyToTile;

    /**
     * Create a stop attack node. Goes to the fortification part
     */
    public Attack() {
        this(null, null, 0, null, null);
    }

    /**
     * Conquer a tile
     * @param fromTile the tile containing the army used
     * @param toTile the tile to invade
     * @param movedTroops the number of troops moved to the eventual new territory
     * @param onSucceed the next action to do if the attack has succeeded
     * @param onFailed the nex action to do if the attack has failed
     */
    public Attack(Tile fromTile, Tile toTile, int movedTroops, Offensive onSucceed, Offensive onFailed) {
        this.fromTile = fromTile;
        this.toTile = toTile;
        this.movedTroops = movedTroops;
        this.onSucceed = onSucceed;
        this.onFailed = onFailed;
    }

    public Offensive getOnSucceed() {
        return onSucceed;
    }

    public Offensive getOnFailed() {
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

    /**
     * Is it a stop attack node
     * @return this object is a stop attack node
     */
    public boolean stopAttack() {
        return fromTile == null &&
                toTile == null &&
                movedTroops == 0 &&
                onSucceed == null &&
                onFailed == null;
    }

    @Override
    public String toString() {
        if (stopAttack()) {
            return "";
        }
        return "[Attack:" + fromTile.getName() + " -" + movedTroops + "-> " + toTile.getName() + "]" + onSucceed;
    }

    @Override
    public boolean isMoveLegal(Agent player) {
        if (stopAttack()) {
            return true;
        }

        // check game state consistency
        if (!fromTile.getOccupier().equals(player) || toTile.getOccupier().equals(player) || fromTile.getNumTroops()<=1) {
            return false;
        }

        // make sure it cannot move all the troops
        if (movedTroops >= fromTile.getNumTroops()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean perform(Agent player) {
        if (!super.perform(player)) {
            return false;
        }

        if (stopAttack()) {
            return true;
        }

        if (!isMoveLegal(player)) {
            return false;
        }

        // fight
        while(toTile.getNumTroops() != 0 && fromTile.getNumTroops() != 1) {
            // throw of the dice
            int diceP = (int)(Math.random() * 6); // dice of the player
            int diceO = (int)(Math.random() * 6); // dice of the opponent

            // succeed attack
            if (diceP > diceO) {
                toTile.setNumTroops(toTile.getNumTroops()-1);
            } else { // failed
                fromTile.setNumTroops(fromTile.getNumTroops()-1);
            }
        }

        // fail the defence
        if (toTile.getNumTroops() == 0) {
            int troopsReallyMoved = min(movedTroops, fromTile.getNumTroops()-1);
            toTile.setOccupier(player, troopsReallyMoved);
            fromTile.setNumTroops(fromTile.getNumTroops()-troopsReallyMoved);
            return onSucceed.perform(player);
        }
        else {
            return onFailed.perform(player);
        }
    }

    @Override
    public boolean doSimulation() {
        if (!super.doSimulation()) {
            return false;
        }

        // copy former value of toTile
        copyToTile = new Tile(toTile.getName(), toTile.getContinent());
        copyToTile.setOccupier(toTile.getOccupier(), toTile.getNumTroops());

        toTile.setOccupier(fromTile.getOccupier(), movedTroops);
        fromTile.setNumTroops(fromTile.getNumTroops()-movedTroops);

        return true;
    }

    @Override
    public boolean undoSimulation() {
        if (!super.undoSimulation()) {
            return false;
        }

        toTile.setOccupier(copyToTile.getOccupier(), copyToTile.getNumTroops());
        fromTile.setNumTroops(fromTile.getNumTroops()-movedTroops);

        return true;
    }
}
