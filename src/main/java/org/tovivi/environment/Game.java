package org.tovivi.environment;


import org.tovivi.agent.Agent;
import org.tovivi.agent.AgentMonteCarlo;
import org.tovivi.agent.Legume;
import org.tovivi.environment.action.*;
import org.tovivi.environment.action.exceptions.IllegalActionException;
import org.tovivi.environment.action.exceptions.SimulationRunningException;

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

    // HashMap who links the Continents to their names
    private HashMap<String, Continent> continents = new HashMap<>();

    // HashMap who links the Tiles to their names
    private HashMap<String, Tile> tiles = new HashMap<>();

    //HashMap of the players
    private HashMap<String, Agent> players = new HashMap<>();
    private Stack<Card> theStack = new Stack<>();

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
            Agent p = turns.get(index); // Perry the platypus
            System.out.println("Player " + p.getColor() + "'s turn");
            p.getDeck().forEach(System.out::println);

            pTerritories = p.getTiles().size();
            if (p instanceof AgentMonteCarlo) this.playAgent(p, executor);

            else {
                Future<Actions> future = executor.submit(p);
                try {
                    Actions a = future.get(playclock, TimeUnit.SECONDS);

                    pTerritories = p.getTiles().size();

                    // deploy
                    String print = "";
                    try {
                        boolean flag = a.getDeployment() != null;
                        if (flag) {
                            if (a.getDeployment().isNumTroopsLegal(p)) {
                                while (flag) {
                                    print = a.getDeployment().toString();
                                    flag = a.performDeployment(p);
                                    System.out.println("    [Success] :: " + print);
                                    if (gameSpeed > 0) Thread.sleep(600 / gameSpeed);
                                    while (gameSpeed < -1) Thread.sleep(50);
                                }
                            }
                        }
                    } catch (SimulationRunningException e) {
                        System.out.println("    [Failed:Simulation currently running] :: " + print);
                    } catch (IllegalActionException e) {
                        System.out.println("    [Failed:too many troops] :: " + print);
                    }

                    // attack
                    print = "";
                    try {
                        do {
                            if (a.getFirstOffensive() != null) {
                                print = a.getFirstOffensive().toString();
                                System.out.println("    [Success] :: " + print);
                                if (gameSpeed > 0) Thread.sleep(600 / gameSpeed);
                                while (gameSpeed < -1) Thread.sleep(50);
                            }
                        } while (a.performAttack(p));
                    } catch (SimulationRunningException e) {
                        System.out.println("    [Failed:Simulation currently running] :: " + print);
                    } catch (IllegalActionException e) {
                        System.out.println("    [Failed:too many troops] :: " + print);
                    }

                    // fortify
                    print = "";
                    try {
                        if (a.getFirstOffensive() != null) {
                            print = a.getFirstOffensive().toString();
                            a.performFortify(p);
                            System.out.println("    [Success] :: " + print);
                            if (gameSpeed > 0) Thread.sleep(600 / gameSpeed);
                            while (gameSpeed < -1) Thread.sleep(50);
                        }
                    } catch (SimulationRunningException e) {
                        System.out.println("    [Failed:Simulation currently running] :: " + print);
                    } catch (IllegalActionException e) {
                        System.out.println("    [Failed:too many troops] :: " + print);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (TimeoutException e) {
                    throw new RuntimeException(e);
                }
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


            // executor.shutdownNow();

            // next player
            index += 1;
            if (index >= turns.size()) {
                index = 0;
            }
            try {
                if (gameSpeed>0) Thread.sleep(1800/gameSpeed);
                while (gameSpeed<-1) Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
        executor.shutdownNow();
        System.out.println("[END] The winner is : " + turns.get(0).getColor() + ". Psartek !");
    }

    private void playAgent(Agent p, ExecutorService executor)  {
        Actuator a = null;
        Future<Actions> future = executor.submit(p);
        do {
            String print = "";
            try {
                //System.out.println("Appel de l'agent pour créer une action");
                a = ((AgentMonteCarlo) p).actionTest();
                //System.out.println(a.getClass().toString());

                if(a instanceof MultiDeploy) {
                    // deploy
                    MultiDeploy md = (MultiDeploy) a;
                    try {
                        if(md.isNumTroopsLegal(p)){
                             for(Deployment dep: md.getDeploys()){
                                if(dep instanceof Deploy){
                                    ((Deploy) dep).setTile(this.getTiles().get(dep.getTiles().get(0).getName()));
                                }
                                if(dep instanceof PlayCards){
                                    ((PlayCards) dep).setPlayer(this.getPlayers().get(((PlayCards) dep).getPlayer().getColor()));
                                }
                                dep.perform(p);
                                print = dep.toString();
                                System.out.println("    [Success] :: " + print);
                                Thread.sleep(600 / gameSpeed);
                                while (gameSpeed<-1) Thread.sleep(50);
                            }
                        }
                    } catch (SimulationRunningException e) {
                        System.out.println("    [Failed:Simulation currently running] :: " + print);
                    } catch (IllegalActionException e) {
                        System.out.println("    [Failed:too many troops] :: " + print);
                    }
                }

                else if(a instanceof Attack) {
                    // attack
                    print = "";
                    Attack att = (Attack) a;
                    try {
                        att.setFromTile(this.getTiles().get(att.getFromTile().getName()));
                        att.setToTile(this.getTiles().get(att.getToTile().getName()));
                        print = att.toString();
                        System.out.println("    [Success] :: " + print);
                        Thread.sleep(600 / gameSpeed);
                        att.perform(p);

                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalActionException e) {
                        throw new RuntimeException(e);
                    } catch (SimulationRunningException e) {
                        throw new RuntimeException(e);
                    }
                }

                /**else if(a.getFirstOffensive() instanceof Fortify) {
                    print = "";
                    // fortify
                    try {
                            print = a.getFirstOffensive().toString();
                            a.performFortify(p);
                            System.out.println("    [Success] :: " + print);
                            Thread.sleep(600 / gameSpeed);
                    } catch (SimulationRunningException e) {
                        System.out.println("    [Failed:Simulation currently running] :: " + print);
                    } catch (IllegalActionException e) {
                        System.out.println("    [Failed:too many troops] :: " + print);
                    }
                }*/
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            //System.out.println("Si l'agent envoie encore une action on continue a jouer");
        } while(a != null);

    }

    private void setupElements(ArrayList<Agent> agents, int territories) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {

        // Setup the map with the data resources
        TextReader tr = new TextReader();
        tr.readAll(this, env_data);

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
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        for(Agent a : agents){

            if(a instanceof AgentMonteCarlo){
                ((AgentMonteCarlo) a).setRoot();
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

    public static void main(String[] args) throws IOException {
        // this main function will just start the game, with parameters (list of player, list of tiles)
        // the Game object will make players play, and restrict time for turn. It will end by giving the winner
    }
}
