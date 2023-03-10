package org.tovivi.environment;


import org.tovivi.agent.Agent;
import org.tovivi.agent.RandomAgent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Game {

    final private static String[] env_data = {"continent-bonus", "continent-country", "country-neighbor"};
    final public static int TROOPS_FACTOR = 2;

    private HashMap<String, Continent> continents = new HashMap<>();
    private HashMap<String, Tile> tiles = new HashMap<>();
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

        // Random tiles distribution
        int rem_tiles = tiles.size(); // Number of remaining tiles to assign
        for (Tile t : tiles.values()) {
            Random rd = new Random();
            double roll = rd.nextDouble();
            double blueLim = (double) (territories-blue.getTiles().size())/(rem_tiles) ;
            double redLim = 1 - (double) (territories-red.getTiles().size())/(rem_tiles) ;

            System.out.print(t.getName() + " : ");

            if (roll<blueLim) {
                t.setOccupier(blue, TROOPS_FACTOR);
                System.out.println("BLUE");
            }
            else if (roll>redLim) {
                t.setOccupier(red, TROOPS_FACTOR);
                System.out.println("RED");
            }
            else {
                t.setOccupier(grey, TROOPS_FACTOR);
                System.out.println("GREY");
            }
            rem_tiles--;
        }
        System.out.println(blue.getTiles().size() + " " + red.getTiles().size());
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
    }
}
