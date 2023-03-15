package org.tovivi.gui;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.tovivi.environment.Game;

public class GameService extends Service<Integer> {

    private Game game;

    public GameService(Game g) {
        game = g;
    }

    /**
     * This task manage the game playing in its own thread
     * @return the task associated to the service
     */
    @Override
    protected Task<Integer> createTask() {
        return new Task<>() {
            @Override
            protected Integer call() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                game.play();
                return 0;
            }
        };
    }
}
