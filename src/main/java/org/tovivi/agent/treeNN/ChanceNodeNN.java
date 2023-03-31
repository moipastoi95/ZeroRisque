package org.tovivi.agent.treeNN;

import org.tovivi.agent.Agent;
import org.tovivi.agent.Node;
import org.tovivi.environment.Card;
import org.tovivi.environment.CardType;
import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;
import org.tovivi.environment.action.Attack;
import org.tovivi.nn.AIManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;

import static java.lang.Math.min;

public class ChanceNodeNN extends NodeNN{

    Attack attack ;

    ArrayList<String> results = new ArrayList<>();

    public ChanceNodeNN(Game game, int pos, NodeNN parent, Agent player, String phase, AIManager aim, double priorP, boolean pick, Attack attack) throws IOException, URISyntaxException {
        super(game, pos, parent, player, phase, aim, priorP, -1, pick);
        setAttack(attack);
    }

    public NodeNN simAttack() throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        String result = "";
        NodeNN res = null;
        boolean pick = false;

        Game nextGame = new Game(getGame());
        Tile fromTile = nextGame.getTiles().get(attack.getFromTile().getName());
        Tile toTile = nextGame.getTiles().get(attack.getFromTile().getName());

        while(toTile.getNumTroops() != 0 && fromTile.getNumTroops() != 1) {
            // roll the dices
            // dices of the player and the opponent
            ArrayList<Integer> dicesP = new ArrayList<>();
            ArrayList<Integer> dicesO = new ArrayList<>();
            for(int i=0; i<min(fromTile.getNumTroops()-1, 3); i++) {
                dicesP.add((int)(Math.random() * 6));
            }
            for(int i=0; i<min(toTile.getNumTroops(), 2); i++) {
                dicesO.add((int)(Math.random() * 6));
            }

            //order dices
            Collections.sort(dicesP, Collections.reverseOrder());
            Collections.sort(dicesO, Collections.reverseOrder());

            for(int i=0; i<min(dicesP.size(), dicesO.size()); i++) {
                // succeed attack
                if (dicesP.get(i) > dicesO.get(i)) {
                    toTile.setNumTroops(toTile.getNumTroops()-1);
                } else { // failed
                    fromTile.setNumTroops(toTile.getNumTroops()-1);
                }
            }
        }

        // fail the defence
        if (toTile.getNumTroops() == 0) {
            if (attack.getNumTroops()>0) { // if the number of troops has been specified
                int troopsReallyMoved = min(attack.getNumTroops(), fromTile.getNumTroops() - 1);
                fromTile.setNumTroops(fromTile.getNumTroops() - troopsReallyMoved);
                toTile.setOccupier(getPlayer(), troopsReallyMoved);
            }
            else {
                toTile.setOccupier(getPlayer(), 1);
                fromTile.setNumTroops(fromTile.getNumTroops() - 1);
            }
            result = "attacker:";
            pick = true; // If the attacker wins he will pick a card at the end
        }
        else {
            result = "defender";
        }

        result += fromTile.getNumTroops() + ":" + toTile.getNumTroops();

        if (results.contains(result)) {
            return getChildren().get(results.indexOf(result));
        }
        else {
            results.add(result);
            res = new NodeNN(nextGame, results.size()-1, this, getPlayer(nextGame), "attack",
                    getAim(), getScore(), -1, pick);
            getChildren().put(results.size()-1, res);
        }

        return res;
    }

    public NodeNN simPickingCard() throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        String result = "";
        NodeNN res = null;

        Game nextGame = new Game(getGame());

        int stack_size = getGame().getTheStack().size();
        // Random card in the stack to simulate the uncertainty
        int card = (int) Math.floor(stack_size*Math.random());

        // Add the card to the player's deck
        nextGame.getPlayers().get(getPlayer().getColor()).getDeck().add(nextGame.getTheStack().get(card));

        // Record the result
        if (nextGame.getTheStack().get(card).getType() == CardType.JOKER) {
            result = "JOKER";
        }
        else result = nextGame.getTheStack().get(card).getBonusTile().getName();

        // Remove the card from the stack
        nextGame.getTheStack().remove(card);

        // If it was the last card in the stack, reuse the discard pile
        if (nextGame.getTheStack().isEmpty()) {
            nextGame.setTheStack(getGame().getTheDiscardPile());
            nextGame.getTheDiscardPile().empty();
        }

        if (results.contains(result)) {
            return getChildren().get(results.indexOf(result));
        }
        else {
            results.add(result);
            String nextPl = nextPlayer(nextGame);
            // After picking a card it can leads to a ChanceNode if the next player is Grey.
            // Or lead to the next player deploy phase.
            res = (nextPl.compareTo("Grey")!=0) ? new NodeNN(nextGame, results.size()-1, this, nextGame.getPlayers().get(nextPlayer(nextGame)),
                    "deploy", getAim(), getScore(), -1, false) :
                    new ChanceNodeNN(nextGame,results.size()-1,this, nextGame.getPlayers().get(nextPlayer(nextGame)),
                            "deploy", getAim(), getScore(), false, null);
            getChildren().put(results.size()-1, res);
        }
        return res;
    }

    public NodeNN simGreyPlayer() throws IOException, URISyntaxException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        String result = "";
        NodeNN res = null;

        Game nextGame = new Game(getGame());

        // We pick a random tile and deploy all the troops (that's how the grey player moves)
        int rand_index = (int) Math.floor(Math.random()*getPlayer().getTiles().size());

        result = getPlayer().getTiles().get(rand_index).getName();
        getPlayer().getTiles().get(rand_index).setNumTroops(getPlayer().getNumDeploy());

        // If we have already encounter this state, we can reuse it
        if (results.contains(result)) {
            return getChildren().get(results.indexOf(result));
        }
        else {
            results.add(result);
            res = new NodeNN(nextGame, results.size()-1, this, nextGame.getPlayers().get(nextPlayer(nextGame)),
                    "deploy", getAim(), getScore(), -1, false);
            getChildren().put(results.size()-1, res);
        }
        return res;
    }

    /**
     * @return A simulation of a possible node of this chance node
     */
    @Override
    public NodeNN getBestNode() {

        NodeNN res = null;

        try {
            if (getPhase().compareTo("attack")==0) {
                res = simAttack();
            }
            else if (getPlayer().getColor().compareTo("Grey")==0) {
                res = simGreyPlayer();
            }
            else {
                res = simPickingCard();
            }
        } catch (IOException | URISyntaxException | ClassNotFoundException | InvocationTargetException |
                 NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    public Attack getAttack() {
        return attack;
    }

    public void setAttack(Attack attack) {
        this.attack = attack;
    }

    public ArrayList<String> getResults() {
        return results;
    }

    public void setResults(ArrayList<String> results) {
        this.results = results;
    }
}
