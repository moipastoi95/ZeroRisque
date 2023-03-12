package org.tovivi.environment;


import org.tovivi.agent.Agent;
import org.tovivi.agent.Legume;
import org.tovivi.agent.RandomAgent;
import org.tovivi.environment.action.Actions;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Game {

    final private static String[] env_data = {"continent-bonus", "continent-country", "country-neighbor"};
    // number of troops per players
    final private int troops = 35;
    //  max time per turn
    final private int timeout = 6;
    private HashMap<String, Continent> continents = new HashMap<>();
    private HashMap<String, Tile> tiles = new HashMap<>();
    private HashMap<String, Agent> players = new HashMap<>();
    private Stack<Card> theStack = new Stack<>();

    public Game() {

        setupElements();
        configElements();

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
                Actions a = future.get(timeout, TimeUnit.SECONDS);

                int territories = p.getTiles().size();

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
                if (p.getTiles().size() > territories && theStack.size() > 0) {
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

    private void setupElements() {
        // the map
        TextReader tr = new TextReader();
        tr.readAll(this, env_data);

        // x players of less
        players.put("Blue", new RandomAgent("Blue", this));
        players.put("Red", new RandomAgent("Red", this));
        players.put("Grey", new Legume("Grey", this));

        // the stack
        for(CardType type : CardType.values()) {
            for(Tile tile : tiles.values()) {
                theStack.push(new Card(type, tile));
            }
        }
        // shuffle the stack
        Collections.shuffle(theStack);
    }

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
        // the Game object will make players play, and restrict time for turn. It will end by giving the winner
        new Game() ;
    }
}
