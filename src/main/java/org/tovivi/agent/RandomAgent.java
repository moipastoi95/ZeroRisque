package org.tovivi.agent;

import org.tovivi.environment.*;
import org.tovivi.environment.action.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

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
        Tile chosenOne = this.getTiles().get((int)(Math.random() * this.getTiles().size()));
        Deployment dep = new Deploy(numTroops, chosenOne);
        ArrayList<Deployment> depL = new ArrayList<>();
        depL.add(dep);

        // attack from a random tile (containing more than 1 troop)
        // get every tile next to an opponent tile, and retrieve opponent's tile next to them
        HashMap<Tile, ArrayList<Tile>> front = new HashMap<>();
        for(Tile t : getTiles()) {
            if (t.getNumTroops() > 1) {
                boolean flag = false;
                ArrayList<Tile> opponentTiles = new ArrayList<>();
                for (Tile neighbor : t.getNeighbors()) {
                    if (!neighbor.getOccupier().equals(this)) {
                        flag = true;
                        opponentTiles.add(neighbor);
                    }
                }
                if (flag) {
                    front.put(t, opponentTiles);
                }
            }
        }
        // pick a random tiles from the front list
        ArrayList<Tile> frontKeys = new ArrayList<>(front.keySet());
        Tile fromTile = frontKeys.get((int)(Math.random() * frontKeys.size()));

        ArrayList<Tile> frontValue = front.get(fromTile);
        Tile toTile = frontValue.get((int)(Math.random() * frontValue.size()));

        Attack ack = new Conquer(fromTile, toTile, fromTile.getNumTroops()-1, new StopAttack(), new StopAttack());

        Fortification forti = new NotFortify();

        return new Actions(depL, ack, forti);

    }
}

