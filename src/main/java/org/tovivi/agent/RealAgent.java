package org.tovivi.agent;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;
import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;
import org.tovivi.environment.action.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

public class RealAgent extends Agent {

    private Actuator action = null;

    private boolean response = false;

    public RealAgent(String color, Game game) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        super(color, game);
    }

    public RealAgent(String color) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, IOException, URISyntaxException {
        super(color);
    }

    public RealAgent(Agent agent) {
        super(agent);
    }

    @Override
    public Actions action() {
        return new Actions();
    }

    @Override
    public Deployment getNextDeploy() {
        response = false;
        support.firePropertyChange("realDeploy",0, getNumDeploy());
        while (!response) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return (Deployment) action;
    }

    @Override
    public Attack getNextAttack() {
        response = false;
        support.firePropertyChange("realAttack",0, 0);
        while (!response) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return (Attack) action;
    }

    @Override
    public Fortify getFortify() {
        // TODO call GUI
        return null;
    }

    @Override
    public Fortify getFortify(Tile fromTile, Tile toTile) {
        // TODO call GUI
        return null;
    }

    public Actuator getAction() {
        return action;
    }

    public void setAction(Actuator action) {
        this.action = action;
    }

    public void setResponse(boolean response) {
        this.response = response;
    }
}
