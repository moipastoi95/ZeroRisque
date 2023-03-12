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
    public Legume(String color, Game game) {
        super(color, game);
    }

    @Override
    public Actions action() {
//        try {
//            Thread.sleep(10000);
//        } catch (Exception e) {
//
//        }
        int numTroops = getNumDeploy();

        // deploy all troops on a random tile
        Tile chosenOne = this.getTiles().get((int)(Math.random() * this.getTiles().size()));
        Deploy dep = new Deploy(numTroops, chosenOne);
        ArrayList<Deployment> depL = new ArrayList<>();
        depL.add(dep);
        MultiDeploy deployPart = new MultiDeploy(depL);

        // no attack
        Attack attackPart = new Attack();

        return new Actions(deployPart, attackPart);
    }
}
