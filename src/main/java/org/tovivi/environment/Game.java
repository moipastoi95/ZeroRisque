package org.tovivi.environment;


import org.tovivi.agent.Agent;
import org.tovivi.agent.RandomAgent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Game {

    final private static String[] env_data = {"continent-bonus", "continent-country", "country-neighbor"};

    private HashMap<String, Continent> continents = new HashMap<>();
    private HashMap<String, Tile> tiles = new HashMap<>();
    private ArrayList<Agent> players = new ArrayList<>();

    public Game() {
        // x players of less
        players.add(new RandomAgent("Blue", this));
        players.add(new RandomAgent("Red", this));

        TextReader tr = new TextReader() ;
        tr.readAll(this, env_data);

        // for each player --> deploy, attack, fortify
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

    public static void main(String[] args) throws IOException {
        // this main function will just start the game, with parameters (list of player, list of tiles)
        // the Game object will make players play, and restrict time for turn. It will ends by giving the winner
        Game g = new Game() ;
    }
}
