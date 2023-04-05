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
    private HashMap<String, Integer> players;

    public Continent(Continent continent) {
        this.name = continent.getName();
        this.bonus = continent.getBonus();
        this.nbTiles = continent.getNbTiles();
        this.occupier = continent.getOccupier(); //Not a deepCopy |
        this.players = continent.getPlayers();
    }

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

    public HashMap<String, Integer> getPlayers() {
        return players;
    }

    public void setPlayers(HashMap<String, Integer> players) {
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
     */
    public Continent(String name, int bonus, int nbTiles) {
        this.bonus = bonus;
        this.name = name;
        this.nbTiles = nbTiles;
        this.occupier = null;
        this.players = new HashMap<String, Integer>();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        //New Occupier : called if a tile of the continent has been claimed by a new player
        if (evt.getPropertyName().compareTo("newOccupier")==0) {
            //The player has already possess a tile of the continent during the game
            if (players.containsKey(((Agent) evt.getNewValue()).getColor())) {

                //Add 1 to the number of tile possessed in the continent
                players.put(((Agent) evt.getNewValue()).getColor(), players.get(((Agent) evt.getNewValue()).getColor())+1);

                // Test if the new occupier of the tile also occupied the whole continent
                if (players.get(((Agent) evt.getNewValue()).getColor())==nbTiles) {
                    setOccupier((Agent) evt.getNewValue());
                }
            }
            //The player has never possessed a tile of the continent and we set its value to 1
            else {
                players.put(((Agent) evt.getNewValue()).getColor(), 1);
            }
            //If there was someone on the tile
            if (evt.getOldValue()!=null) {
                players.put(((Agent) evt.getOldValue()).getColor(), players.get(((Agent) evt.getOldValue()).getColor())-1);
                if (getOccupier()!=null && getOccupier().getColor().compareTo(((Agent) evt.getOldValue()).getColor())==0) {
                    setOccupier(null);
                }
            }
        }
    }
}
