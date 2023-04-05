package org.tovivi.agent.treeNN;

import Jama.Matrix;
import org.tovivi.agent.Agent;
import org.tovivi.agent.AgentMCNN;
import org.tovivi.agent.Node;
import org.tovivi.environment.Card;
import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;
import org.tovivi.environment.action.*;
import org.tovivi.environment.action.exceptions.IllegalActionException;
import org.tovivi.environment.action.exceptions.SimulationRunningException;
import org.tovivi.nn.AIManager;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class NodeNN {

    /**
     * Create a node for the MCNN algorithm
     * A bit different from Node because it stores also the index of the action taken to lead to this node from the parent
     * @param game state
     * @param N number of visits of this node
     * @param parent node
     * @param deck
     * @param player that needs to play
     * @param phase current phase in the game state
     */

    private Game game;
    private int N = 0; //Number of visits of this node

    private double wins = 0; // Sum of the estimations of this node and child nodes rewards

    private final int maxChild = 5;

    private double score;

    private double priorP;
    private String player;
    private String phase;
    private NodeNN parent;

    private int pos;

    private int numToDeploy;

    private boolean pick; // if the player conquer a tile, it becomes true, and will pick a card at the end of the turn. After the picking, pick becomes false

    private AIManager aim;
    private Matrix inputG; // Matrix that encode the game state
    private Matrix targetA; // Output of the model that will become the target
    private HashMap<Integer, NodeNN> children = new HashMap<>(); // Hashmap of the child nodes (the key is the position in the Matrix of the probabilities

    public NodeNN(Game game, int pos, NodeNN parent, Agent player, String phase, AIManager aim, double priorP, int numToDeploy, boolean pick) throws IOException, URISyntaxException {
        this.game = game;
        this.parent = parent;
        this.player = player.getColor();
        setPhase(phase);
        this.pos = pos;
        this.aim = aim;
        inputG = aim.gameToMatrix(game, player.getColor());
        this.numToDeploy = numToDeploy;
        this.pick = pick;
        setScore(priorP);
        setPriorP((priorP));
    }

    public NodeNN(NodeNN n) throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        this.game = new Game(n.game);
        this.parent = getParent();
        this.player = n.getPlayer().getColor();
        setPhase(n.getPhase());
        this.aim = n.getAim();
        this.pos = n.pos;
        inputG = n.getInputG();
        this.numToDeploy = n.getNumToDeploy();
        this.pick = n.isPick();
        setScore(n.getPriorP());
        if (n.getParent()!=null) {
            n.getParent().getChildren().put(n.pos,this);
        }
    }

    /**
     * @return The evaluation of the current game state for the two players
     */
    public Matrix eval() {
        return aim.getModels().get("evaluation").predict(inputG);
    }

    public Matrix predict(String phase) {
        return aim.getModels().get(phase).predict(inputG);
    }

    /**
     * Generate the children of a Node according to the prediction
     * and select the n best ones
     * @param maxChild number of child nodes to generate
     * @param numToDeploy number of troops to deploy (in case of deployment)
     * @return the Best maxChilds actions according to the NN (for attack and fortify we always take the null action)
     */
    public HashMap<Integer, NodeNN> generateChildren(int maxChild, int numToDeploy) {

        HashMap<Integer, NodeNN> res = new HashMap<>();
        LinkedList<Integer> priorPs = new LinkedList<>(); // List of the position of the actions in the targetMatrix
        LinkedList<Actuator> actList = new LinkedList<>(); // List of the corresponding actuators

        // If we are in the case of fortify or attack phase we will add the nullAction
        int nullAction = (getPhase().compareTo("deploy")==0) ? 0 : 1;
        double[][] targetArr = predict(getPhase()).getArray();
        Actuator act = null;
        for (int i=0; i<targetArr[0].length; i++) {

            switch (getPhase()) {
                case "deploy":
                    act = aim.legalDeploy(getPlayer(),i, numToDeploy);
                    break;
                case "attack":
                    if (i<targetArr[0].length-1) {
                        act = aim.legalAttack(getPlayer(),i);
                    }
                    break;
                case "fortify":
                    if (i<targetArr[0].length-1) {
                        act = aim.legalFortify(getPlayer(),i);
                    }
                    break;
            }
            if (act==null && !(i==targetArr[0].length-1 && getPhase().compareTo("deploy")!=0)) {
                targetArr[0][i] = 0;
            }
            int j=-1;
            // While the value of the action i is higher than the element j+1 of the linked list, continue
            if (i<targetArr[0].length-1 || getPhase().compareTo("deploy")==0) {

                while (j+1<priorPs.size() && (targetArr[0][priorPs.get(j+1)]<targetArr[0][i])) {
                    j++;
                }
                // If j>=0 then the value need to be added to the linked list

                if (j>=0) {
                    // The value will be added to the list (we check j+1 is indeed a index in the list)
                    if (j+1==priorPs.size()) {
                        priorPs.add(i);
                        actList.add(act);
                    }
                    else {
                        priorPs.add(j+1,i);
                        actList.add(j+1, act);
                    }
                    // We will only add maxChild-1 nodes because we will always add the null action
                    // If the list size reaches the max size -1 , we remove the first one
                    if (priorPs.size()>maxChild-nullAction) {
                        priorPs.remove();
                        actList.remove();
                    }
                }
                // If the list size does not reach maxChild-1, we add the actuator anyway
                else if (priorPs.size()<maxChild-nullAction) {
                    priorPs.addFirst(i);
                    actList.addFirst(act);
                }
            }
        }
        setTargetA(new Matrix(targetArr));
        try {
            for (int k=0; k<priorPs.size(); k++) {
                res.put(priorPs.get(k), getNextNodeNN(priorPs.get(k), actList.get(k), targetArr[0][priorPs.get(k)]));
            }
            // Add the null action if it an attack or a fortify (this is the last action)
            if (getPhase().compareTo("deploy")!=0) {
                res.put(targetArr[0].length-1, getNextNodeNN(targetArr[0].length-1, null, targetArr[0][targetArr[0].length-1]));

            }

        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        return res;
    }

    /**
     * Generate the Node according to the given actuator and score
     * @param act Actuator
     * @param score score given by the NN model
     * @return The next NodeNN deduced from the actuator (it can be a chance Node in the case of an attack or a card picking
     * @throws IOException
     * @throws URISyntaxException
     */
    public NodeNN getNextNodeNN(int pos, Actuator act, double score) throws IOException, URISyntaxException {

        String nextPhase;

        try {
            Game nextGame = new Game(getGame());
            // System.out.println(nextGame.getTheDiscardPile().size()==getGame().getTheDiscardPile().size());
            String nextPl = nextPlayer(nextGame);

            // After an attack or a fortify phase, it can lead to a Chance node
            if (getPhase().compareTo("attack")==0 || getPhase().compareTo("fortify")==0) {
                // If the act is null (and the player can not pick a card after fortify) it leads to the next node without changing the game
                if (act==null) {
                    switch (getPhase()) {
                        case "attack":
                            return new NodeNN(nextGame, pos,this,nextGame.getPlayers().get(player),"fortify", aim, score, -1, isPick());
                        case "fortify":
                            // NumToDeploy will be updated in the next node where the next player will see if he can play cards
                            if (!isPick()) {
                                // If the next player is not Grey, it is a normal NodeNN
                                if (nextPl.compareTo("Grey")!=0) return new NodeNN(nextGame, pos,this, nextGame.getPlayers().get(nextPlayer(nextGame)),
                                        "deploy", aim, score, -1, false);
                                // Else it is a chanceNode that will determine where the grey player will deploy its troops
                                return new ChanceNodeNN(nextGame, pos,this, nextGame.getPlayers().get(nextPl), "deploy", aim, score, false, null);
                            }
                    }
                }
                // If it is a (fortify action and the player can pick) or (attack action (not null)) it leads to a chance node
                Attack attack = null;
                if (getPhase().compareTo("attack")==0 || (getPhase().compareTo("fortify")==0 && isPick())) {
                    // if it is an (not null) attack we can send it to the chance node
                    if (act!=null && getPhase().compareTo("attack")==0) {
                        attack = (Attack) act;
                        ((Attack) act).setFromTile(nextGame.getTiles().get(((Attack) act).getFromTile().getName()));
                        ((Attack) act).setToTile(nextGame.getTiles().get(((Attack) act).getToTile().getName()));
                    }
                    // If it is a fortify phase, we can perform it before going to the chance node (attack can be null here)
                    else if (act!=null && getPhase().compareTo("fortify")==0) {
                        ((Fortify) act).setFromTile(nextGame.getTiles().get(((Fortify) act).getFromTile().getName()));
                        ((Fortify) act).setToTile(nextGame.getTiles().get(((Fortify) act).getToTile().getName()));
                    }
                    // The chanceNode knows that it needs to pick a card because its phase is fortify
                    return new ChanceNodeNN(getGame(), pos,this, getPlayer(nextGame), getPhase(), aim, score, isPick(), attack);
                }
            }
            int nextNumToDeploy = -1;
            // if act is a Deployment we can modify the game according to these deployments.
            if(act instanceof Deployment){
                for (int i=0; i<((Deployment) act).getTiles().size(); i++) {
                    String tileName = ((Deployment) act).getTiles().get(i).getName();
                    nextGame.getTiles().get(tileName)
                            .setNumTroops(nextGame.getTiles().get(tileName).getNumTroops() + ((Deployment) act).getNumTroops());
                }
                nextNumToDeploy = numToDeploy-((Deployment) act).getNumTroops();
                // If there is still troops to deploy, the next node will be a deploy node too
                nextPhase = (nextNumToDeploy>0) ? "deploy" : "attack";
                return new NodeNN(nextGame, pos, this, getPlayer(nextGame), nextPhase, aim, score, nextNumToDeploy, isPick());
            }
            else {
                // Here it can only be a fortify phase because deploy and attack have been managed
                if (act!=null) {
                    ((Fortify) act).setFromTile(nextGame.getTiles().get(((Fortify) act).getFromTile().getName()));
                    ((Fortify) act).setToTile(nextGame.getTiles().get(((Fortify) act).getToTile().getName()));
                    act.perform(nextGame.getPlayers().get(player));
                }
                // Next phase is deploy of the next player because the pick case has been tested (we set pick as false)
                nextPhase = "deploy";
                if (nextPl.compareTo("Grey")!=0) return new NodeNN(nextGame, pos, this,nextGame.getPlayers().get(player), nextPhase, aim, score, nextNumToDeploy, false);
                return new ChanceNodeNN(nextGame, pos,this, nextGame.getPlayers().get(nextPl), "deploy", aim, score, false, null);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException | IllegalActionException | SimulationRunningException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the best nodes among the children
     */
    public NodeNN getBestNode() {
        double max = Double.MIN_VALUE;
        NodeNN res = null;
        for (NodeNN n : children.values()) {
            if (n.getScore()>max) {
                max = n.getScore();
                res = n;
            }
        }
        return res;
    }

    /**
     * Generate the node, i.e. it sets all the necessary values (game evaluation, N, first update of puct, and numToDeploy
     * in the case of the first deploy of the turn for the player)
     */
    public void generate() {

        Matrix eval = new Matrix(1,2);

        // If the player is "victory", then the parent player has won
        if (player.compareTo("victory")==0) {
            switch (getParent().getPlayer().getColor()) {
                case "Red":
                    eval.set(0,0,1);
                    eval.set(0,0,0);
                    break;
                case "Blue":
                    eval.set(0,0,0);
                    eval.set(0,0,1);
                    break;
            }

        }
        else {
            if (getPhase().compareTo("deploy") == 0 && getNumToDeploy() < 0) {
                setNumToDeploy(getPlayer().getNumDeploy());
                playCards();
            }
            eval = eval();
            setN(1);
        }
        updatePUCT(eval);
        if (player.compareTo("victory")==0) {
            // Save the data
            saveData(eval);
        }
        else {
            //Continue the generation of the tree
            setChildren(generateChildren(maxChild, getNumToDeploy()));
        }
    }

    public void updatePUCT(Matrix eval) {
        int i = (player.compareTo("Blue")==0) ? 1 : 0;
        setWins(getWins()+eval.getArray()[0][i]);
        // We update the puct value of the parent if it is not the initial root
        if (getParent()!=null) {
            puct();
            getParent().updatePUCT(eval);
        }

    }

    public void saveData(Matrix eval) {

        // Save data only if the parent is not a ChanceNodeNN because otherwise these data have no sense
        if (!(getParent() instanceof ChanceNodeNN)) {
            // If this node is not the last node (victory node)
            // We update targetA and save the data
            if (!getChildren().isEmpty()) {
                for (Integer i : getChildren().keySet()) {
                    // If one of the child has been an unpredicted root, we do not taking it in account
                    if (i>=0) {
                        getTargetA().set(0, i, getChildren().get(i).getScore());
                    }
                }
                aim.saveInOutData(inputG,targetA, getPhase());
            }
            aim.saveInOutData(inputG,eval, "evaluation");
        }
        if (getParent()!=null) {
            getParent().saveData(eval);
        }
    }

    /**
     * Update the score of the node according to the number of wins, N, N of the parent and the prior probability
     */
    public void puct() {
        if (getPriorP()>=0) {
            setScore(getWins()/getN() + getPriorP() * AgentMCNN.C * (Math.sqrt(getParent().getN())/getN()));
        }
    }

    /**
     * In case of a new turn, the agent will play its best cards if it can
     */
    public void playCards() {

        ArrayList<Card> goodCards = Card.chooseCards(getPlayer().getDeck(), getPlayer());
        int goodCardsValue = Card.count(goodCards, getPlayer());
        if (goodCardsValue > 0) {
            PlayCards pc = new PlayCards(goodCards, getPlayer());
            setNumToDeploy(getNumToDeploy() + Card.countOnlyCombo(goodCards, getPlayer()));
            for (Deployment d : pc.autoDeploy()) {
                try {
                    d.perform(getPlayer());
                } catch (SimulationRunningException | IllegalActionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public AIManager getAim() {
        return aim;
    }

    public void setAim(AIManager aim) {
        this.aim = aim;
    }

    public Matrix getInputG() {
        return inputG;
    }

    public void setInputG(Matrix inputG) {
        this.inputG = inputG;
    }

    public Matrix getTargetA() {
        return targetA;
    }

    public void setTargetA(Matrix targetA) {
        this.targetA = targetA;
    }

    public HashMap<Integer, NodeNN> getChildren() {
        return children;
    }

    public void setChildren(HashMap<Integer, NodeNN> children) {
        this.children = children;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game, Agent player) throws IOException, URISyntaxException {
        this.game = game;
        children.clear();
        this.player = player.getColor();
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public int getN() {
        return N;
    }

    public void setN(int n) {
        N = n;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Agent getPlayer() {
        return this.getGame().getPlayers().get(this.player);
    }

    public Agent getPlayer(Game game) {
        return game.getPlayers().get(this.player);
    }

    public void setPlayer(Agent player) {
        this.player = player.getColor();
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public NodeNN getParent() {
        return parent;
    }

    public void setParent(NodeNN parent) {
        this.parent = parent;
    }

    public int getNumToDeploy() {
        return numToDeploy;
    }

    public void setNumToDeploy(int numToDeploy) {
        this.numToDeploy = numToDeploy;
    }

    public boolean isPick() {
        return pick;
    }

    public void setPick(boolean pick) {
        this.pick = pick;
    }

    public String nextPlayer(Game game) {
        String[] players = game.getPlayers().keySet().toArray(new String[3]);
        int index=0;
        while (index<3 && players[index].compareTo(player)!=0) {
            index++;
        }
        if (index==3) {
            System.out.println("Player not found");
        }
        if (!game.getPlayers().get(players[(index+1)%3]).getTiles().isEmpty()) {
            return players[(index+1)%3];
        }
        if (!game.getPlayers().get(players[(index+2)%3]).getTiles().isEmpty()) {
            return players[(index+2)%3];
        }
        return "victory";
    }

    public double getWins() {
        return wins;
    }

    public void setWins(double wins) {
        this.wins = wins;
    }

    public double getPriorP() {
        return priorP;
    }

    public void setPriorP(double priorP) {
        this.priorP = priorP;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String stringHashing() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            ByteBuffer bb = ByteBuffer.allocate(getInputG().getRowPackedCopy().length * 8);
            for(double d : getInputG().getRowPackedCopy()) {
                bb.putDouble(d);
            }
            byte[] matrixBytes = bb.array();
            byte[] strBytes = getPhase().getBytes(StandardCharsets.UTF_8);
            byte[] boolBytes = new byte[] { (byte) (isPick() ? 1 : 0) };
            byte[] combined = new byte[matrixBytes.length + strBytes.length + boolBytes.length];
            System.arraycopy(matrixBytes, 0, combined, 0, matrixBytes.length);
            System.arraycopy(strBytes, 0, combined, matrixBytes.length, strBytes.length);
            System.arraycopy(boolBytes, 0, combined, matrixBytes.length + strBytes.length, boolBytes.length);
            byte[] hash = digest.digest(combined);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
