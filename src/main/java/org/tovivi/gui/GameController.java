package org.tovivi.gui;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.control.Label;

import javafx.stage.WindowEvent;
import org.tovivi.agent.Agent;
import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;
import org.w3c.dom.events.Event;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    @FXML
    // world is the AnchorPane that gathers all the AnchorPanes associated to the tiles
    private AnchorPane world ;

    //Game that will be run
    private Game game ;

    // Colors of the 3 agents
    final static private Color blue = Color.rgb(153,204,255);
    final static private Color red = Color.rgb(255,153,153);
    final static private Color grey = Color.rgb(224,224,224);

    // PropertyChangeListener that listens the property changes from notably tiles to update the GUI
    private PropertyChangeListener pcl = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            //New Occupier : called if a tile of the continent has been claimed by a new player
            if (evt.getPropertyName().compareTo("newOccupier")==0) {
                Tile changedT = (Tile) evt.getSource();
                Platform.runLater(() -> {
                    fill(changedT, ((Agent) evt.getNewValue()).getColor());
                    highligth(changedT);
                });
            }
            if (evt.getPropertyName().compareTo("newNumTroops")==0) {
                Tile changedT = (Tile) evt.getSource();
                Platform.runLater(() -> {changeNumTroops(changedT, (int) evt.getNewValue());});
            }
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
        for (Tile t : game.getTiles().values()) {
            fill(t, t.getOccupier().getColor());
            changeNumTroops(t, t.getNumTroops());
        }
        GameService gs = new GameService(game);
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
                    c.setFill(blue);
                    break;
                case "Red":
                    c.setFill(red);
                    break;
                default:
                    c.setFill(grey);
            }
        }
        else {
            System.out.println(t.getName() + " doesn't exist in the world");
        }
    }

    /**
     * Highlight the circle associated to the Tile, in order to make the game running GUI understandable
     * @param t Tile to highlight
     */
    public void highligth(Tile t) {
        // Searching for the AnchorPane link to Tile t and the AnchorPane of the continent (The highlight circle will be added at this level)
        AnchorPane aT = (AnchorPane) world.lookup("#"+t.getName());
        AnchorPane cT = (AnchorPane) world.lookup("#"+t.getContinent().getName());

        if (aT!=null) {
            Circle c = (Circle) aT.getChildren().get(0);
            Circle h = new Circle(aT.getLayoutX()+40, aT.getLayoutY()+40, 44);
            h.setFill(new Color(0,0,0,0));
            h.setStrokeWidth(c.getStrokeWidth()+5);
            h.setId(aT.getId() + "_highlight");

            Stop[] arr_stops = {(new Stop(0, Color.WHITE)), new Stop(0, Color.valueOf(c.getStroke().toString()))};
            List<Stop> stops = List.of(arr_stops);
            RadialGradient paint = new RadialGradient(
                    0.0, 0.0, 0.5, 0.5, 0.7, true, CycleMethod.NO_CYCLE,
                    new Stop(0.0, new Color(0.7632, 0.609, 0.2191, 1.0)),
                    new Stop(0.5, Color.valueOf(c.getStroke().toString())),
                    new Stop(1.0, new Color(1.0, 1.0, 1.0, 1.0)));
            RadialGradient rg = new RadialGradient(360, 40, 40, 40, 50, true, CycleMethod.NO_CYCLE, stops);
            h.setStroke(paint);
            cT.toFront();
            cT.getChildren().add(h);

        }
        else {
            System.out.println(t.getName() + " doesn't exist in the world");
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
}
