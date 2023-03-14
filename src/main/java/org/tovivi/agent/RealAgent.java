package org.tovivi.agent;

import org.tovivi.environment.Game;
import org.tovivi.environment.action.Actions;
import org.tovivi.environment.action.Attack;
import org.tovivi.environment.action.Deployment;
import org.tovivi.environment.action.Fortify;

public class RealAgent extends Agent {

    public RealAgent(String color, Game game) {
        super(color, game);
    }

    public RealAgent(String color) {
        super(color);
    }

    @Override
    public Actions action() {
        return new Actions();
    }

    @Override
    public Deployment getNextDeploy() {
        // TODO call GUI
        return null;
    }

    @Override
    public Attack getNextAttack() {
        // TODO call GUI
        return null;
    }

    @Override
    public Fortify getFortify() {
        // TODO call GUI
        return null;
    }
}
