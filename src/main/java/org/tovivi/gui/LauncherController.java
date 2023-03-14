package org.tovivi.gui;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import org.tovivi.agent.Agent;
import org.tovivi.environment.Game;

public class LauncherController implements Initializable {

    final private static String agentPackage = "org.tovivi.agent.";
    @FXML
    private ComboBox<Integer> playclock;
    @FXML
    private ComboBox<String> blue;
    @FXML
    private ComboBox<String> red;
    @FXML
    private Spinner<Integer> territories;
    @FXML
    private TextField troops;

    /**
     * Called to initialize a controller after its root element has been completely processed
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Set the agents
        red.setItems(FXCollections.observableArrayList("RandomAgent"));
        red.setValue("RandomAgent");
        blue.setItems(FXCollections.observableArrayList("RandomAgent"));
        blue.setValue("RandomAgent");

        // Set playclock value
        playclock.setItems(FXCollections.observableArrayList(10, 60, 120));
        playclock.setValue(120);

        // Set the number of territories and troops according to the troops factor
        territories.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 21, 14));
        troops.setPromptText(String.valueOf(territories.getValue() * Game.TROOPS_FACTOR));

        // binding the troops value to the number of territories
        territories.valueProperty().addListener((observableValue, integer, t1) -> troops.setPromptText(String.valueOf(t1 * Game.TROOPS_FACTOR)));
    }

    /**
     * This function is called when someone click on START GAME on the launcher
     * it initializes the game according to the values in the launcher
     */
    @FXML
    private void launch() {
        try {
            Constructor<?> constr;
            //Instantiate the agents
            constr = Class.forName(agentPackage + red.getValue()).getConstructor(String.class);
            Agent redAgent = (Agent) constr.newInstance("Red");

            constr = Class.forName(agentPackage + blue.getValue()).getConstructor(String.class);
            Agent blueAgent = (Agent) constr.newInstance("Blue");

            ArrayList<Agent> agents = new ArrayList<>();
            agents.add(redAgent);
            agents.add(blueAgent);

            // Create the game according to the inputs

            GameController.setGame(new Game(agents, territories.getValue(), playclock.getValue()));

            //Switching to the game to instantiate it
            App.newConf("ZeroRisque", 1280, 720);
            App.setRoot("game");

        } catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
