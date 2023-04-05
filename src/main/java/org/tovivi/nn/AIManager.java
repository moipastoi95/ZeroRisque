package org.tovivi.nn;

import Jama.Matrix;
import org.tovivi.agent.Agent;
import org.tovivi.environment.*;
import org.tovivi.environment.action.*;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class AIManager {

    private String configName;
    private HashMap<String,Integer> allCards; // Format of the key : cardType:country
    private String[] allTiles; // Format : country
    private String[] allEdges; // Format : country1:country2
    private final int MAX_TROOPS = 1000; // Number of maximum troops on a tile (for input Normalization)

    private final int CARDS_START_INDEX = 126; // Starting index to encode the cards

    private final int JOKERS_START_INDEX = 252; // Starting index to encode the cards
    private final int TURN_INDEX = 255; // index of the bit that encode the turn
    private HashMap<String, MultiLayerPerceptron> models = new HashMap<>();

    private final double TEST_RATIO = 0.1; // Proportion of test data compared with train data

    /**
     * Constructor of the AIManager. Game needs to be just initialized.
     * @param configName the config for the MLPs
     */
    public AIManager(String configName) throws FileNotFoundException, URISyntaxException {
        //TODO
        // - et qui sait écrire les données d'entrainement
        //          ce sera typiquement juste un tableau a ecrire (La donnée d'entrée et de sortie sont sur la même ligne
        // -Créer l'agent
        // PENSER A COPIER LE PYTHON A CHAQUE FOIS
        this.configName = configName;
        TextReader.readAllNames(this);
        config();
        System.out.println(getAllTiles()[41]);
    }

    public void config() throws URISyntaxException, FileNotFoundException {
        File file = new File(AIManager.class.getResource(configName).toURI());
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Skip empty lines and comment lines
                if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
                    // Split the line by the separator ":"
                    String[] parts = line.trim().split(":");
                    // Use the activation functions name and the name in config to create the MLP
                    models.put(parts[0], new MultiLayerPerceptron(new ArrayList<>(Arrays.asList(parts[2].trim().split(","))), parts[0], getConfigName()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Encode the game in a vector of 256 parameters
     * @param game to encode
     * @param player that needs to play (turn)
     * @return The matrix that has been build
     */
    public Matrix gameToMatrix(Game game, String player) {

        HashMap<String, Agent> players = game.getPlayers();
        Stack<Card> discardPile = game.getTheDiscardPile();
        HashMap<String, Tile> tiles = game.getTiles();
        Matrix G = new Matrix(1, TURN_INDEX+1);

        // Encode the tiles
        for (int i=0; i<getAllTiles().length; i++) {
            Tile t = tiles.get(getAllTiles()[i]);
            int index = getMultiplier(t.getOccupier().getColor())*getAllTiles().length + i;
            G.set(0,index, (double) t.getNumTroops()/MAX_TROOPS);
        }

        // Encode the cards of the players
        for (Agent p : players.values()) {
            ArrayList<Card> deck = p.getDeck();
            int offset = getMultiplier(p.getColor())*getAllTiles().length;
            int index;
            for (Card c : deck) {
                if (c.getType().equals(CardType.JOKER)) {
                    // index = joker start index + player multiplier
                    index = JOKERS_START_INDEX + getMultiplier(p.getColor());
                }
                else {
                    // index = cards start index + player offset (multiplier*42) + card position
                    index = CARDS_START_INDEX + offset + getAllCards().get(c.getBonusTile().getName());
                }
                G.set(0,index, G.get(0,index)+1);
            }
        }

        // Encode the cards in the discard pile
        for (Card c : discardPile) {
            int offset = getMultiplier("DiscardPile")*getAllTiles().length; // Typically = 2*42
            int index;
            if (c.getType().equals(CardType.JOKER)) {
                // index = joker start index + player multiplier
                index = JOKERS_START_INDEX + getMultiplier("DiscardPile");
            }
            else {
                // index = cards start index + player offset (multiplier*42) + card position
                index = CARDS_START_INDEX + offset + getAllCards().get(c.getBonusTile().getName());
            }
            G.set(0,index, G.get(0,index)+1);
        }
        G.set(0,TURN_INDEX, getMultiplier(player));
        return G;
    }


    public Matrix prediction(String modelName, Matrix X) {
        return getModels().get(modelName).predict(new Matrix(X.getArray()));
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public HashMap<String, MultiLayerPerceptron> getModels() {
        return models;
    }

    public void setModels(HashMap<String, MultiLayerPerceptron> models) {
        this.models = models;
    }

    public HashMap<String,Integer> getAllCards() {
        return allCards;
    }

    public void setAllCards(HashMap<String,Integer> allCards) {
        this.allCards = allCards;
    }

    public String[] getAllTiles() {
        return allTiles;
    }

    public void setAllTiles(String[] allTiles) { //TODO FAIRE LA CONNEXION AVEC LE TEXT READER
        this.allTiles = allTiles;
    }

    public String[] getAllEdges() {
        return allEdges;
    }

    public void setAllEdges(String[] allEdges) {
        this.allEdges = allEdges;
    }

    /**
     * Return the right multiplier according to the entity (it can be a player of the discard pile)
     * It is mainly use to set the right coefficient in the Game Matrix
     * @param entity
     * @return the int multiplier (1,2 or 3)
     */
    public int getMultiplier(String entity) {
        switch (entity){
            case "Red":
                return 0;
            case "Blue":
                return 1;
            default:
                return 2;
        }
    }

    /**
     * Function that return the Deploy action associated to column c in the Matrix X (1-line Matrix)
     * @param player that want to do this move
     * @param c column in the Matrix
     * @return the corresponding Deploy if it is legal, null otherwise
     */
    public Deploy legalDeploy(Agent player, int c, int numToDeploy) {
        try {
            Tile t = player.getGame().getTiles().get(getAllTiles()[c%42]);
            int numTroops ;
            switch (c/42) {
                case 0:
                    numTroops=Math.min(2, numToDeploy);
                    break;
                case 1:
                    numTroops = (int) Math.ceil((double) numToDeploy/4);
                    break;
                case 2:
                    numTroops = (int) Math.ceil((double) numToDeploy/2);
                    break;
                case 3:
                    numTroops = (int) Math.ceil((double) 3*numToDeploy/4);
                    break;
                default:
                    numTroops = numToDeploy;
            }
            Deploy res = new Deploy(numTroops, t);
            if (res.isMoveLegal(player)) return res;
            return null;
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Function that return the Attack action associated to column c in the Matrix X (1-line Matrix)
     * We suppose that c will never have the value 498 which correspond to skip the Attack phase (the MCTS Agent will know it)
     * @param player that want to do this move
     * @param c column in the Matrix
     * @return the corresponding Attack if it is legal, null otherwise
     */
    public Attack legalAttack(Agent player, int c) {
        try {
            int attack = c%166; // attack encode the to tiles in conflict and which tile is attacking
            int edge = attack%83; // edge extracts which tiles are in conflict in attack
            int order = attack/83; // order encodes who is attacking, if 0 it follow the order in the String[] AllEdges : the first one is attacking, if 1 it's the opposite

            String edgeStr = getAllEdges()[edge]; String country1 = edgeStr.substring(0,edgeStr.indexOf(':')); String country2 = edgeStr.substring(edgeStr.indexOf(':')+1);
            Tile tile1 = player.getGame().getTiles().get(country1);
            Tile tile2 = player.getGame().getTiles().get(country2);

            Tile fromTile = (order==0) ? tile1 : tile2;
            Tile toTile = (order==0) ? tile2 : tile1;

            int numTroops ;
            switch (c/166) { // encode how many troops to send to the next tile
                case 0:
                    numTroops=1;
                    break;
                case 1:
                    numTroops = (int) Math.floor((double) fromTile.getNumTroops()/2);
                    break;
                default:
                    numTroops = fromTile.getNumTroops();
            }
            Attack res = new Attack(fromTile, toTile, numTroops, null, null);
            if (res.isMoveLegal(player)) return res;
            return null;
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Function that return the Fortify action associated to column c in the Matrix X (1-line Matrix)
     * We suppose that c will never have the value 1722 which correspond to skip the Fortify phase (the MCTS Agent will know it)
     * @param player that want to do this move
     * @param c column in the Matrix
     * @return the corresponding Attack if it is legal, null otherwise
     */
    public Fortify legalFortify(Agent player, int c) {
        try {
            int fromTileInt = c/42; // fromTileInt encodes the to tile where the troops come from
            int toTileInt = c%41; // fromTileInt encodes which tile receives the troops (be careful that the fromTile is not included so the int order is shifted by 1 from it)
            // So we have to increment toTileInt if above fromTileInt to find the real int
            toTileInt = (toTileInt>=fromTileInt) ? toTileInt+1 : toTileInt;

            String fromTileStr = getAllTiles()[fromTileInt];
            String toTileStr = getAllTiles()[toTileInt];

            Tile fromTile = player.getGame().getTiles().get(fromTileStr);
            Tile toTile = player.getGame().getTiles().get(toTileStr);

            Fortify res = new Fortify(fromTile, toTile, fromTile.getNumTroops()-1);
            if (res.isMoveLegal(player)) {
                return res;
            }

            return null;
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Function that save the data generates by the Agent
     * @param X the Matrix to save
     * @param modelName name of the concerned model
     * @param type type of the data (input (x) or target (y))
     * @param goal goal of the data (train or test)
     */
    public void saveData(Matrix X, String modelName, String type, String goal) throws IOException, URISyntaxException {

        double rand = Math.random();
        String path = configName+"_models/" + modelName + "/" + goal + "/" + type;
        path += type;

        File file;

        URL url = AIManager.class.getResource(path);
        if (url == null) {
            URL modelURL = AIManager.class.getResource(configName + "_models/" + modelName);
            System.out.println(modelURL);
            path = modelURL.toURI().getPath();
            path += goal + "/" + type;
            file = new File(path);
            if (file.getParentFile().mkdirs()) System.out.println("Parent repositories created");;
            if (file.createNewFile()) {
                System.out.println("File : " + path + " created");
            }
        }
        else {
            System.out.println("File " + path + " loaded");
            file = new File(url.toURI());
        }
        // Create a BufferedWriter object to write to file
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
        // Loop through each element in X and write it to file
        double[][] xData = X.getArray();
        for (int i = 0; i < xData.length; i++) {
            for (int j = 0; j < xData[0].length; j++) {
                writer.write(String.valueOf(xData[i][j]));
                // If this is not the last column, add a comma separator
                if (j < xData[0].length - 1) {
                    writer.write(",");
                }
            }
            writer.newLine();
        }
        // Close the BufferedWriter object for file1
        writer.close();
    }

    /**
     * Function that save the data generates by the Agent
     * and distribute them in the test training data with the right TEST_RATIO
     * @param In the Matrix that encode the input
     * @param Out the Matrix that encode the target for the model
     * @param modelName name of the concerned model
     */
    public void saveInOutData(Matrix In, Matrix Out, String modelName) {
        double rand = Math.random();
        String goal = (rand>TEST_RATIO) ? "train" : "test";
        try {
            saveData(In, modelName, "x", goal);
            saveData(Out, modelName, "y", goal);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        // Create the neural networks according to the config file

        String configName = "config";

        String mainPath = AIManager.class.getResource("ZeroRisqueNN/main.py").toURI().getPath();
        System.out.println(mainPath);
        Process p = Runtime.getRuntime().exec("python3 " + mainPath + " " + configName);
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        in.lines().forEach(System.out::println);

        /*
        // Load the NN according to the config
        AIManager aim = new AIManager(configName);
        aim.config();
        System.out.println(aim.models.get("evaluation"));
        */
    }
}
