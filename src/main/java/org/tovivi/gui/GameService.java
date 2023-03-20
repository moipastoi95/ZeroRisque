package org.tovivi.gui;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.tovivi.environment.Game;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class GameService extends Service<Integer> {

    private GameController gc;

    public GameService(GameController gc) {
        this.gc = gc;
    }

    /**
     * This task manage the game playing in its own thread
     * @return the task associated to the service
     */
    @Override
    protected Task<Integer> createTask() {
        return new Task<>() {
            @Override
            protected Integer call() throws ExecutionException, InterruptedException, TimeoutException {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                gc.getGame().play();
                return 0;
            }
        };
    }
}
