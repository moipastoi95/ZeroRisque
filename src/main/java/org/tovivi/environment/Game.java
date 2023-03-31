package org.tovivi.environment;

import org.tovivi.agent.Agent;
import org.tovivi.agent.AgentMCNN;
import org.tovivi.agent.AgentMonteCarlo;
import org.tovivi.agent.Legume;
import org.tovivi.environment.action.*;
import org.tovivi.environment.action.exceptions.IllegalActionException;
import org.tovivi.environment.action.exceptions.SimulationRunningException;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.*;
import java.util.concurrent.*;

public class Game {

    // Data used to create the game based on the original map of the board game Risk
    final private static String[] env_data = {"continent-bonus", "continent-country", "country-neighbor", "country-card"};

    // The number of troops at the beginning of the game at each territory
    final public static int TROOPS_FACTOR = 2;

    private int gameSpeed;

    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    // HashMap who links the Continents to their names
    private HashMap<String, Continent> continents = new HashMap<>();

    // HashMap who links the Tiles to their names
    private HashMap<String, Tile> tiles = new HashMap<>();

    //HashMap of the players
    private HashMap<String, Agent> players = new HashMap<>();
    private Stack<Card> theStack = new Stack<>();
    private Stack<Card> theDiscardPile = new Stack<>();

    private int playclock;

    public Game(ArrayList<Agent> agents, int territories, int playclock, int gameSpeed) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {

        this.playclock = playclock;
        setGameSpeed(gameSpeed);
        setupElements(agents, territories);
    }

    public Game(Game Game) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        if (Game == null) {
            return;
        }

        //System.out.println("Create a copy of all the players");
        for(String key : Game.getPlayers().keySet()){
            Constructor<Agent> constr;
            //Instantiate the agents

            Agent player = Game.getPlayers().get(key);
            constr = (Constructor<Agent>) player.getClass().getConstructor(Agent.class);
            this.players.put(key, (Agent) constr.newInstance(player));
            this.players.get(key).setGame(this);
        }

        //System.out.println("Copy of all the continents of the game");
        for(String key : Game.getContinents().keySet()){
            Continent cont = Game.getContinents().get(key);
            this.continents.put(key, new Continent(cont));
            if(cont.getOccupier()!=null)
                this.continents.get(key).setOccupier(this.players.get(cont.getOccupier().getColor()));
        }

        //System.out.println("DeepCopy of all the tile of the game");
        for(String key : Game.getTiles().keySet()){
            Tile til = Game.getTiles().get(key);

            this.tiles.put(key, new Tile(til));

            this.tiles.get(key).setContinent(this.continents.get(til.getContinent().getName()));

            if(til.getOccupier() != null){
                this.tiles.get(key).setOccupier(this.players.get(til.getOccupier().getColor()), til.getNumTroops());
            }
        }

        //System.out.println("DeepCopy des voisins d'une tile");
        for(String key: this.tiles.keySet()){
            ArrayList<Tile> neigh = new ArrayList<>();
            for(Tile t: this.tiles.get(key).getNeighbors()){
                neigh.add(this.tiles.get(t.getName()));
            }
            this.tiles.get(key).setNeighbors(neigh);
        }

        //System.out.println("DeepCopy des bonus tiles des decks");
        for(Agent player: this.getPlayers().values()){
            for(Card c: player.getDeck()){
                if(c.getType() != CardType.JOKER)
                    c.setBonusTile(this.getTiles().get(c.getBonusTile().getName()));
            }
        }

    }

    public void play() {
        // for each player --> deploy, attack, fortify
        int index = 0;
        int pTerritories;
        ArrayList<Agent> turns = new ArrayList<>(players.values());
        ExecutorService executor = Executors.newSingleThreadExecutor();
        System.out.println("Let's begin !");
        while(turns.size() > 1) {
            support.firePropertyChange("newTurn", turns.get(Math.floorMod(index-1,turns.size())), turns.get(index));
            Agent p = turns.get(index); // Perry the platypus
            System.out.println("Player " + p.getColor() + "'s turn");
            p.getDeck().forEach(System.out::println);

            pTerritories = p.getTiles().size();
            //if (p instanceof AgentMonteCarlo) this.playAgent(p, executor);
            Future<Actions> future = executor.submit(p);
            FutureTask<Actuator> futureTask;
            long startTurn = System.currentTimeMillis();
            try {
                Actions a = future.get(playclock*1000L - (System.currentTimeMillis() - startTurn), TimeUnit.MILLISECONDS);

                pTerritories = p.getTiles().size();
                // deploy
                String print = "";
                try {
                    support.firePropertyChange("newPhase", "", "Deployment");
                    // Set the futureTask
                    futureTask = new FutureTask<>((() -> {
                        try {
                            a.getDeployment(p);
                        } catch (IOException | URISyntaxException | IllegalActionException |
                                 SimulationRunningException e) {
                            throw new RuntimeException(e);
                        }
                    }), null);
                    // Submit the task until we get the result
                    executor.submit(futureTask);
                    while (!futureTask.isDone() && (playclock*1000L - (System.currentTimeMillis() - startTurn))>0) {
                        Thread.sleep(1);
                    }
                    boolean flag = (futureTask.isDone()) && a.getDeployment(p) != null;
                    if (flag) {
                        if (a.getDeployment(p).isNumTroopsLegal(p)) {
                            while (flag) {

                                // Set the futureTask
                                futureTask = new FutureTask<>((() -> {
                                    try {
                                        a.performDeployment(p);
                                    } catch (IOException | URISyntaxException | IllegalActionException |
                                             SimulationRunningException e) {
                                        throw new RuntimeException(e);
                                    }
                                }), null);

                                print = a.getDeployment(p).toString();
                                // In case of playing cards
                                if (a.getDeployment(p) instanceof MultiDeploy && ((MultiDeploy) a.getDeployment(p)).getDeploys().get(0) instanceof PlayCards) {
                                    ArrayList<Card> cards = ((PlayCards) ((MultiDeploy) a.getDeployment(p)).getDeploys().get(0)).getCards();
                                    theDiscardPile.addAll(cards);
                                }

                                if (gameSpeed > 0) Thread.sleep(300 / gameSpeed);
                                while (gameSpeed < -1) Thread.sleep(50);

                                // Submit the task until we get the result
                                executor.submit(futureTask);
                                while (!futureTask.isDone() && (playclock*1000L - (System.currentTimeMillis() - startTurn))>0) {
                                    Thread.sleep(1);
                                }
                                flag = a.getTroopsRemaining()>0 && futureTask.isDone();
                                System.out.println("    [Success] :: " + print);

                            }
                        }
                    }
                } catch (SimulationRunningException e) {
                    System.out.println("    [Failed:Simulation currently running] :: " + print);
                } catch (IllegalActionException e) {
                    System.out.println("    [Failed:too many troops] :: " + print);
                } catch (IOException | URISyntaxException e) {
                    throw new RuntimeException(e);
                }

                // attack
                futureTask = new FutureTask<>((() -> {a.getFirstOffensive(p,"Attacking");}), null);
                executor.submit(futureTask);
                print = "";
                do {
                    support.firePropertyChange("newPhase", "", "Attacking");

                    // Execute it and wait for the answer
                    while (!futureTask.isDone() && (playclock*1000L - (System.currentTimeMillis() - startTurn))>0) {
                        Thread.sleep(1);
                    }
                    if (futureTask.isDone() && a.getFirstOffensive(p, "GetAttack") instanceof Attack) {
                        print = a.getFirstOffensive(p, "GetAttack").toString();
                        System.out.println("    [Success] :: " + print);
                        if (gameSpeed > 0) Thread.sleep(300 / gameSpeed);
                        while (gameSpeed < -1) Thread.sleep(50);
                    }
                    System.out.println();
                    // Set the futureTask
                    futureTask = new FutureTask<>((() -> {
                        try {
                            a.performAttack(p);
                        } catch (IllegalActionException | SimulationRunningException e) {
                            throw new RuntimeException(e);
                        }
                    }), null);
                    // Execute it and wait for the answer
                    executor.submit(futureTask);
                    while (!futureTask.isDone() && (playclock*1000L - (System.currentTimeMillis() - startTurn))>0) {
                        Thread.sleep(1);
                    }
                } while (futureTask.isDone() && a.getFirstOffensive(p, "GetAttack") != null && a.getFirstOffensive(p, "GetAttack") instanceof Attack);

                // fortify
                print = "";
                try {
                    support.firePropertyChange("newPhase", "", "Fortifying");
                    // Set the futureTask
                    futureTask = new FutureTask<>((() -> {a.getFirstOffensive(p,"Fortifying");}), null);
                    // Submit the task until we get the result or the timeout exception
                    executor.submit(futureTask);
                    while (!futureTask.isDone() && (playclock*1000L - (System.currentTimeMillis() - startTurn))>0) {
                        Thread.sleep(1);
                    }
                    if (futureTask.isDone() && a.getFirstOffensive(p,"GetFortify") != null) {
                        print = a.getFirstOffensive(p,"GetFortify").toString();
                        a.performFortify(p);
                        System.out.println("    [Success] :: " + print);
                        if (gameSpeed > 0) Thread.sleep(300 / gameSpeed);
                        while (gameSpeed < -1) Thread.sleep(50);
                    }
                } catch (SimulationRunningException e) {
                    System.out.println("    [Failed:Simulation currently running] :: " + print);
                } catch (IllegalActionException e) {
                    System.out.println("    [Failed:too many troops] :: " + print);
                }
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new RuntimeException(e);
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
                    p.addAllCards(agent.getDeck());
                }
            }

            // Check if the stack is empty or not and then refuel the Stack with the DiscardPile
            if (theStack.isEmpty()) {
                theStack.addAll(theDiscardPile);
                theDiscardPile = new Stack<>();
                Collections.shuffle(theStack);
            }
            // check if the player could retrieve cards
            if (p.getTiles().size() > pTerritories) {
                if (gameSpeed > 0) {
                    support.firePropertyChange("newPhase", "", "Drawing a card");
                    p.addCard(theStack.pop());
                }

            }
            executor.shutdownNow();
            executor = Executors.newSingleThreadExecutor();

            // next player
            index = Math.floorMod(index+1,turns.size());
            try {
                if (gameSpeed>0 && (playclock*1000L - (System.currentTimeMillis() - startTurn))>0) Thread.sleep(1200/gameSpeed);
                while (gameSpeed<-1) Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("[END] The winner is : " + turns.get(0).getColor() + ". Psartek !");
        support.firePropertyChange("Winner", 0, turns.get(0).getColor());
    }

    private void setupElements(ArrayList<Agent> agents, int territories) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {

        // Setup the map with the data resources
        TextReader.readAll(this, env_data);

        for(Agent a : agents) {
            a.setGame(this);
            players.put(a.getColor(), a);
        }

        try {
            Agent grey = new Legume("Grey", this);
            players.put(grey.getColor(), grey);

            // Randomly distribute the tiles among the players
            distributeTiles(agents.get(0), grey, agents.get(1), territories);
        } catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException | InstantiationException |
                 IllegalAccessException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        for(Agent a : agents){

            if(a instanceof AgentMonteCarlo){
                ((AgentMonteCarlo) a).setRoot();
            }
            if(a instanceof AgentMCNN){
                ((AgentMCNN) a).setRoot();
            }
        }
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

    public int getGameSpeed() {
        return gameSpeed;
    }

    public void setGameSpeed(int gameSpeed) {
        this.gameSpeed = gameSpeed;
    }

    public void setTiles(HashMap<String, Tile> tiles) {
        this.tiles = tiles;
    }

    public HashMap<String, Tile> getTiles() {
        return tiles;
    }

    public Stack<Card> getTheDiscardPile() {
        return theDiscardPile;
    }

    public void setTheDiscardPile(Stack<Card> theDiscardPile) {
        this.theDiscardPile = theDiscardPile;
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

    public Stack<Card> getTheStack() {
        return theStack;
    }

    public void setTheStack(Stack<Card> theStack) {
        this.theStack = theStack;
    }

    public int score(Agent player){
        int scoreAgent = 0;
        int scoreOpp = 0;
        for(Tile t: this.getTiles().values()){
            if(t.getOccupier().getColor() == player.getColor()){scoreAgent += t.getNumTroops();}
            else if(t.getOccupier().getColor() != "Grey"){scoreOpp += t.getNumTroops();}
        }
        return scoreAgent-scoreOpp;
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    public int getPlayclock() {
        return playclock;
    }

    public void setPlayclock(int playclock) {
        this.playclock = playclock;
    }

    public static void main(String[] args) throws IOException {
        // this main function will just start the game, with parameters (list of player, list of tiles)
        // the Game object will make players play, and restrict time for turn. It will end by giving the winner
    }
}
