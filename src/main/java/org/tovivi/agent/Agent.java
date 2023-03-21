package org.tovivi.agent;

import org.tovivi.environment.*;
import org.tovivi.environment.action.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Callable;

public abstract class Agent implements Callable<Actions> {

    private String color;
    private ArrayList<Tile> tiles;
    private Game game;
    private ArrayList<Card> deck;
    private LinkedList<LinkedList<Double>> proba = new LinkedList<>();

    /**
     * @param color : String of the color
     * @param game : ref to the game object
     * */
    public Agent(String color, Game game) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        this.color = color;
        this.tiles = new ArrayList<>();
        this.deck = new ArrayList<>();
        this.game = game;
        TextReader tr = new TextReader();
        this.proba = tr.readProba(Objects.requireNonNull(TextReader.class.getResource("proba.txt")));
    }
    public Agent(String color) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        this(color, null);
    }

    public Agent(Agent agent){
        this.color = agent.getColor();
        this.tiles = new ArrayList<>();
        this.deck = new ArrayList<>();
        for(Card c: agent.getDeck()){
            this.deck.add(new Card(c));
        }
    }

    /**
     * Add a tile to the list of the player's tiles
     * @param tile : the new tile
     * @author Vincent
     */
    public void addTile(Tile tile) {
        tiles.add(tile);
    }

    /**
     * Remove a tile from the list of the player's tiles
     * @param tile : the tile to remove
     */
    public void removeTile(Tile tile) {
        tiles.remove(tile);
    }

    /**
     * Remove a tile from the list of the player's tiles
     * @param  : id of the tile to remove
     */
    /*
    public void removeTile(int idTile) {
        boolean found = false;
        Iterator<Tile> i = this.tiles.iterator();
        while (!found && i.hasNext()) {
            if (i.next().getId() == idTile) {
                i.remove();
            }
        }
    }
    */
    public Game getGame() throws IOException, URISyntaxException {
        return game;
    }
    /**Return the probability of winning for a fight of i attackers against j defenders
     * @return A double giving the winninig probability
     * */
    public double getProba(int i, int j){
        if (i>=50 || j>=50) {
            if (i>=j) {
                j = (j/i)*50;
                i = 50;
            }
            else {
                i = (i/j)*50;
                j = 50;
            }
        }
        return this.proba.get(i).get(j);
    }

    public void setGame(Game game) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        TextReader tr = new TextReader();
        this.proba = tr.readProba(Objects.requireNonNull(TextReader.class.getResource("proba.txt")));
        this.game = game;
    }

    /**
     * Get a list of tiles owned by the player
     * @return the tiles
     */
    public ArrayList<Tile> getTiles() {
        return this.tiles;
    }

    /**
     * Get the color of the player
     * @return the color (String)
     */
    public String getColor() {
        return this.color;
    }

    public ArrayList<Card> getDeck() {
        return deck;
    }

    /**
     * Get the number of troops the player is able to deploy at each turn
     * @return the number of troops
     */
    public final int getNumDeploy() {
        int total = 0;
        if (getTiles().size() <= 3) {
            total = 3;
        } else {
            total = (getTiles().size()/3);

            // adding bonus for continents
            // retrieve continents where the player owns territories
            HashMap<String, Continent> continents = new HashMap<>();
            for (Tile t : getTiles()) {
                if (!continents.containsKey(t.getContinent().getName())) {
                    continents.put(t.getContinent().getName(), t.getContinent());
                }
            }
            for(Continent c : continents.values()) {
                if(c.getOccupier() != null && c.getOccupier().equals(this)) {
                    total += c.getBonus();
                }
            }
        }
        return total;
    }

    public abstract Actions action() throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException;

    public abstract Deployment getNextDeploy() throws IOException, URISyntaxException;
    public abstract Attack getNextAttack();
    public abstract Fortify getFortify();

    @Override
    public String toString() {
        return "Player " + color;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Agent) {
            Agent p = (Agent) obj;
            return p.getColor().equals(this.getColor());
        }
        return false;
    }

    @Override
    public Actions call() throws Exception {
        return action();
    }
}
