package org.tovivi.agent;

import org.tovivi.environment.*;
import org.tovivi.environment.action.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Random Agent will choose a random tile next to a opponent tile, deploy all troops available and attack.
 * In case of success, it will move all the troops to the captured tile.
 * Finally, it won't fortify one of its tiles.
 */
public class RandomAgent extends Agent{

    /**
     * Typically used to create the neutral player //TODO A enlever quand on aura créer l'agent neutre
     * @param color : String of the color
     * @param game  : ref to the game object
     */
    public RandomAgent(String color, Game game) {
        super(color, game);
    }


    /**
     * Used to create the players before the game, typically the red and blue players
     * @param color : String of the color
     */
    public RandomAgent(String color) {
        super(color);
    }

    public Actions action(int numTroops) {
        // deploy all troops on a random tile
        Tile chosenOne = this.getTiles().get((int) (Math.random() * this.getTiles().size()));
        Deployment dep = new Deploy(numTroops, chosenOne);
        ArrayList<Deployment> depL = new ArrayList<>();
        depL.add(dep);
        return null;
    }

    @Override
    public Actions action() {
        int numTroops = getNumDeploy();

        // get every tile next to an opponent tile, and retrieve opponent's tile next to them
        HashMap<Tile, ArrayList<Tile>> front = new HashMap<>();
        for(Tile t : getTiles()) {
            boolean flag = false;
            ArrayList<Tile> opponentTiles = new ArrayList<>();
            for (Tile neighbor : t.getNeighbors()) {
                if (!neighbor.getOccupier().equals(this)) {
                    flag = true;
                    // add the real ref of tiles
                    opponentTiles.add(getGame().getTiles().get(neighbor.getName()));
                }
            }
            if (flag) {
                front.put(getGame().getTiles().get(t.getName()), opponentTiles);
            }
        }
        // pick a random tiles from the front list
        ArrayList<Tile> frontKeys = new ArrayList<>(front.keySet());
        Tile fromTile = frontKeys.get((int)(Math.random() * frontKeys.size()));

        // if cards owned, use them
        ArrayList<Deployment> depL = new ArrayList<>();
        ArrayList<Card> goodCards = Card.chooseCards(getDeck(), this);
        int goodCardsValue = Card.value(goodCards, this);
        if (goodCardsValue > 0) {
            depL.add(new PlayCards(goodCards, this));
        }
        // deploy all troops on this tile
        depL.add(new Deploy(numTroops+goodCardsValue, fromTile));
        MultiDeploy deployPart = new MultiDeploy(depL);

        // do a simulation to create the attack part
        deployPart.doSimulation();

        // attack a random tile next to the tile chosen
        ArrayList<Tile> frontValue = front.get(fromTile);
        Tile toTile = frontValue.get((int)(Math.random() * frontValue.size()));

        Offensive offensivePart = new Attack(fromTile, toTile, fromTile.getNumTroops()-1, new Fortify(), new Fortify());

        // don't forget to undo the simulation
        deployPart.undoSimulation();

        return new Actions(deployPart, offensivePart);
    }
}

