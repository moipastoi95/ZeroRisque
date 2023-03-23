package org.tovivi.agent;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;
import org.tovivi.environment.Card;
import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;
import org.tovivi.environment.action.*;
import org.tovivi.environment.action.exceptions.IllegalActionException;
import org.tovivi.environment.action.exceptions.SimulationRunningException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;

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

        int troopsCard = 0; MultiDeploy playCards = new MultiDeploy();
        if (!Card.getAllSets(this).isEmpty()) {
            playCards = getPlayCards();
            if (playCards!=null) troopsCard = Card.countOnlyCombo(((PlayCards) playCards.getDeploys().get(0)).getCards(), this);
        }
        response = false;
        support.firePropertyChange("realDeploy",0, getNumDeploy()+troopsCard);

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
        support.firePropertyChange("realAttack",0, 1);
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
        response = false;
        support.firePropertyChange("realFortify",0,1);
        while (!response) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return (Fortify) action;
    }

    @Override
    public Fortify getFortify(Tile fromTile, Tile toTile) {
        response = false;
        support.firePropertyChange("realFortifyAfterAttack",fromTile, toTile);
        while (!response) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return (Fortify) action;
    }

    @Override
    public MultiDeploy getPlayCards() {
        response = false;
        support.firePropertyChange("realPlayCards",0,Card.getAllSets(this));
        while (!response) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return (MultiDeploy) action;
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
