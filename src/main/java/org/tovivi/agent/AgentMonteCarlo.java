package org.tovivi.agent;

import org.tovivi.environment.Game;
import org.tovivi.environment.action.Actions;

import java.lang.reflect.InvocationTargetException;

public class AgentMonteCarlo extends Agent {
    public AgentMonteCarlo(String color, Game game) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(color, game);
    }

    @Override
    public Actions action() {
        return null;
    }
}
