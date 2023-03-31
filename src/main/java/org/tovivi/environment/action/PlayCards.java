package org.tovivi.environment.action;

import org.tovivi.agent.Agent;
import org.tovivi.environment.Card;
import org.tovivi.environment.Tile;
import org.tovivi.environment.action.exceptions.IllegalActionException;
import org.tovivi.environment.action.exceptions.SimulationRunningException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

public class PlayCards extends Deployment {
    private ArrayList<Card> cards;

    private HashMap<String, Integer> bonuses;

    private Agent player;

    /**
     * Play cards to increase the num of troops available
     * @param cards Array list of 3 cards
     */
    public PlayCards(ArrayList<Card> cards, Agent player) {
        this.player = player;
        this.cards = cards;
        this.bonuses = Card.value(cards, player);
    }

    @Override
    public boolean isMoveLegal(Agent player) {
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
        player.removeAllCards(cards);
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
        String str = "[CardPlaying:+" + Card.countOnlyCombo(cards, player) + "]";
        if (bonuses.size()>1) {
            str += "[Extra:";
            for (String s : bonuses.keySet()) {
                if (s.compareTo("Combo")!=0) {
                    str += " (" + s + " -> +" + bonuses.get(s) + ")";
                }
            }
            str += "]";
        }
        return str;
    }

    @Override
    public boolean stopDeploy() {
        return false;
    }

    public Agent getPlayer() {
        return player;
    }

    public void setPlayer(Agent player) {
        this.player = player;
    }

    @Override
    public ArrayList<Tile> getTiles() {
        return null;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    /**
     * reduce the number of troops
     * @return
     */
    @Override
    public int getNumTroops() {
        return -Card.count(cards, player);
    }

    @Override
    public int getFirstNumTroops() {
        return getNumTroops();
    }

    @Override
    public boolean isNumTroopsLegal(Agent player) {
        return true;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public ArrayList<Deployment> autoDeploy() {
        ArrayList<Deployment> res = new ArrayList<>();
        for (String s : bonuses.keySet()) {
            try {
                if (s.compareTo("Combo")!=0) {
                    res.add(new Deploy(bonuses.get(s), player.getGame().getTiles().get(s)));
                }
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return res;
    }

}
