package org.tovivi.agent;

import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;
import org.tovivi.environment.action.*;
import org.tovivi.environment.action.exceptions.IllegalActionException;
import org.tovivi.environment.action.exceptions.SimulationRunningException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.*;

import static java.lang.Math.*;

public class AgentMonteCarlo extends Agent {

    private Node root;
    private Node actual_node;
    private int nbActions = 0;
    private long timeLimit = 2000;
    private int E = 1;
    private double c = sqrt(4); //Paramètre d'exploration

    public AgentMonteCarlo(String color, Game game) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        super(color, game);
        this.root = new Node(new Game(game), 0, null, this.getDeck(), this, "Deploy");
    }

    public AgentMonteCarlo(String color) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        super(color);
    }

    /**Create a monte carlo agent with the given parameters E and C
     * @param E Multiplied by game's score in the UCT calculus
     * @param c Exploration parameter
     * */
    public AgentMonteCarlo(String color, Game game, int E, double c) throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(color, game);
        this.E = E;
        this.c = c;
        this.root = new Node(new Game(game), 0, null, this.getDeck(), this, "Deploy");
    }

    public AgentMonteCarlo(Agent agent) {
        super(agent);
    }

    public void setGame(Game game) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        super.setGame(game);
    }

    /**
     * Update the game copy that is stored in the root node
     */
    public void setRoot() throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        this.root = new Node(new Game(this.getGame()), 0, null, this.getDeck(), this, "Deploy");
    }

    public Actions action() {
        return new Actions();
    }

    /**
     * Monte Carlo tree search from the root node
     * 0 - Choose the new node from where to explore (traverse function)
     * 1 - From this node, play the game until a certain depth (rollout function) and get the value of the resulting game
     * 2 - Recursively propagate this value to the parents' node (backpropagate function)
     * @return 3 - The best Action to do (As an actuator)*/
    public Actuator actionTest() throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        Actuator new_action;
        ArrayList<Actuator> possible_actions;

        long timeL = timeLimit;

        if(this.root.getPhase().equals("Deploy") || this.root.getPhase().equals("Fortify")){
            timeL = 4*timeLimit;
        }

        long time = System.currentTimeMillis();

        while (System.currentTimeMillis() - time < timeL) {
            //System.out.println(this.best_child());
            this.traverse();

            //System.out.println(this.actual_node.getPhase());
            possible_actions = this.actual_node.getActions();
            //System.out.println(possible_actions);
            ArrayList<Actuator> redundant = new ArrayList<>();
            for (Actuator act : actual_node.getChilds().keySet()) {
                for (Actuator newAct : possible_actions) {
                    if(act != null && newAct != null) {
                        if (act.toString().equals(newAct.toString())) {
                            redundant.add(newAct);
                        }
                    }
                    else if(act == null && newAct == null) redundant.add(null);
                }
            }
            if(nbActions == 0 && possible_actions.contains(null)) possible_actions.remove(null);
            possible_actions.removeAll(redundant);

            int s = possible_actions.size();
            if(s == 0) this.backPropagate(actual_node, 1);
            else {
                new_action = possible_actions.get(0);
                actual_node.generateChilds(new_action);
                double Score;
                Node next_node = actual_node.getNextNode(new_action);
                Score = this.rollout(next_node);
                this.backPropagate(next_node, Score);
                if(s == 1) actual_node.setNoMoreChild();
            }
        }

        //System.out.println("Appel de best child");
        new_action = this.best_child();

        return new_action;
    }

    /**Should return the best deployment to do in the actual situation by calling the function action test*/
    @Override
    public Deployment getNextDeploy(int numToDeploy) throws IOException, URISyntaxException {
        this.nbActions = 0;
        this.root.setN(0);
        this.root.resetScore();
        this.root.setPhase("Deploy");

        try {
            this.root.setGame(new Game(this.getGame()), this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            Actuator actionToPerfom = this.actionTest();
            MultiDeploy action = (MultiDeploy) actionToPerfom;
            for(Deployment dep: action.getDeploys()) {
                if (dep instanceof Deploy) {
                    ((Deploy) dep).setTile(this.getGame().getTiles().get(dep.getTiles().get(0).getName()));
                }
                if (dep instanceof PlayCards) {
                    ((PlayCards) dep).setPlayer(this.getGame().getPlayers().get(((PlayCards) dep).getPlayer().getColor()));
                }
            }
            return (Deployment) actionToPerfom;
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**Should return the best Attack to do in the actual situation by calling the function action test*/
    @Override
    public Attack getNextAttack() {
        this.root.setN(0);
        this.root.resetScore();
        this.root.setPhase("Attack");

        try {
            this.root.setGame(new Game(this.getGame()), this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        try {
            //System.out.println(scoreAct);
            Actuator actionToPerfom = this.actionTest();
            //System.out.println(actionToPerfom);
            if(actionToPerfom == null) return null;
            Attack att = (Attack) actionToPerfom;
            att.setFromTile(this.getGame().getTiles().get(att.getFromTile().getName()));
            att.setToTile(this.getGame().getTiles().get(att.getToTile().getName()));
            this.nbActions += 1;
            return att;
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**Should return the best Fortification to do in the actual situation by calling the function action test*/
    @Override
    public Fortify getFortify() {
        this.root.setN(0);
        this.root.resetScore();
        this.root.setPhase("Fortify");

        try {
            this.root.setGame(new Game(this.getGame()), this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        try {
            //System.out.println(scoreAct);
            Actuator actionToPerfom = this.actionTest();
            //System.out.println(actionToPerfom);
            if(actionToPerfom == null) return null;
            Fortify fort = (Fortify) actionToPerfom;
            fort.setFromTile(this.getGame().getTiles().get(fort.getFromTile().getName()));
            fort.setToTile(this.getGame().getTiles().get(fort.getToTile().getName()));
            return fort;
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Fortify getFortify(Tile fromTile, Tile toTile) {
        return null;
    }

    @Override
    public MultiDeploy getPlayCards() {
        return null;
    }

    /**
     * Traverse the actual tree to find the actual best node to extend from
     */
    public void traverse() {
        actual_node = this.root;
        Actuator act;
        while (actual_node.getNoMoreChild()) {
            act = this.getBestChild(actual_node);
            //if(act == null) System.out.println("null cool");
            this.actual_node = actual_node.getNextNode(act);
        }
    }

    /**
     * Calcul la valeur UCT d'un Node n en fonction du nombre de fois ou il a été visité et du nombre de fois ou son noeud parent
     * a été visité.
     *
     * @param n    : noeud dont on vaut calculer la valeur
     * @return La valeur sous forme d'un double
     */
    public double getUCT(Node n) {
        if (n.getN() == 0) {
            return Double.MAX_VALUE;
        } else {
            return E * (n.getScore() / n.getN()) + c * sqrt(log(n.getParent().getN()) / n.getN());
        }
    }

    /**
     * Return the best child of the family
     *
     * @return The best actual best Action to perform from the node n
     */
    public Actuator getBestChild(Node n) {
        double max = -Double.MAX_VALUE;
        Actuator res = null;
        for (Actuator act : n.getChilds().keySet()) {
            for (Node child : n.getChilds().get(act).keySet()) {
                //System.out.print("Calcul de luct");
                double i = this.getUCT(child);
                if (i > max && child.getN() > 0) {
                    max = i;
                    res = act;
                }
                //System.out.println(" - " + i + " max = " + max + " - Ca s'est bien passé..." + act);
            }
        }
        //if(res != null) System.out.print(" - " + res);
        return res;
    }

    /**"Random" Simulation of the game from the node n ("Random" because the rollout try to randomly choose
     * in the best possibles actions during the simulation which one to perform)*/
    public double rollout(Node n) {
        Random rand = new Random();
        //System.out.println("Début d'un rollout");
        Agent player = n.getPlayer();
        int nbrTour = 0;
        double res;
        try {
            Game simu = new Game(n.getGame());
            Agent gamer = simu.getPlayers().get(n.getPlayer().getColor());
            if(!Objects.equals(gamer.getColor(), this.getColor()))
                nbrTour++;

            res = simu.score(player);
            Node next;
            String phase = n.getPhase();
            int a=nbrTour,i=0;
            while (nbrTour != 2) {
                if(a != nbrTour && i > 6) {
                    a = nbrTour;
                    phase = "Fortify";
                    i=0;
                }
                else i++;

                next = new Node(simu, 0, null, gamer.getDeck(), gamer, phase);

                ArrayList<Actuator> possible_actions = next.getActions();
                //System.out.println(possible_actions);
                int s = possible_actions.size();
                if(s == 0 && Objects.equals(gamer.getColor(), this.getColor())) return 1;
                else if(s == 0) return 0;
                //System.out.println(possible_actions);
                int p = rand.nextInt(s);
                Actuator new_action = possible_actions.get(p/2);
                //System.out.println(new_action)

                if(new_action != null) new_action.perform(gamer);
                res = simu.score(gamer);

                if(Objects.equals(next.getPhase(), "Fortify")) {
                    gamer = next.getOpp();
                    phase = "Deploy";
                    nbrTour++;
                }
                else if(Objects.equals(next.getPhase(), "Attack")){
                    if(new_action == null) phase = "Fortify";
                    else phase = "Attack";
                }
                else if(next.getPhase().equals("Deploy")) phase = "Attack";
            }
        } catch (IOException | URISyntaxException | ClassNotFoundException | InvocationTargetException |
                 NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalActionException |
                 SimulationRunningException e) {
            throw new RuntimeException(e);
        }
        System.out.println("fin du rollout");
        return res/600;
    }

    /**Recursively Backpropagates the value returned by rollout to update all the visited nodes*/
    public void backPropagate(Node n, double result) {
        if (n.getParent() != null) {
            n.addScore(result);
            n.setN(n.getN() + 1);
            if(!Objects.equals(n.getParent().getPlayer().getColor(), n.getPlayer().getColor()))
                backPropagate(n.getParent(), 1-result);
            backPropagate(n.getParent(), result);
        } else {
            n.addScore(result);
            n.setN(n.getN() + 1);
        }
    }

    /**Return the best action to do from the root node*/
    public Actuator best_child() {
        return this.getBestChild(this.root);
    }
}