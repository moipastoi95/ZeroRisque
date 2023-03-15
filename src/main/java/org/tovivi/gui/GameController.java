package org.tovivi.gui;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
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
import java.util.ResourceBundle;

public class GameController implements Initializable {

    @FXML
    private AnchorPane world ;
    private Game game ;
    final static private Color blue = Color.rgb(153,204,255);
    final static private Color red = Color.rgb(255,153,153);
    final static private Color grey = Color.rgb(224,224,224);

    private PropertyChangeListener pcl = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            //New Occupier : called if a tile of the continent has been claimed by a new player
            if (evt.getPropertyName().compareTo("newOccupier")==0) {
                Tile changedT = (Tile) evt.getSource();
                Platform.runLater(() -> {fill(changedT, ((Agent) evt.getNewValue()).getColor());});
            }
            if (evt.getPropertyName().compareTo("newNumTroops")==0) {
                Tile changedT = (Tile) evt.getSource();
                Platform.runLater(() -> {changeNumTroops(changedT, (int) evt.getNewValue());});
            }
        }
    };

    public void setGame(Game game) {
        this.game = game;
        for (Tile t : game.getTiles().values()) {
            t.addPropertyChangeListener(pcl);
        }
    }

    public Game getGame() {
        return game;
    }

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

    public void fill(Tile t, String color) {;
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

    public void changeNumTroops(Tile t, int numTroops) {;
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
