package org.tovivi.environment;


import org.tovivi.agent.Agent;
import org.tovivi.agent.RandomAgent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Game {

    // Data used to create the game based on the original map of the board game Risk
    final private static String[] env_data = {"continent-bonus", "continent-country", "country-neighbor"};

    // The number of troops at the beginning of the game at each territories
    final public static int TROOPS_FACTOR = 2;

    // HashMap who links the Continents to their names
    private HashMap<String, Continent> continents = new HashMap<>();

    // HashMap who links the Tiles to their names
    private HashMap<String, Tile> tiles = new HashMap<>();

    // ArrayList of the players
    private ArrayList<Agent> players = new ArrayList<>();

    private int playclock;

    public Game(Agent blue, Agent red, int territories, int playclock) {

        this.playclock = playclock;

        TextReader tr = new TextReader() ;
        tr.readAll(this, env_data);

        // Initialized the players
        players.add(blue); blue.setGame(this); players.add(red); red.setGame(this);
        //TODO Créer un agent Neutre
        Agent grey = new RandomAgent("grey", this); players.add(grey);

        // Randomly distribute the tiles among the players
        distributeTiles(blue, grey, red, territories);
    }

    public HashMap<String, Continent> getContinents() {
        return continents;
    }

    public void setContinents(HashMap<String, Continent> continents) {
        this.continents = continents;
    }

    public void setTiles(HashMap<String, Tile> tiles) {
        this.tiles = tiles;
    }

    public HashMap<String, Tile> getTiles() {
        return tiles;
    }

    /**
     * This function distributes the tiles of the map randomly among the players
     * @param blue : blue agent
     * @param grey  : grey //TODO Un jour il faudra peut-être l'enlever et le mettre en variable final
     * @param red  : red agent
     * @param territories : The number of territories to assigned to both red and blue players
     */
    public void distributeTiles(Agent blue, Agent grey, Agent red, int territories) {
        int rem_tiles = tiles.size(); // Number of remaining tiles to assign
        for (Tile t : tiles.values()) {
            Random rd = new Random();
            double roll = rd.nextDouble();

            //Setting probabilities to pick the next territory
            // More an agent possess territories compare to the others, less will be its chances to get the next one
            double blueLim = (double) (territories-blue.getTiles().size())/(rem_tiles) ;
            double redLim = 1 - (double) (territories-red.getTiles().size())/(rem_tiles) ;

            if (roll<blueLim) {
                t.setOccupier(blue, TROOPS_FACTOR);
            }
            else if (roll>redLim) {
                t.setOccupier(red, TROOPS_FACTOR);
            }
            else {
                t.setOccupier(grey, TROOPS_FACTOR);
            }
            rem_tiles--;
        }
    }
}
