package org.tovivi.environment.action;

import org.tovivi.agent.Agent;
import org.tovivi.environment.*;
import org.tovivi.environment.action.exceptions.IllegalActionException;
import org.tovivi.environment.action.exceptions.SimulationRunningException;

import static java.lang.Math.min;

public class Attack extends Offensive{
    private Offensive onSucceed;
    private Offensive onFailed;
    private Tile fromTile;
    private Tile toTile;
    private int movedTroops;
    private Tile copyToTile;

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

    @Override
    public String toString() {
        return "[Attack:" + fromTile.getName() + " -" + movedTroops + "-> " + toTile.getName() + "]";
    }

    @Override
    public boolean isMoveLegal(Agent player) {

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
    public Actuator perform(Agent player) throws SimulationRunningException, IllegalActionException {
        if (!super.isSimulating()) {
            throw new SimulationRunningException();
        }

        if (!isMoveLegal(player)) {
            throw new IllegalActionException();
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
            System.out.println(troopsReallyMoved);
            toTile.setOccupier(player, troopsReallyMoved);
            fromTile.setNumTroops(fromTile.getNumTroops()-troopsReallyMoved);
            return onSucceed;
        }
        else {
            return onFailed;
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
