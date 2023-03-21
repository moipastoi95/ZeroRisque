package org.tovivi.gui;

import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.control.Label;

import javafx.scene.text.Font;
import javafx.util.Duration;
import org.tovivi.agent.Agent;
import org.tovivi.environment.Card;
import org.tovivi.environment.CardType;
import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    @FXML
    // world is the AnchorPane that gathers all the AnchorPanes associated to the tiles
    private AnchorPane world ;

    @FXML
    private VBox players;

    @FXML
    private ImageView pause ;

    //Game that will be run
    private Game game ;

    private int mem_speed;

    final static private Color SEA = Color.rgb(220,240,255);

    // Colors of the 3 agents
    final static private Color BLUE = Color.rgb(140,150,255);
    final static private Color RED = Color.rgb(255,150,140);
    final static private Color GREY = Color.rgb(224,224,224);

    final static private RadialGradient HIGHLIGHT = new RadialGradient(
            0.0, 0.0, 0.5, 0.5, 0.7, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, new Color(0.9529, 0.7372, 0.1960, 1.0)),
            new Stop(0.5, new Color(0.9425, 0.9868, 0.5437, 1.0)),
            new Stop(1.0, new Color(1.0, 1.0, 1.0, 1.0)));

    final static private int RADIUS = 40;
    final static private int STROKE = 5;

    // PropertyChangeListener that listens the property changes from notably tiles to update the GUI
    private PropertyChangeListener pcl = evt -> {
        //New Occupier : called if a tile of the continent has been claimed by a new player
        if (evt.getPropertyName().compareTo("newOccupier")==0) {
            Tile changedT = (Tile) evt.getSource();
            Platform.runLater(() -> {
                fill(changedT, ((Agent) evt.getNewValue()).getColor());
            });
        }
        if (evt.getPropertyName().compareTo("newNumTroops")==0) {
            try {
                Tile changedT = (Tile) evt.getSource();

                if (!changedT.isInConflict() && game.getGameSpeed()>0) {
                    Thread.sleep(900/game.getGameSpeed());
                    Platform.runLater(() -> {highligth(changedT);});
                }
                while (game.getGameSpeed()<-1) Thread.sleep(50);

                Platform.runLater(() -> {
                    changeNumTroops(changedT, (int) evt.getNewValue());
                });
                boolean earn = ((int) evt.getNewValue()) > ((int) evt.getOldValue());
                impact(changedT, earn);

                if (!changedT.isInConflict() && game.getGameSpeed()>0) {
                    Thread.sleep(900/game.getGameSpeed());
                    Platform.runLater(() -> {turnOff(changedT);});}
                while (game.getGameSpeed()<-1) Thread.sleep(50);

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        if (evt.getPropertyName().compareTo("inConflictChange")==0) {
            Tile changedT = (Tile) evt.getSource();
            Platform.runLater(() -> {
                if ((boolean) evt.getNewValue()) {
                    highligth(changedT);
                }
                else {
                    turnOff(changedT);
                }
            });
        }
        if (evt.getPropertyName().compareTo("deckChange")==0) {
            Agent p = (Agent) evt.getSource();
            Platform.runLater(() -> {
                VBox pVB = (VBox) players.lookup("#"+p.getColor());
                if (evt.getOldValue()==null) {
                    Card c = (Card) evt.getNewValue();
                    Label l = new Label("    " + c.toString());
                    l.setId((c.getBonusTile().getName()+"Card")); // we had Card to make the difference between tiles id and cards id
                    l.setFont(Font.font(10));
                    pVB.getChildren().add(l);
                }
                else {
                    Card c = (Card) evt.getOldValue();
                    String str = "#" + (c.getBonusTile().getName()+"Card");
                    pVB.getChildren().remove(pVB.lookup(str));
                }
            });
        }
    };

    /**
     * @param game the game to set
     */
    public void setGame(Game game) {
        this.game = game;
        for (Tile t : game.getTiles().values()) {
            t.addPropertyChangeListener(pcl);
        }
        for (Agent p : game.getPlayers().values()) {
            p.addPropertyChangeListener(pcl);
        }
    }

    public Game getGame() {
        return game;
    }

    /**
     * Initialize the controller, notably update the tiles to see which player has which territory
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setGame(LauncherController.game);

        BackgroundFill bf = new BackgroundFill(SEA, null, null);
        world.setBackground(new Background(bf));
        for (Tile t : game.getTiles().values()) {
            fill(t, t.getOccupier().getColor());
            changeNumTroops(t, t.getNumTroops());
        }

        mem_speed = game.getGameSpeed();

        GameService gs = new GameService(this);
        gs.start();
        App.getStage().show();
    }

    /**
     * fill the circle associated to the tile conquered by a player with the color of this player
     * @param t Tile associated to the circle to fill in the GUI
     * @param color color to paint
     */
    public void fill(Tile t, String color) {

        // Searching for the AnchorPane link to Tile t
        AnchorPane aT = (AnchorPane) world.lookup("#"+t.getName());
        if (aT!=null) {
            Circle c = (Circle) aT.getChildren().get(0);
            switch (color) {
                case "Blue":
                    c.setFill(BLUE);
                    break;
                case "Red":
                    c.setFill(RED);
                    break;
                default:
                    c.setFill(GREY);
            }
        }
        else {
            System.out.println(t.getName() + " doesn't exist in the world");
        }
    }

    /**
     * Highlight the circle associated to the Tile, in order to make the game running GUI understandable
     */
    public void highligth(Tile t) {
        // Searching for the AnchorPane link to Tile t and the AnchorPane of the continent (The highlight circle will be added at this level)
        AnchorPane aT = (AnchorPane) world.lookup("#"+t.getName());
        AnchorPane cT = (AnchorPane) world.lookup("#"+t.getContinent().getName());

        if (cT!=null && aT!=null) {
            Circle c = (Circle) aT.getChildren().get(0);
            Circle h = new Circle(aT.getLayoutX()+RADIUS, aT.getLayoutY()+RADIUS, RADIUS+STROKE-1);
            h.setFill(new Color(0,0,0,0));
            h.setStrokeWidth(2*STROKE);
            h.setId(aT.getId() + "_highlight");

            h.setStroke(HIGHLIGHT);
            cT.toFront();
            cT.getChildren().add(h);

        }
        else {
            System.out.println(t.getName() + " or " + t.getContinent().getName() + " doesn't exist in the world");
        }
    }

    /**
     * Turn off the highlight the circle associated to the Tile, in order to make the game running GUI understandable
     * @param t Tile to highlight
     */
    public void turnOff(Tile t) {
        // Searching for the AnchorPane of the continent (The highlight circle will be added at this level)
        AnchorPane cT = (AnchorPane) world.lookup("#"+t.getContinent().getName());

        if (cT!=null) {
            Circle h = (Circle) cT.lookup("#"+t.getName()+"_highlight");
            cT.getChildren().remove(h);
        }
        else {
            System.out.println(t.getContinent().getName() + " doesn't exist in the world");
        }
    }

    /**
     * Change the number of troops printed in the GUI on the Label associated to the Tile
     * @param t Tile associated to the Label to change in the GUI
     * @param numTroops
     */
    public void changeNumTroops(Tile t, int numTroops) {;
        // Searching for the AnchorPane link to Tile t
        AnchorPane aT = (AnchorPane) world.lookup("#"+t.getName());
        if (aT!=null) {
            Label l = (Label) aT.getChildren().get(2);
            l.setText(String.valueOf(numTroops));
        }
        else {
            System.out.println(t.getName() + " doesn't exist in the world");
        }
    }

    public void scale(Label l, double sc) {
        l.setScaleX(sc); l.setScaleY(sc);
    }

    public void impact(Tile t, boolean earn) throws InterruptedException {
        if (game.getGameSpeed()>0) {
            // Searching for the AnchorPane link to Tile t
            AnchorPane aT = (AnchorPane) world.lookup("#"+t.getName());
            Label l = (Label) aT.getChildren().get(2);
            double init_scale = earn ? 3 : 0.3;
            for (int i=0; i<=10; i++) {
                int finalI = i;
                Platform.runLater(() -> {
                    scale(l,init_scale - ((init_scale-1)*finalI/10));
                });
                Thread.sleep(30/game.getGameSpeed());
                while (game.getGameSpeed()<-1) Thread.sleep(50);
            }
        }
    }

    public void pause() {
        if (game.getGameSpeed()>-1) {
            try {
                Image i = new Image(getClass().getResource("play.png").toURI().toString());
                pause.setImage(i);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            game.setGameSpeed(-2);
        }
        else {
            try {
                Image i = new Image(getClass().getResource("pause.png").toURI().toString());
                pause.setImage(i);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            game.setGameSpeed(mem_speed);
        }

    }
}
