package org.tovivi.environment;


import org.tovivi.agent.Agent;
import org.tovivi.agent.Legume;
import org.tovivi.agent.RandomAgent;
import org.tovivi.environment.action.Actions;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.*;
import java.util.concurrent.*;

public class Game {

    // Data used to create the game based on the original map of the board game Risk
    final private static String[] env_data = {"continent-bonus", "continent-country", "country-neighbor"};

    // The number of troops at the beginning of the game at each territory
    final public static int TROOPS_FACTOR = 2;

    // HashMap who links the Continents to their names
    private HashMap<String, Continent> continents = new HashMap<>();

    // HashMap who links the Tiles to their names
    private HashMap<String, Tile> tiles = new HashMap<>();

    //HashMap of the players
    private HashMap<String, Agent> players = new HashMap<>();
    private Stack<Card> theStack = new Stack<>();

    private int playclock;

    public Game(ArrayList<Agent> agents, int territories, int playclock) {

        this.playclock = playclock;
        setupElements(agents, territories);
    }

    private void play() {
        // for each player --> deploy, attack, fortify
        int index = 0;
        ArrayList<Agent> turns = new ArrayList<>(players.values());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        System.out.println("Let's begin !");
        while(turns.size() > 1) {
            Agent p = turns.get(index); // Perry the platypus
            System.out.println("Player " + p.getColor() + "'s turn");

            Future<Actions> future = executor.submit(p);
            try {
                Actions a = future.get(playclock, TimeUnit.SECONDS);

                int pTerritories = p.getTiles().size();

                // deploy
                if(a.getDeployment().isNumTroopsLegal(p)) {
                    if (a.getDeployment().perform(p)) {
                        System.out.println("    [Success] :: " + a.getDeployment().toString());
                    } else {
                        System.out.println("    [Failed:illegal move] :: " + a.getDeployment().toString());
                    }
                } else {
                    System.out.println("    [Failed:too many troops] :: " + a.getDeployment().toString());
                }


                // attack
                if (a.getFirstOffensive().perform(p)) {
                    System.out.println("    [Success] :: " + a.getFirstOffensive().toString());
                } else {
                    System.out.println("    [Failed:illegal move] :: " + a.getFirstOffensive().toString());
                }

                System.out.println("    [TOTAL TERRITORIES : " + p.getTiles().size() + "]");

                // check if one or more of the players lose
                Iterator<Agent> iterator = turns.iterator();
                while (iterator.hasNext()) {
                    Agent agent = iterator.next();
                    if (agent.getTiles().size() == 0) {
                        iterator.remove();
                        System.out.println("[DEAD] The player " + agent.getColor() + " DIED like a beetroot !");
                        // give the player the cards
                        p.getDeck().addAll(agent.getDeck());
                    }
                }

                // check if the player could retrieve cards
                if (p.getTiles().size() > pTerritories && theStack.size() > 0) {
                    p.getDeck().add(theStack.pop());
                }

            } catch (TimeoutException e) {
                future.cancel(true);
                System.out.println("    [TIMEOUT => ending " + p.getColor() + "'s turn]");
            } catch (Exception e) {
                System.out.println("    [Error] " + e);
            }

            // executor.shutdownNow();

            // next player
            index += 1;
            if (index >= turns.size()) {
                index = 0;
            }
        }
        executor.shutdownNow();
        System.out.println("[END] The winner is : " + turns.get(0).getColor() + ". Psartek !");
    }

    private void setupElements(ArrayList<Agent> agents, int territories) {
        // the map
        TextReader tr = new TextReader();
        tr.readAll(this, env_data);

        for(Agent a : agents) {
            a.setGame(this);
            players.put(a.getColor(), a);
        }

        Agent grey = new Legume("Grey", this);
        players.put(grey.getColor(), grey);

        // Randomly distribute the tiles among the players
        distributeTiles(agents.get(0), grey, agents.get(1), territories);

        // the stack
        for(CardType type : CardType.values()) {
            for(Tile tile : tiles.values()) {
                theStack.push(new Card(type, tile));
            }
        }
        // shuffle the stack
        Collections.shuffle(theStack);
    }
    
    /*
    private void configElements() {

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
    }
    */

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
            double blueLim = (double) (territories - blue.getTiles().size()) / (rem_tiles);
            double redLim = 1 - (double) (territories - red.getTiles().size()) / (rem_tiles);

            if (roll < blueLim) {
                t.setOccupier(blue, TROOPS_FACTOR);
            } else if (roll > redLim) {
                t.setOccupier(red, TROOPS_FACTOR);
            } else {
                t.setOccupier(grey, TROOPS_FACTOR);
            }
            rem_tiles--;
        }
    }

    public HashMap<String, Agent> getPlayers() {
        return players;
    }

    public static void main(String[] args) throws IOException {
        // this main function will just start the game, with parameters (list of player, list of tiles)
        // the Game object will make players play, and restrict time for turn. It will end by giving the winner
    }
}
