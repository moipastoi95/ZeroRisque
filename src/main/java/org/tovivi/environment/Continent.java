package org.tovivi.environment;

import org.tovivi.agent.Agent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

public class Continent implements PropertyChangeListener {
    private String name;
    private int bonus;
    private int nbTiles;
    private Agent occupier;
    private HashMap<Agent, Integer> players;

    public int getBonus() {
        return bonus;
    }

    public void setBonus(int bonus) {
        this.bonus = bonus;
    }

    public Agent getOccupier() {
        return occupier;
    }

    public void setOccupier(Agent occupier) {
        this.occupier = occupier;
    }

    public int getNbTiles() {
        return nbTiles;
    }

    public void setNbTiles(int nbTiles) {
        this.nbTiles = nbTiles;
    }

    public HashMap<Agent, Integer> getPlayers() {
        return players;
    }

    public void setPlayers(HashMap<Agent, Integer> players) {
        this.players = players;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Put a player on a tile. Moreover, give the new player a ref to the tile and remove the ref from the other player.
     * @param name : the name of the continent
     * @param bonus : the extra troops earned by the player who totally possess the continent
     * @param nbTiles : the number of tiles in the continent
     * @param occupier : the player that possess the continent
     * @param players : the hashmap which links the players to their number of tiles in the continent
     */
    public Continent(String name, int bonus, int nbTiles) {
        this.bonus = bonus;
        this.name = name;
        this.nbTiles = nbTiles;
        this.occupier = null;
        this.players = new HashMap<Agent, Integer>();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        //New Occupier : called if a tile of the continent has been claimed by a new player
        if (evt.getPropertyName().compareTo("newOccupier")==0) {

            //The player has already possess a tile of the continent during the game
            if (players.containsKey(evt.getNewValue())) {

                //Add 1 to the number of tile possessed in the continent
                players.put((Agent) evt.getNewValue(), players.get(evt.getNewValue())+1);

                // Test if the new occupier of the tile also occupied the whole continent
                if (players.get(evt.getNewValue())==nbTiles) {
                    setOccupier((Agent) evt.getNewValue());
                }
            }
            //The player has never possessed a tile of the continent and we set its value to 1
            else {
                players.put((Agent) evt.getNewValue(), 1);
            }
            //If there was someone
            if (evt.getOldValue()!=null) {
                players.put((Agent) evt.getOldValue(), players.get(evt.getOldValue())-1);
            }
        }
    }
}
