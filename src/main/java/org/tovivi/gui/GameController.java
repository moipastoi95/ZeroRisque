package org.tovivi.gui;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import org.tovivi.agent.Agent;
import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    @FXML
    private AnchorPane world ;

    private static Game game ;

    private static PropertyChangeListener pcl = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            //New Occupier : called if a tile of the continent has been claimed by a new player
            if (evt.getPropertyName().compareTo("newOccupier")==0) {
                Tile changedT = (Tile) evt.getSource();
            }
        }
    };

    public static void setGame(Game game) {
        GameController.game = game;
        for (Tile t : game.getTiles().values()) {
            t.addPropertyChangeListener(pcl);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        System.out.println();

    }
}
