package org.tovivi.agent;

import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;
import org.tovivi.environment.action.*;

import java.util.ArrayList;

public class Legume extends Agent{

    /**
     * Main constructor
     *
     * @param color : String of the color
     * @param game  : ref to the game object
     */
    public Legume(String color, Game mGame) {
        super(color, mGame);
    }

    @Override
    public Actions action() {
        int numTroops = getNumDeploy();

        // deploy all troops on a random tile
        Tile chosenOne = this.getTiles().get((int)(Math.random() * this.getTiles().size()));
        Deploy dep = new Deploy(numTroops, chosenOne);
        ArrayList<Deploy> depL = new ArrayList<>();
        depL.add(dep);
        Deployment deployPart = new Deployment(depL);

        // no attack
        Attack attackPart = new Attack();

        return new Actions(deployPart, attackPart);
    }
}
