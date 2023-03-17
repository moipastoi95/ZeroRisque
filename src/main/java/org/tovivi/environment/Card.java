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
        int bonus = 0;
        if (cards.size() != 3) {
            return 0;
        }
        int infantry=0 , cavalry=0 , artillery=0, joker=0;
        for (Card c : cards) {
            switch (c.getType()) {
                case INFANTRY:
                    infantry++;
                    break;
                case CAVALRY:
                    cavalry++;
                    break;
                case ARTILLERY:
                    artillery++;
                    break;
                default:
                    joker++;
            }
        }
        int[] typeCount = {infantry, cavalry, artillery};
        if (joker==1) {
            int i = 2;
            do {
                if (i<2) {
                    typeCount[i+1]--;
                }
                typeCount[i]++;
                i--;
            } while (combo(typeCount)==0 && i>=0);
        }
        bonus = combo(typeCount);
        if (joker==2) {
            bonus = 10;
        }

        // adding bonus if the player own a specific territory
        if (bonus>0) {
            for (Card c : cards) {
                if (c.getType()!=CardType.JOKER &&  player.getTiles().contains(c.getBonusTile())) {
                        bonus+=2;
                }
            }
        }
        return bonus;
    }

    /**
     * Suppose that the sum of the 3 int has a value of 3
     * @param typeCount array of the type counts
     * @return the value of the combo
     */
    public static int combo(int[] typeCount) {
        int infantry = typeCount[0]; int cavalry = typeCount[1]; int artillery = typeCount[2];
        if (infantry==2 || cavalry == 2 || artillery==2) {
            return 0;
        }
        else if (infantry==1 && cavalry==1) {
            return 10;
        }
        else {
            if (artillery==3) {
                return 8;
            }
            else if (cavalry==3){
                return 6;
            }
            return 4;
        }
    }

    /**
     * Choose the best cards
     * @param deck the deck the play owns
     * @param player the player
     * @return an array list of 3 cards
     */
    public static ArrayList<Card> chooseCards(ArrayList<Card> deck, Agent player) {
        if (deck.size() < 3) {
            return new ArrayList<>();
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
            if (c.getType()==CardType.JOKER) {
                return true;
            }
            return c.getType() == this.type && c.getBonusTile().equals(this.bonusTile);
        }
        return false;
    }

    @Override
    public String toString() {
        String str = type.toString();
        if (bonusTile!=null) {
            str += " - " + bonusTile.getName();
        }
        return str;
    }
}
