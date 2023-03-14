package org.tovivi.environment;

import org.tovivi.agent.Agent;

import java.util.ArrayList;

public class Card {
    private CardType type;
    private Tile bonusTile;

    /**
     * Main constructor
     * @param type type of the card
     * @param bonusTile +2 bonus if the player own the tile
     */
    public Card(CardType type, Tile bonusTile) {
        this.type = type;
        this.bonusTile = bonusTile;
    }

    public Card(Card c) {
        this.type = c.getType();
        this.bonusTile = new Tile(bonusTile);
    }

    public CardType getType() {
        return type;
    }

    public Tile getBonusTile() {
        return bonusTile;
    }

    /**
     * Give the value of the combination of 3 cards that a player could play.
     * @param cards Array list of 3 cards
     * @param player The player who want to play the cards
     * @return the bonus value of the combination
     */
    public static int value(ArrayList<Card> cards, Agent player) {
        if (cards.size() != 3) {
            return 0;
        }
        Card c1 = cards.get(0);
        Card c2 = cards.get(1);
        Card c3 = cards.get(2);
        int bonus = 0;
        // 3 same cards
        if (c1.getType() == c2.getType() && c2.getType() == c3.getType()) {
            switch (c1.getType()) {
                case INFANTRY:
                    bonus += 4;
                    break;
                case CAVALRY:
                    bonus += 6;
                    break;
                case ARTILLERY:
                    bonus += 8;
                    break;
            }

        } else if (c1.getType() != c2.getType() && c2.getType() != c3.getType() && c1.getType() != c3.getType()) {
            bonus += 10;
        }

        // adding bonus if the player own a specific territory
        if (player.getTiles().contains(c1.getBonusTile())) {
            bonus += 2;
        }
        if (player.getTiles().contains(c2.getBonusTile())) {
            bonus += 2;
        }
        if (player.getTiles().contains(c3.getBonusTile())) {
            bonus += 2;
        }
        return bonus;
    }

    /**
     * Choose the best cards
     * @param deck the deck the play owns
     * @param player the player
     * @return an array list of 3 cards
     */
    public static ArrayList<Card> chooseCards(ArrayList<Card> deck, Agent player) {
        if (deck.size() < 3) {
            return new ArrayList<Card>();
        }

        int maxI = 0, maxJ=1, maxK=2, maxVal=0;
        for(int i=0; i<=deck.size()-3; i++) {
            for(int j=i+1; j<=deck.size()-2; j++) {
                for(int k=j+1; k<=deck.size()-1; k++) {
                    ArrayList<Card> cards = new ArrayList<>();
                    cards.add(deck.get(i));
                    cards.add(deck.get(j));
                    cards.add(deck.get(k));
                    int processVal = Card.value(cards, player);
                    if (processVal > maxVal) {
                        maxI = i;
                        maxJ = j;
                        maxK = k;
                        maxVal = processVal;
                    }
                }
            }
        }
        ArrayList<Card> cards = new ArrayList<>();
        cards.add(deck.get(maxI));
        cards.add(deck.get(maxJ));
        cards.add(deck.get(maxK));
        return cards;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Card) {
            Card c = (Card) obj;
            return c.getType() == this.type && c.getBonusTile().equals(this.bonusTile);
        }
        return false;
    }

    @Override
    public String toString() {
        return "[CARD:" + this.type + " - " + this.bonusTile.getName() + "]";
    }
}
