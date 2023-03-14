package org.tovivi.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;
    private static Stage stage;

    @Override
    public void start(Stage stg) throws IOException {
        stage = stg ;
        stage.setTitle("ZeroRisque Launcher");
        stage.setMinHeight(200); stage.setMinWidth(600);
        scene = new Scene(loadFXML("launcher"));
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    static Node getNode(String fxid) {
        
        for (Node n : scene.getRoot().getChildrenUnmodifiable()) {
            System.out.println(n);
        }
        return scene.getRoot().lookup(fxid);
    }

    static void newConf(String title, int w, int h) {
        stage.setTitle(title);
        stage.setMinWidth(w); stage.setMinHeight(h);
        stage.centerOnScreen();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }

}