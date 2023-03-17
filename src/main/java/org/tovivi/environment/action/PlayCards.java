package org.tovivi.environment.action;

import org.tovivi.agent.Agent;
import org.tovivi.environment.Card;
import org.tovivi.environment.Tile;
import org.tovivi.environment.action.exceptions.IllegalActionException;
import org.tovivi.environment.action.exceptions.SimulationRunningException;

import java.util.ArrayList;

public class PlayCards extends Deployment {
    private ArrayList<Card> cards;
    private Agent player;

    /**
     * Play cards to increase the num of troops available
     * @param cards Array list of 3 cards
     */
    public PlayCards(ArrayList<Card> cards, Agent player) {
        this.player = player;
        this.cards = cards;
    }

    @Override
    boolean isMoveLegal(Agent player) {
        return this.player.equals(player) && player.getDeck().containsAll(cards) && cards.size() == 3;
    }

    @Override
    public Actuator perform(Agent player) throws SimulationRunningException, IllegalActionException {
        if (!super.isSimulating()) {
            throw new SimulationRunningException();
        }

        if (!isMoveLegal(player)) {
            throw new IllegalActionException();
        }

        player.getDeck().removeAll(cards);
        return null;
    }

    /**
     * Nothing to change
     * @return
     */
    @Override
    public boolean doSimulation() {
        if (!super.doSimulation()) {
            return false;
        }
        return true;
    }

    /**
     * Nothing to change
     * @return
     */
    @Override
    public boolean undoSimulation() {
        if (!super.undoSimulation()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[Deploy:+" + Card.value(cards, player) + "]";
    }

    @Override
    public boolean stopDeploy() {
        return false;
    }

    /**
     * reduce the number of troops
     * @return
     */
    @Override
    public int getNumTroops() {

        System.out.println(Card.value(cards, player));return -Card.value(cards, player);
    }

    @Override
    public boolean isNumTroopsLegal(Agent player) {
        return true;
    }

    @Override
    public ArrayList<Tile> getTiles() {
        return new ArrayList<Tile>();
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

}
