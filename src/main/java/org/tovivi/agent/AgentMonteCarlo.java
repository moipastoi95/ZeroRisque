package org.tovivi.agent;

import org.tovivi.environment.Card;
import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;
import org.tovivi.environment.action.*;
import org.tovivi.environment.action.exceptions.IllegalActionException;
import org.tovivi.environment.action.exceptions.SimulationRunningException;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.*;

import static java.lang.Math.*;

public class AgentMonteCarlo extends Agent {

    private Node root;

    private Node actual_node;

    private int E = 10;
    private double c = sqrt(2); //Paramètre d'exploration
    private int depth = 0; //Profondeur actuelle de la recherche

    public AgentMonteCarlo(String color, Game game) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        super(color, game);
        this.root = new Node(new Game(game), 0, null, this.getDeck(), this, "Deploy");
    }

    public AgentMonteCarlo(String color) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        super(color);
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

    //TODO: while(ressource_left):
    // 0 - Choisir le nouveau noeud à partir duquel explorer (fonction traverse)
    // 1 - A partir de ce noeud parcourir l'arbre jusqu'à un état final (fonction rollout)
    // 2 - Calculer la valeur de l'état final (en fonction de qui à gagné)
    // 3 - Propager la valeur de manière récursive sur les noeuds parents (fonction backpropagate)
    // 4 - Renvoyer le meilleur child
    public Actuator actionTest() throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        Random rand = new Random();
        Actuator new_action;
        ArrayList<Actuator> possible_actions;

        while (depth < 1) {
            depth = 0;
            this.traverse();

            possible_actions = this.actual_node.getActions();

            ArrayList<Actuator> redundant = new ArrayList<>();
            for (Actuator act : actual_node.getChilds().keySet()) {
                for (Actuator newAct : possible_actions) {
                    if (act.toString().equals(newAct.toString())) {
                        redundant.add(newAct);
                    }
                }
            }

            possible_actions.removeAll(redundant);

            int s = possible_actions.size();
            new_action = possible_actions.get(rand.nextInt(s));

            actual_node.generateChilds(new_action);
            Node next_node = actual_node.getNextNode(new_action);
            int Score = this.rollout(next_node);
            this.backPropagate(next_node, Score);

            if(s == 1) actual_node.setNoMoreChild();
        }

        new_action = this.best_child();

        this.depth = 0;

        return new_action;
    }

    @Override
    public Deployment getNextDeploy(int numTroops) throws IOException, URISyntaxException {
        this.root.setN(0);
        this.root.resetScore();
        this.root.setPhase("Deploy");
        depth = 0;

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

    @Override
    public Attack getNextAttack() {
        this.root.setN(0);
        this.root.resetScore();
        this.root.setPhase("Attack");
        depth = 0;

        try {
            this.root.setGame(new Game(this.getGame()), this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            Actuator actionToPerfom = this.actionTest();
            Attack att = (Attack) actionToPerfom;
            att.setFromTile(this.getGame().getTiles().get(att.getFromTile().getName()));
            att.setToTile(this.getGame().getTiles().get(att.getToTile().getName()));
            return (Attack) att;
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Fortify getFortify() {
        return null;
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
     * Parcours l'arbre actuel jusqu'au meilleur leaf node actuel
     *
     * @return le meilleur noeud feuille
     */
    public void traverse() throws IOException, URISyntaxException {
        actual_node = this.root;
        Actuator act = null;
        while (actual_node.getNoMoreChild()) {
            depth += 1;
            act = this.getBestChild(actual_node);
            this.actual_node = actual_node.getNextNode(act);
        }
    }

    /**
     * Calcul la valeur UCT d'un Node n en fonction du nombre de fois ou il a été visité et du nombre de fois ou son noeud parent
     * a été visité.
     *
     * @param n    : noeud dont on vaut calculer la valeur
     * @param prob
     * @return La valeur sous forme d'un double
     */
    public double getUCT(Node n, Double prob) {
        if (n.getN() == 0) {
            return Double.MAX_VALUE;
        } else {
            return E * prob * (n.getScore() / n.getN()) + c * sqrt(log(n.getParent().getN()) / n.getN());
        }
    }

    /**
     * Renvoie le meilleur enfant de la famille
     *
     * @return Le meilleur noeud
     */
    public Actuator getBestChild(Node n) {
        double max = -Double.MAX_VALUE;
        Actuator res = null;
        for (Actuator act : n.getChilds().keySet()) {
            for (Node child : n.getChilds().get(act).keySet()) {
                //System.out.print("Calcul de luct");
                double i = this.getUCT(child, n.getChilds().get(act).get(child));
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

    //TODO: Doit renvoyer la valeur du leaf node atteint à partir du noeud n
    // Pour ce faire, parcours le jeu de manière aléatoire
    // Pour le moment c'est de type très très nul mais oklm
    public int rollout(Node n) {
        Agent player = n.getPlayer();
        Random rand = new Random();
        int res = 0;
        try {
            Game simu = new Game(n.getGame());
            Agent gamer = simu.getPlayers().get(player.getColor());
            res = simu.score(player);
            for (int i = 0; i < 0; i++) {
                Node next = new Node(simu, 0, null, gamer.getDeck(), gamer, "Attack");
                ArrayList<Actuator> possible_actions = next.getActions();
                int s = possible_actions.size();
                Attack new_action = (Attack) possible_actions.get(rand.nextInt(s));

                new_action.perform(gamer);
                res = simu.score(gamer);
            }
        } catch (IOException | URISyntaxException | ClassNotFoundException | InvocationTargetException |
                 NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalActionException |
                 SimulationRunningException e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
        return res;
    }

    //TODO: Doit propager la valeur du noeud final à tous les noeuds précedents déjà exploré dans l'arbre
    public void backPropagate(Node n, int result) {
        if (n.getParent() != null) {
            n.addScore(result);
            n.setN(n.getN() + 1);
            backPropagate(n.getParent(), result);
        } else {
            n.addScore(result);
            n.setN(n.getN() + 1);
        }
    }

    //TODO: Doit renvoyer le meilleur noeuds, genre l'action que l'algo doit renvoyer en gros
    public Actuator best_child() {
        return this.getBestChild(this.root);
    }
}