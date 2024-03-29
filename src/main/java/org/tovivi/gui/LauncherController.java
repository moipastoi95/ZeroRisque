package org.tovivi.gui;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.tovivi.agent.Agent;
import org.tovivi.environment.Game;

public class LauncherController implements Initializable {

    final private static String agentPackage = "org.tovivi.agent.";

    protected static Game game ;

    @FXML
    private GridPane particular;
    @FXML
    private ComboBox<String> playclock;
    @FXML
    private ComboBox<String> blue;
    @FXML
    private ComboBox<String> red;
    @FXML
    private Spinner<Integer> territories;
    @FXML
    private ComboBox<String> speed;

    /**
     * Called to initialize a controller after its root element has been completely processed
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Set the agents
        red.setItems(FXCollections.observableArrayList("RandomAgent", "AgentMonteCarlo", "RealAgent", "AgentMCNN"));
        red.setValue("AgentMonteCarlo");
        blue.setItems(FXCollections.observableArrayList("RandomAgent", "AgentMonteCarlo", "RealAgent", "AgentMCNN"));
        blue.setValue("RandomAgent");

        updateParameter();

        // Set playclock value
        playclock.setItems(FXCollections.observableArrayList("120", "60", "30"));
        playclock.setValue("120");

        // Set the number of territories and game speed
        territories.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 21, 14));
        speed.setItems(FXCollections.observableArrayList("1", "3", "15", "MAX"));
        speed.setValue("1");
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
            Agent redAgent = null; Agent blueAgent = null;

            if (red.getValue().compareTo("AgentMonteCarlo")==0) {
                int E = Integer.valueOf(((TextField) particular.lookup("#eSRed")).getText());
                double C = Math.sqrt(Double.valueOf(((TextField) particular.lookup("#cSRed")).getText()));
                long tl = Long.valueOf(((TextField) particular.lookup("#tSRed")).getText());
                constr = Class.forName(agentPackage + red.getValue()).getConstructor(String.class, int.class, double.class, long.class);
                redAgent = (Agent) constr.newInstance("Red", E, C, tl);
            }
            else {
                constr = Class.forName(agentPackage + red.getValue()).getConstructor(String.class);
                redAgent = (Agent) constr.newInstance("Red");
            }

            if (blue.getValue().compareTo("AgentMonteCarlo")==0) {
                int E = Integer.valueOf(((TextField) particular.lookup("#eSBlue")).getText());
                double C = Math.sqrt(Double.valueOf(((TextField) particular.lookup("#cSBlue")).getText()));
                long tl = Long.valueOf(((TextField) particular.lookup("#tSBlue")).getText());
                constr = Class.forName(agentPackage + red.getValue()).getConstructor(String.class, int.class, double.class, long.class);
                blueAgent = (Agent) constr.newInstance("Blue", E, C, tl);
            }
            else {
                constr = Class.forName(agentPackage + blue.getValue()).getConstructor(String.class);
                blueAgent = (Agent) constr.newInstance("Blue");
            }

            ArrayList<Agent> agents = new ArrayList<>();
            agents.add(blueAgent);
            agents.add(redAgent);

            // Create the game according to the inputs
            // Before we look at "speed" which can be equals to MAX)
            int sp ;
            if (speed.getValue().compareTo("MAX")==0) {
                sp = -1;
            }
            else {
                sp = Integer.valueOf(speed.getValue())*2;
            }
            game = new Game(agents, territories.getValue(), Integer.valueOf(playclock.getValue()), sp);

            //Switching to the game to instantiate it
            App.getStage().hide();
            App.newConf("ZeroRisque", 1280, 720);
            App.setRoot("game");

        } catch (NoSuchMethodException | ClassNotFoundException | InvocationTargetException | InstantiationException |
                 IllegalAccessException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateParameter() {
        for (Node n : particular.getChildren()) {
            if (n.getId()!=null) {
                n.setDisable(false);
                if (n.getId().contains("Blue") && blue.getValue().compareTo("AgentMonteCarlo")!=0) {
                    n.setDisable(true);
                }
                if (n.getId().contains("Red") && red.getValue().compareTo("AgentMonteCarlo")!=0) {
                    n.setDisable(true);
                }
                if (!n.isDisable()) {
                    if (n.getId().contains("eS")) {
                        ((TextField) n).setText("1");
                    }
                    if (n.getId().contains("cS")) {
                        ((TextField) n).setText("4");
                    }
                    if (n.getId().contains("tS")) {
                        ((TextField) n).setText("2000");
                    }
                }
            }
        }
    }
}
