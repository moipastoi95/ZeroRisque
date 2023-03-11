package org.tovivi.environment.action;

import org.tovivi.agent.Agent;
import org.tovivi.environment.Card;
import org.tovivi.environment.Tile;

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
    public boolean perform(Agent player) {
        if (!super.perform(player)) {
            return false;
        }

        if (!isMoveLegal(player)) {
            return false;
        }

        player.getDeck().removeAll(cards);
        return true;
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
        if (!super.doSimulation()) {
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
        return -Card.value(cards, player);
    }

    @Override
    public ArrayList<Tile> getTiles() {
        return new ArrayList<Tile>();
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

}
