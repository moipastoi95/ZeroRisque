package org.tovivi.gui;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.tovivi.environment.Game;

public class GameService extends Service<Integer> {

    private Game game;

    public GameService(Game g) {
        game = g;
    }
    @Override
    protected Task<Integer> createTask() {
        return new Task<>() {
            @Override
            protected Integer call() {
                game.play();
                return 0;
            }
        };
    }
}
