package org.tovivi.environment.action;

import org.tovivi.agent.Agent;
import org.tovivi.environment.*;
import org.tovivi.environment.action.exceptions.IllegalActionException;
import org.tovivi.environment.action.exceptions.SimulationRunningException;

import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.min;

public class Attack extends Offensive{
    private Offensive onSucceed;
    private Offensive onFailed;
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
        super(fromTile, toTile, movedTroops);
        this.onSucceed = onSucceed;
        this.onFailed = onFailed;
    }

    public Offensive getOnSucceed() {
        return onSucceed;
    }

    public Offensive getOnFailed() {
        return onFailed;
    }

    @Override
    public String toString() {
        return "[Attack:" + getFromTile().getName() + " -" + getNumTroops() + "-> " + getToTile().getName() + "]";
    }

    @Override
    public boolean isMoveLegal(Agent player) {

        // check game state consistency
        if (!getFromTile().getOccupier().equals(player) || getToTile().getOccupier().equals(player) || getFromTile().getNumTroops()<=1) {
            return false;
        }

        // make sure it cannot move all the troops
        if (getNumTroops() >= getFromTile().getNumTroops()) {
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
        while(getToTile().getNumTroops() != 0 && getFromTile().getNumTroops() != 1) {
            // throw of the dices
            // dices of the player and the opponent
            ArrayList<Integer> dicesP = new ArrayList<>();
            ArrayList<Integer> dicesO = new ArrayList<>();
            for(int i=0; i<min(getFromTile().getNumTroops()-1, 3); i++) {
                dicesP.add((int)(Math.random() * 6));
            }
            for(int i=0; i<min(getToTile().getNumTroops(), 2); i++) {
                dicesO.add((int)(Math.random() * 6));
            }

            //order dices
            Collections.sort(dicesP, Collections.reverseOrder());
            Collections.sort(dicesO, Collections.reverseOrder());

            for(int i=0; i<min(dicesP.size(), dicesO.size()); i++) {
                // succeed attack
                if (dicesP.get(i) > dicesO.get(i)) {
                    getToTile().setNumTroops(getToTile().getNumTroops()-1);
                } else { // failed
                    getFromTile().setNumTroops(getFromTile().getNumTroops()-1);
                }
            }
        }

        // fail the defence
        if (getToTile().getNumTroops() == 0) {
            int troopsReallyMoved = min(getNumTroops(), getFromTile().getNumTroops()-1);
            getFromTile().setNumTroops(getFromTile().getNumTroops()-troopsReallyMoved);
            getToTile().setOccupier(player, troopsReallyMoved);
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
        copyToTile = new Tile(getToTile().getName(), getToTile().getContinent());
        copyToTile.setOccupier(getToTile().getOccupier(), getToTile().getNumTroops());

        getToTile().setOccupier(getFromTile().getOccupier(), getNumTroops());
        getFromTile().setNumTroops(getFromTile().getNumTroops()-getNumTroops());

        return true;
    }

    @Override
    public boolean undoSimulation() {
        if (!super.undoSimulation()) {
            return false;
        }

        getToTile().setOccupier(copyToTile.getOccupier(), copyToTile.getNumTroops());
        getFromTile().setNumTroops(getFromTile().getNumTroops()-getNumTroops());

        return true;
    }
}
