package org.tovivi.agent;

import org.tovivi.environment.Game;
import org.tovivi.environment.action.Actions;
import org.tovivi.environment.action.Attack;
import org.tovivi.environment.action.Deployment;
import org.tovivi.environment.action.Fortify;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

public class RealAgent extends Agent {

    public RealAgent(String color, Game game) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        super(color, game);
    }

    public RealAgent(String color) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
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
