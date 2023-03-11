package org.tovivi.environment;


import org.tovivi.agent.Agent;
import org.tovivi.agent.Legume;
import org.tovivi.agent.RandomAgent;
import org.tovivi.environment.action.Actions;

import javax.swing.text.PlainDocument;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Game {

    final private static String[] env_data = {"continent-bonus", "continent-country", "country-neighbor"};

    private HashMap<String, Continent> continents = new HashMap<>();
    private HashMap<String, Tile> tiles = new HashMap<>();
    private HashMap<String, Agent> players = new HashMap<>();

    public Game() {
        // number of troops per players
        int troops = 35;
        // x players of less
        players.put("Blue", new RandomAgent("Blue", this));
        players.put("Red", new RandomAgent("Red", this));
        players.put("Grey", new Legume("Grey", this));

        TextReader tr = new TextReader();
        tr.readAll(this, env_data);

        if (troops < (int)(tiles.size()/players.size())) {
            System.out.println("Not enough troops");
            System.exit(-1);
        }

        // assign territories to players
        // build a list made with tiles
        ArrayList<Tile> tilesLeft = new ArrayList<>(tiles.values());
        for(Agent a : players.values()) {
            int localTroops = troops;
            // assigning territories
            for(int i=1; i <= (int)(tiles.size()/players.size()); i++) {
                int index = (int)(Math.random() * tilesLeft.size());
                tilesLeft.get(index).setOccupier(a, 1);
                localTroops -= 1;
                tilesLeft.remove(index);
            }
            // adding more troops
            for(int i=1; i<=localTroops; i++) {
                Tile tile = a.getTiles().get((int)(Math.random() * a.getTiles().size()));
                tile.setNumTroops(tile.getNumTroops()+1);
            }
        }

        // for each player --> deploy, attack, fortify
        int index = 0;
        ArrayList<Agent> turns = new ArrayList<>(players.values());
        while(turns.size() > 1) {
            Agent p = turns.get(index);
            Actions a = p.action();

            System.out.println("Player " + p.getColor() + "'s turn");

            // deploy
            if (a.getDeployment().perform(p)) {
                System.out.println("    [Success] :: " + a.getDeployment().toString());
            } else {
                System.out.println("    [Failed] :: " + a.getDeployment().toString());
            }

            // attack
            if (a.getFirstOffensive().perform(p)) {
                System.out.println("    [Success] :: " + a.getFirstOffensive().toString());
            } else {
                System.out.println("    [Failed] :: " + a.getFirstOffensive().toString());
            }

            System.out.println("    [TOTAL TERRITORIES : " + p.getTiles().size() + "]");

            // check if players lose
            Iterator<Agent> iterator = turns.iterator();
            while (iterator.hasNext()) {
                Agent agent = iterator.next();
                if (agent.getTiles().size() == 0) {
                    iterator.remove();
                    System.out.println("[DEAD] The player " + agent.getColor() + " is DEAD like a beetroot !");
                }
            }

            // next player
            index += 1;
            if (index >= turns.size()) {
                index = 0;
            }
        }
        System.out.println("[END] The winner is : " + turns.get(0).getColor() + ". Psartek !");
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

    public HashMap<String, Agent> getPlayers() {
        return players;
    }

    public static void main(String[] args) throws IOException {
        // this main function will just start the game, with parameters (list of player, list of tiles)
        // the Game object will make players play, and restrict time for turn. It will ends by giving the winner
        Game g = new Game() ;
    }
}
