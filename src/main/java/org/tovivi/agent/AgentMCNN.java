package org.tovivi.agent;

import org.tovivi.agent.treeNN.ChanceNodeNN;
import org.tovivi.agent.treeNN.NodeNN;
import org.tovivi.environment.Card;
import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;
import org.tovivi.environment.action.*;
import org.tovivi.environment.action.exceptions.IllegalActionException;
import org.tovivi.environment.action.exceptions.SimulationRunningException;
import org.tovivi.nn.AIManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class AgentMCNN extends Agent{

    private AIManager aim;

    final static public double C = Math.sqrt(2);
    private final int seconds = 1;
    private NodeNN root;

    private HashMap<String, NodeNN> visitedNodes = new HashMap<>();

    public AgentMCNN(String color, Game game) throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(color, game);
        aim = new AIManager("config");
        this.root = new NodeNN(new Game(game), -1, null,  this, "deploy", aim,-1, this.getNumDeploy(), false);
        visitedNodes.put(root.stringHashing(), getRoot());
    }

    public AgentMCNN(String color) throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(color);
        aim = new AIManager("config");
    }

    public AgentMCNN(Agent agent) {

        super(agent);
        aim = ((AgentMCNN) agent).getAim();
    }
    @Override
    public Actions action() throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return new Actions();
    }

    @Override
    public Deployment getNextDeploy(int numTroops) throws IOException, URISyntaxException {

        ArrayList<Deployment> depL = new ArrayList<>();
        ArrayList<Card> goodCards = Card.chooseCards(getDeck(), this);
        int goodCardsValue = Card.count(goodCards, this);
        if (goodCardsValue > 0) {
            PlayCards pc = new PlayCards(goodCards, this);
            depL.add(pc);
            depL.addAll(pc.autoDeploy());
        }
        depL.add((Deployment) searchForBestAction("deploy"));
        MultiDeploy deployPart = new MultiDeploy(depL);
        return deployPart;
    }

    @Override
    public Attack getNextAttack() {
        return (Attack) searchForBestAction("attack");
    }

    @Override
    public Fortify getFortify() {
        return (Fortify) searchForBestAction("fortify");
    }

    @Override
    public Fortify getFortify(Tile fromTile, Tile toTile) {
        return null;
    }

    @Override
    public MultiDeploy getPlayCards() {
        return null;
    }

    // TODO fonction qui génère le root s'il n'a pas été généré (pour des raisons de Random gen ou de profondeur trop faible)
    public NodeNN traverse() {

        NodeNN res = root;
        // while the node we are at is a Chance Node Or have Childrens, we can traverse it
        while (res instanceof ChanceNodeNN || !res.getChildren().isEmpty()) {
            res.setN(res.getN()+1);
            res = res.getBestNode();
        }
        return res;
    }

    /**
     *
     * @return The best action after a monte carlo NN search
     */
    public Actuator getBestAction() {

        Actuator res = null;
        double max = Double.MIN_VALUE;
        int maxInd=-1;

        for (Integer i : root.getChildren().keySet()) {
            if (root.getChildren().get(i).getScore()>max) {
                max = root.getChildren().get(i).getScore();
                maxInd=i;
            }
        }
        if (maxInd>=0) {
            switch (root.getPhase()) {
                case "deploy":
                    res = aim.legalDeploy(root.getPlayer(), maxInd, root.getNumToDeploy());
                    break;
                case "attack":
                    if (maxInd < root.getTargetA().getArray()[0].length - 1) {
                        res = aim.legalAttack(root.getPlayer(), maxInd);
                    }
                    break;
                case "fortify":
                    if (maxInd < root.getTargetA().getArray()[0].length - 1) {
                        res = aim.legalFortify(root.getPlayer(), maxInd);
                    }
                    break;
            }
            return res;
        }
        return null;
    }

    public Actuator searchForBestAction(String phase) {

        boolean pick = false;

        // If the current phase is not deploy, then it should be an attack or a fortify from the same player
        // and then the node can inherit the pick value from the root

        if (phase.compareTo("deploy")!=0) {
            pick = root.isPick();
        }

        try {

            NodeNN copyRoot = new NodeNN(root);
            NodeNN newRoot = new NodeNN(getGame(), -1, copyRoot,this,phase,aim,-1,-1,pick);
            String hash = newRoot.stringHashing();
            if (hash.equals(root.stringHashing())) {
                root = visitedNodes.get(hash);
            }
            else if (visitedNodes.containsKey(hash)) {

            }
            else {
                //TODO AJouter un delete des enfants
                // Créer un score en tableau (pour les changements entre les joueurs)
                root = newRoot;
                newRoot.generate();
            }
        } catch (IOException | URISyntaxException | ClassNotFoundException | InvocationTargetException |
                 NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        long startSearching = System.currentTimeMillis();
        System.out.println(root);
        while((System.currentTimeMillis() - startSearching)<(seconds*1000)) {
            //System.out.println("oui");
            NodeNN n = traverse();
            System.out.println(n.getPhase() + " " + n.getScore() + " " + n.getPriorP() + " " + n.getNumToDeploy());
            n.generate();
            visitedNodes.put(n.stringHashing(),n);
            //System.out.println("non");

        }
        return getBestAction();
    }

    @Override
    public void setGame(Game game) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        super.setGame(game);
    }

    public NodeNN getRoot() {
        return root;
    }

    public void setRoot(NodeNN root) {
        this.root = root;
    }

    public void setRoot() throws IOException, URISyntaxException {
        setRoot(new NodeNN(getGame(), -1, null,  this, "deploy", aim,-1, this.getNumDeploy(), false));
        visitedNodes.put(root.stringHashing(), getRoot());
    }

    public AIManager getAim() {
        return aim;
    }

    public void setAim(AIManager aim) {
        this.aim = aim;
    }
}