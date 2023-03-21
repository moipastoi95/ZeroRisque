package org.tovivi.agent;

import org.tovivi.environment.Game;
import org.tovivi.environment.action.Actions;
import org.tovivi.environment.action.Attack;
import org.tovivi.environment.action.Deployment;
import org.tovivi.environment.action.Fortify;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

public class RealAgent extends Agent {

    private PropertyChangeSupport support = new PropertyChangeSupport(this);

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
        support.firePropertyChange("realDeploy",0, getNumDeploy());
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
