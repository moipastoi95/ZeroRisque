package org.tovivi.gui;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
        territories.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer t1) {
                troops.setPromptText(String.valueOf(t1 * Game.TROOPS_FACTOR));
            }
        });
    }

    @FXML
    private void launch() throws IOException {
        Constructor<?> constr = null;
        try {
            //Instantiate the agents
            constr = Class.forName(agentPackage+red.getValue()).getConstructor(String.class);
            Agent redAgent = (Agent) constr.newInstance("red") ;

            constr = Class.forName(agentPackage+blue.getValue()).getConstructor(String.class);
            Agent blueAgent = (Agent) constr.newInstance("blue") ;

            // Create the game according to the inputs
            Game g = new Game(blueAgent, redAgent, territories.getValue(), playclock.getValue());

        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
