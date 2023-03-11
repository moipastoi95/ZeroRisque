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
     * Main constructor
     *
     * @param color : String of the color
     * @param mGame  : ref to the game object
     */
    public RandomAgent(String color, Game mGame) {
        super(color, mGame);
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

        // deploy all troops on this tile
        Deploy dep = new Deploy(numTroops, fromTile);
        ArrayList<Deploy> depL = new ArrayList<>();
        depL.add(dep);
        Deployment deployPart = new Deployment(depL);

        // attack a random tile next to the tile chosen
        ArrayList<Tile> frontValue = front.get(fromTile);
        Tile toTile = frontValue.get((int)(Math.random() * frontValue.size()));

        Offensive offensivePart = new Attack(fromTile, toTile, fromTile.getNumTroops()-1, new Fortify(), new Fortify());

        return new Actions(deployPart, offensivePart);
    }
}

