package org.tovivi.gui;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import org.tovivi.environment.Game;

import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    @FXML
    private AnchorPane world ;
    private static Game g ;

    public static void setG(Game g) {
        GameController.g = g;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        AnchorPane nA = (AnchorPane) world.getChildren().get(0);
        AnchorPane a = (AnchorPane) nA.getChildren().get(0);
        Circle c = (Circle) a.getChildren().get(0);
        String t = g.getTiles().get("Alaska").getOccupier().getColor();
        System.out.println(t);
        c.setFill(Paint.valueOf(t));

    }
}
