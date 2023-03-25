package org.tovivi.gui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.tovivi.agent.Agent;
import org.tovivi.agent.RealAgent;
import org.tovivi.environment.Card;
import org.tovivi.environment.Game;
import org.tovivi.environment.Tile;
import org.tovivi.environment.action.*;

import java.beans.PropertyChangeListener;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class GameController implements Initializable {

    @FXML
    // world is the AnchorPane that gathers all the AnchorPanes associated to the tiles
    private AnchorPane world ;
    @FXML
    private VBox players;
    @FXML
    private ImageView pause ;
    @FXML
    private Label phase ;
    @FXML
    private VBox phaseInf ;
    @FXML
    private ProgressBar time;

    private DoubleProperty timer = new SimpleDoubleProperty(1);
    private Timeline tl ;

    //Game that will be run
    private Game game ;
    private ArrayList<String> selectedTiles = new ArrayList<>();
    private String realAgentTurn = "";
    private String realAgentPhase;
    private MultiDeploy md = new MultiDeploy();

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
            String newC = ((Agent) evt.getNewValue()).getColor();
            String oldC = ((Agent) evt.getOldValue()).getColor();
            Label lOldP = (Label) players.lookup("#territories"+oldC);
            Label lNewP = (Label) players.lookup("#territories"+newC);
            Label lOldP_deploy = (Label) players.lookup("#troops"+oldC);
            Label lNewP_deploy = (Label) players.lookup("#troops"+newC);
            Platform.runLater(() -> {
                fill(changedT, newC); // Change the color
                lOldP.setText(String.valueOf(Integer.valueOf(lOldP.getText())-1)); // Change the number of territories
                lNewP.setText(String.valueOf(Integer.valueOf(lNewP.getText())+1));
                lOldP_deploy.setText((lOldP_deploy.getText().substring(0,lOldP_deploy.getText().indexOf("+")+1) + game.getPlayers().get(oldC).getNumDeploy() + ")"));
                lNewP_deploy.setText((lNewP_deploy.getText().substring(0,lNewP_deploy.getText().indexOf("+")+1) + game.getPlayers().get(newC).getNumDeploy() + ")"));
            });
        }
        if (evt.getPropertyName().compareTo("newNumTroops")==0) {
            try {
                Tile changedT = (Tile) evt.getSource();

                if (!changedT.isInConflict() && game.getGameSpeed()>0) {
                    Thread.sleep(300/game.getGameSpeed());
                    Platform.runLater(() -> {highligth(changedT);});
                }
                while (game.getGameSpeed()<-1) Thread.sleep(50);
                String c = changedT.getOccupier().getColor();
                Label lTroops = (Label) players.lookup("#troops"+c);
                Platform.runLater(() -> {
                    changeNumTroops(changedT, (int) evt.getNewValue());
                    int diff = ((int) evt.getNewValue()) - ((int) evt.getOldValue());
                    lTroops.setText(String.valueOf(Integer.valueOf(lTroops.getText().substring(0,lTroops.getText().indexOf(" "))) + diff) + lTroops.getText().substring(lTroops.getText().indexOf(" ")));
                });
                boolean earn = ((int) evt.getNewValue()) > ((int) evt.getOldValue());
                impact(changedT, earn);

                if (!changedT.isInConflict() && game.getGameSpeed()>0) {
                    Thread.sleep(600/game.getGameSpeed());
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
                VBox pVB = (VBox) players.lookup("#"+p.getColor()+"Cards"); // VBox for the cards
                if (evt.getOldValue()==null) {
                    Card c = (Card) evt.getNewValue();
                    Label l = new Label(c.toString());
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
        if (evt.getPropertyName().compareTo("newTurn")==0) {
            Platform.runLater(() -> {
                Agent pNew = (Agent) evt.getNewValue() ; Agent pOld = (Agent) evt.getOldValue();

                tl.stop();
                timer.setValue(1);
                time.setStyle("-fx-accent: "+pNew.getColor().toLowerCase());
                tl.play();

                if (phaseInf.getChildren().size()>1) {
                    phaseInf.getChildren().remove(1);
                }

                // We unselect potential selections and set variables for the potential real agent
                realAgentTurn = (pNew instanceof RealAgent) ? pNew.getColor() : "";
                realAgentPhase = "";
                unselect();

                phase.setTextFill(Color.valueOf(pNew.getColor()));
                VBox pNewVB = (VBox) players.lookup("#"+pNew.getColor());
                VBox pOldVB = (VBox) players.lookup("#"+pOld.getColor());
                Label l = (Label) pNewVB.getChildren().get(0); l.setFont(Font.font("System", FontWeight.BOLD, 14));
                l = (Label) pOldVB.getChildren().get(0) ; l.setFont(Font.font("System", FontWeight.NORMAL, 13));
            });
        }
        if (evt.getPropertyName().compareTo("newPhase")==0) {
            Platform.runLater(() -> {
                unselect();
                phase.setText((String) evt.getNewValue());
            });
        }
        if (evt.getPropertyName().compareTo("realDeploy")==0) {
            Platform.runLater(() -> {
                realAgentPhase = "Deployment";
                deploymentInit((Agent) evt.getSource(),(int) evt.getNewValue());
            });
        }
        if (evt.getPropertyName().compareTo("realAttack")==0) {
            Platform.runLater(() -> {
                unselect();
                realAgentPhase = "Attacking";
                RealAgent p = (RealAgent) evt.getSource();
                Tile biggestF = biggestTileAtFront(p);
                if (biggestF==null || biggestF.getNumTroops()==1) {
                    p.setAction(null);
                    p.setResponse(true);
                }
                else {
                    attackInit((Agent) evt.getSource());
                }

            });
        }
        if (evt.getPropertyName().compareTo("realFortifyAfterAttack")==0) {
            Platform.runLater(() -> {
                unselect();
                realAgentPhase = "FortifyAfterAttack";
                fortifyInit((Agent) evt.getSource(), (Tile) evt.getOldValue(), (Tile) evt.getNewValue());
            });
        }
        if (evt.getPropertyName().compareTo("realFortify")==0) {
            Platform.runLater(() -> {
                unselect();
                realAgentPhase = "Fortifying";
                RealAgent p = (RealAgent) evt.getSource();
                Tile biggestNF = biggestTileNotAtFront(p);
                if (biggestNF.getNumTroops()==1) {
                    p.setAction(null);
                    p.setResponse(true);
                }
                else {
                    fortifyInit( p, biggestNF, lowestConnexTile(biggestNF));
                }
            });
        }
        if (evt.getPropertyName().compareTo("realPlayCards")==0) {
            Platform.runLater(() -> {
                unselect();
                realAgentPhase = "PlayingCards";
                RealAgent p = (RealAgent) evt.getSource();
                cardsInit(p, (LinkedList<ArrayList<Card>>) evt.getNewValue(), 0);
            });
        }
        if (evt.getPropertyName().compareTo("Winner")==0) {
            Platform.runLater(() -> {
                tl.stop();
                phase.setText((String) evt.getNewValue() + " rules the World");
            });
        }
    };


    /**
     * @param game the game to set
     */
    public void setGame(Game game) {
        this.game = game; game.addPropertyChangeListener(pcl);
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

        tl = new Timeline(new KeyFrame(Duration.seconds(0.1), e -> timer.setValue(timer.getValue()-(0.1/(double) game.getPlayclock()))));
        tl.setCycleCount(Animation.INDEFINITE);
        time.progressProperty().bindBidirectional(timer);

        for (Tile t : game.getTiles().values()) {
            fill(t, t.getOccupier().getColor());
            changeNumTroops(t, t.getNumTroops());

            Node n = world.lookup("#"+t.getName());
            n.setOnMouseClicked(mouseEvent -> select(t));
        }
        for (Agent p : game.getPlayers().values()) {
            Label lTerritories = (Label) players.lookup("#territories"+p.getColor());
            Label lTroops = (Label) players.lookup("#troops"+p.getColor());
            lTerritories.setText(String.valueOf(game.getPlayers().get(p.getColor()).getTiles().size()));
            lTroops.setText(game.getPlayers().get(p.getColor()).getTiles().size() * Game.TROOPS_FACTOR + " (+" + p.getNumDeploy() + ")");
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
            Circle h = new Circle(aT.getLayoutX()+RADIUS, aT.getLayoutY()+RADIUS, RADIUS+STROKE-1);
            h.setFill(new Color(0,0,0,0));
            h.setStrokeWidth(2*STROKE);
            h.setId(aT.getId() + "_highlight");

            h.setStroke(HIGHLIGHT);
            h.setOnMouseClicked(mouseEvent -> select(t));
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
        if (game.getGameSpeed() > -1) {
            try {
                Image i = new Image(getClass().getResource("play.png").toURI().toString());
                pause.setImage(i);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            game.setGameSpeed(-2);
        } else {
            try {
                Image i = new Image(getClass().getResource("pause.png").toURI().toString());
                pause.setImage(i);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            game.setGameSpeed(mem_speed);
        }
    }

    /**
     * Function which manages how to select tiles with the mouse according to the phase and turn
     * @param t
     */
    public void select(Tile t) {
        if (realAgentTurn.compareTo("")!=0) {
            switch (realAgentPhase) {
                case "Deployment":
                    if (t.getOccupier().getColor().compareTo(realAgentTurn)==0) {
                        if (selectedTiles.size()==0) {
                            highligth(t);
                            selectedTiles.add(t.getName());
                            ((Label) phaseInf.lookup("#deployTile")).setText(t.getName());
                        }
                        else {
                            turnOff(game.getTiles().get(selectedTiles.get(0)));
                            selectedTiles.remove(0);
                            select(t);
                        }
                    }
                    break;
                case "Attacking":
                    if (selectedTiles.size()==0) {
                        highligth(t);
                        selectedTiles.add(t.getName());
                        Label l = (Label) phaseInf.lookup("#fromTile");
                        (l).setText(t.getName()); l.setTextFill(Color.valueOf(t.getOccupier().getColor()));
                        select(atTheFront(t));
                    }
                    else if (selectedTiles.size()==1) {
                        highligth(t);
                        selectedTiles.add(t.getName());
                        Label l = (Label) phaseInf.lookup("#toTile");
                        (l).setText(t.getName()); l.setTextFill(Color.valueOf(t.getOccupier().getColor()));
                    }
                    else if (selectedTiles.size()==2) {
                        if (t.getOccupier().getColor().compareTo(realAgentTurn)!=0 && (game.getTiles().get(selectedTiles.get(0)).getNeighbors().contains(t))) {
                            turnOff(game.getTiles().get(selectedTiles.get(1)));
                            selectedTiles.remove(1);
                            select(t);
                        }
                        else if (t.getOccupier().getColor().compareTo(realAgentTurn)==0 && t.getName().compareTo(selectedTiles.get(0))!=0 && atTheFront(t)!=null && t.getNumTroops()>1) {
                            turnOff(game.getTiles().get(selectedTiles.get(1))); turnOff(game.getTiles().get(selectedTiles.get(0)));
                            selectedTiles = new ArrayList<>();
                            select(t);
                        }
                    }
                    break;
                case "Fortifying":
                    if (selectedTiles.size()==0 && Fortify.connexTiles(t).size()>1) {
                        highligth(t);
                        selectedTiles.add(t.getName());
                        Label l = (Label) phaseInf.lookup("#moveFromTile");
                        (l).setText(t.getName()); l.setTextFill(Color.valueOf(t.getOccupier().getColor()));
                        Spinner<Integer> sp = (Spinner<Integer>) phaseInf.lookup("#moveNum");
                        sp.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, t.getNumTroops()-1,t.getNumTroops()-1));
                        select(lowestConnexTile(t));
                    }
                    else if (selectedTiles.size()==1) {
                        highligth(t);
                        selectedTiles.add(t.getName());
                        Label l = (Label) phaseInf.lookup("#moveToTile");
                        (l).setText(t.getName()); l.setTextFill(Color.valueOf(t.getOccupier().getColor()));
                    }
                    else if (selectedTiles.size()==2) {
                        if (t.getName().compareTo(selectedTiles.get(0))==0 || t.getName().compareTo(selectedTiles.get(1))==0) {
                            unselect();
                            Label l = (Label) phaseInf.lookup("#moveFromTile");
                            l.setText("Select a tile...");
                            Spinner<Integer> sp = (Spinner<Integer>) phaseInf.lookup("#moveNum");
                            sp.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1,1));
                            l = (Label) phaseInf.lookup("#moveToTile");
                            l.setText("Select a tile...");

                        }
                        else if (Fortify.connexTiles(game.getTiles().get(selectedTiles.get(0))).contains(t)) {
                            turnOff(game.getTiles().get(selectedTiles.get(1)));
                            selectedTiles.remove(1);
                            select(t);
                        }
                    }
                    break;
                case "FortifyAfterAttack":
                    if (selectedTiles.size()<2) {
                        highligth(t);
                        selectedTiles.add(t.getName());
                    }
                    break;
            }
        }
    }

    /**
     * Create or modify the inputs for the deployment of RealAgent
     * @param p
     * @param numDeploy
     */
    public void deploymentInit(Agent p, int numDeploy) {

        if (phaseInf.getChildren().size()>1 && phaseInf.getChildren().get(1).getId().compareTo("deployInput")!=0) {
            phaseInf.getChildren().remove(1);
        }
        Spinner<Integer> sp; Button b;
        if (phaseInf.getChildren().size()<=1) {

            VBox deployInput = new VBox(); deployInput.setId("deployInput"); deployInput.setAlignment(Pos.CENTER);

            HBox hb = new HBox(); deployInput.getChildren().add(hb); hb.setAlignment(Pos.CENTER); hb.setSpacing(10);

            Label tile = new Label(p.getTiles().get(0).getName()); tile.setId("deployTile"); hb.getChildren().add(tile);

            sp = new Spinner<>(1,numDeploy,numDeploy); sp.setId("deployTroops");
            sp.setPrefWidth(60); hb.getChildren().add(sp);

            b = new Button("Deploy"); deployInput.getChildren().add(b); b.setId("deploy"); deployInput.setSpacing(5);

            for (Deployment d : md.getDeploys()) {
                if (d instanceof Deploy) {
                    Label dep = new Label(((Deploy) d).toShortString());
                    deployInput.getChildren().add(dep);
                }
            }

            phaseInf.getChildren().add(deployInput);
            select(biggestTileAtFront(p));
        }
        else {
            sp = (Spinner<Integer>) phaseInf.lookup("#deployTroops");
            b = (Button) phaseInf.lookup("#deploy");
        }
        sp.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, numDeploy, numDeploy));
        b.setOnMouseClicked(evt -> deploy(selectedTiles.get(0), sp.getValue(), numDeploy, (RealAgent) p));
    }

    public void deploy(String nameTile, int numTroops, int maxTroops, RealAgent p) {

        Deploy d = new Deploy(numTroops, game.getTiles().get(nameTile));
        md.getDeploys().add(d);
        p.setAction(new MultiDeploy(md.getDeploys()));
        p.setResponse(true);
        phaseInf.getChildren().remove(1);
        md = new MultiDeploy();
    }

    /**
     * Create or modify the inputs for the deployment of RealAgent
     * @param p
     */
    public void attackInit(Agent p) {

        if (phaseInf.getChildren().size()>1 && phaseInf.getChildren().get(1).getId().compareTo("attackInput")!=0) {
            phaseInf.getChildren().remove(1);
        }
        Button bA, bS;
        if (phaseInf.getChildren().size()<=1) {

            Tile front = biggestTileAtFront(p);

            VBox attackInput = new VBox(); attackInput.setId("attackInput"); attackInput.setAlignment(Pos.CENTER); attackInput.setSpacing(10);

            VBox names = new VBox() ; attackInput.getChildren().add(names); names.setSpacing(5); names.setAlignment(Pos.CENTER);

            Label lFromTile = new Label(p.getTiles().get(0).getName()); lFromTile.setId("fromTile"); names.getChildren().add(lFromTile);
            names.getChildren().add(new Label("VS"));
            Label lToTile = new Label(p.getTiles().get(0).getNeighbors().get(0).getName()); lToTile.setId("toTile"); names.getChildren().add(lToTile);

            HBox hb = new HBox(); hb.setSpacing(10); hb.setAlignment(Pos.CENTER); attackInput.getChildren().add(hb);
            bA = new Button("Attack"); hb.getChildren().add(bA); bA.setId("attack");
            bS = new Button("Skip"); hb.getChildren().add(bS); bS.setId("skip");

            phaseInf.getChildren().add(attackInput);
            select(front);
        }
        else {
            bA = (Button) phaseInf.lookup("#attack");
            bS = (Button) phaseInf.lookup("#skip");
        }
        bS.setOnMouseClicked(evt -> attack(null, null, (RealAgent) p));
        if (selectedTiles.size()<2) {
            bA.setOnMouseClicked(evt -> attack(null, null, (RealAgent) p));
        }
        else {
            bA.setOnMouseClicked(evt -> attack(selectedTiles.get(0), selectedTiles.get(1), (RealAgent) p));
        }
    }

    public void attack(String fromTile, String toTile, RealAgent p) {

        if (fromTile==null || toTile==null) {
            p.setAction(null);
            p.setResponse(true);
        }
        else {
            p.setAction(new Attack(game.getTiles().get(fromTile),game.getTiles().get(toTile),0,null,null));
            p.setResponse(true);
        }
        phaseInf.getChildren().remove(1);
    }

    public Tile atTheFront(Tile t) {
        Tile res = null;
        for (Tile n : t.getNeighbors()) {
            if (n.getOccupier().getColor().compareTo(t.getOccupier().getColor())!=0) {
                if (res==null || res.getNumTroops()>n.getNumTroops()) {
                    res = n;
                }
            }
        }
        return res;
    }

    public void fortifyInit(Agent p, Tile fromTile, Tile toTile) {

        if (phaseInf.getChildren().size()>1 && phaseInf.getChildren().get(1).getId().compareTo("moveInput")!=0) {
            phaseInf.getChildren().remove(1);
        }
        Spinner<Integer> sp;
        Button b; Button bS;
        if (phaseInf.getChildren().size()<=1) {
            VBox moveInput = new VBox(); moveInput.setId("moveInput"); moveInput.setAlignment(Pos.CENTER); moveInput.setSpacing(10);

            HBox hb = new HBox(); hb.setSpacing(5); hb.setAlignment(Pos.CENTER); moveInput.getChildren().add(hb);
            Label lFromTile = new Label("MOVE"); hb.getChildren().add(lFromTile);
            sp = new Spinner<>(1,fromTile.getNumTroops()-1,fromTile.getNumTroops()-1); sp.setPrefWidth(60) ;sp.setId("moveNum"); hb.getChildren().add(sp);
            Label lToTile = new Label("FROM"); hb.getChildren().add(lToTile);

            VBox names = new VBox() ; moveInput.getChildren().add(names); names.setSpacing(5); names.setAlignment(Pos.CENTER);

            lFromTile = new Label(fromTile.getName()); lFromTile.setId("moveFromTile"); names.getChildren().add(lFromTile);
            lFromTile.setTextFill(Color.valueOf(fromTile.getOccupier().getColor()));
            names.getChildren().add(new Label("TO"));
            lToTile = new Label(toTile.getName()); lToTile.setId("moveToTile"); names.getChildren().add(lToTile);
            lToTile.setTextFill(Color.valueOf(toTile.getOccupier().getColor()));

            HBox hb2 = new HBox(); hb2.setSpacing(10); moveInput.getChildren().add(hb2); hb2.setAlignment(Pos.CENTER);
            b = new Button("Move"); hb2.getChildren().add(b); b.setId("move");
            bS = new Button("Skip"); hb2.getChildren().add(bS); bS.setId("moveSkip");

            phaseInf.getChildren().add(moveInput);
            select(fromTile);
            if (realAgentPhase.compareTo("FortifyAfterAttack")==0) {
                select(toTile);
            }
        }
        else {
            b = (Button) phaseInf.lookup("#move");
            bS = (Button) phaseInf.lookup("#moveSkip");
            sp = (Spinner<Integer>) phaseInf.lookup("#moveNum");
        }
        b.setOnMouseClicked(evt -> move(game.getTiles().get(selectedTiles.get(0)), game.getTiles().get(selectedTiles.get(1)), (RealAgent) p,sp.getValue()));
        bS.setOnMouseClicked(evt -> move(null, null, (RealAgent) p,sp.getValue()));
    }

    public void move(Tile fromTile, Tile toTile, RealAgent p, int numTroops) {
        if (fromTile==null || toTile==null) {
            p.setAction(null);
        }
        else {
            p.setAction(new Fortify(fromTile, toTile, numTroops));
        }
        p.setResponse(true);
        phaseInf.getChildren().remove(1,2);
    }

    public Tile biggestTileNotAtFront(Agent p) {
        int maxTroops = 1;
        Tile res = null;
        for (Tile t : p.getTiles()) {
            if (t.getNumTroops()>maxTroops && atTheFront(t)==null && (Fortify.connexTiles(t).size()>1)) {
                maxTroops = t.getNumTroops();
                res = t;
            }
        }
        //If no tiles which are not at the front have been found, search at the front
        if (res==null) {
            res = biggestTileAtFront(p);
        }
        return res;
    }

    public Tile biggestTileAtFront(Agent p) {
        int maxTroops = 1;
        Tile res = null;
        for (Tile t : p.getTiles()) {
            if (t.getNumTroops()>=maxTroops && atTheFront(t)!=null) {
                maxTroops = t.getNumTroops();
                res = t;
            }
        }
        return res;
    }

    public Tile lowestConnexTile(Tile t) {
        ArrayList<Tile> cT = Fortify.connexTiles(t);
        int min = Integer.MAX_VALUE;
        Tile res = null;
        // We prioritize frontiers to fortify
        for (Tile n : cT) {
            if (n.getNumTroops() < min && atTheFront(n) != null && !n.equals(t)) {
                min = n.getNumTroops();
                res = n;
            }
        }
        if (res==null) {
            for (Tile n : cT) {
                if (n.getNumTroops() < min) {
                    min = n.getNumTroops();
                    res = n;
                }
            }
        }
        return res;
    }

    public void cardsInit(RealAgent p, LinkedList<ArrayList<Card>> sets, int index) {

        if (phaseInf.getChildren().size()>1 && phaseInf.getChildren().get(1).getId().compareTo("cardsInput")!=0) {
            phaseInf.getChildren().remove(1);
        }
        Button bPrev; Button bNext; Button play; Button skip;
        VBox vb;
        if (phaseInf.getChildren().size()<=1) {

            VBox cardsInput = new VBox(); cardsInput.setId("moveInput"); cardsInput.setAlignment(Pos.CENTER); cardsInput.setSpacing(10);

            vb = new VBox(); vb.setSpacing(5); vb.setAlignment(Pos.CENTER); cardsInput.getChildren().add(vb); vb.setId("vBoxCards");
            VBox vb2 = new VBox(); vb2.setSpacing(5); vb2.setAlignment(Pos.CENTER); cardsInput.getChildren().add(vb2);

            HBox hb = new HBox(); hb.setSpacing(10); hb.setAlignment(Pos.CENTER); vb2.getChildren().add(hb);
            bPrev = new Button("Previous"); hb.getChildren().add(bPrev); bPrev.setId("prev");
            if (index==0) bPrev.setDisable(true); else bPrev.setDisable(false);
            bNext = new Button("Next"); hb.getChildren().add(bNext); bNext.setId("next");
            if (index==sets.size()-1) bNext.setDisable(true); else bNext.setDisable(false);

            Label l2 = new Label("Troops : "+ Card.count(sets.get(index), p)); l2.setId("troopsCard"); vb2.getChildren().add(l2);

            HBox hb3 = new HBox(); hb3.setSpacing(10); hb3.setAlignment(Pos.CENTER); vb2.getChildren().add(hb3);
            play = new Button("Play Cards"); hb3.getChildren().add(play); bNext.setId("playCards");
            skip = new Button("Skip"); hb3.getChildren().add(skip); bNext.setId("skipCards");
            if (p.getDeck().size()>=5) skip.setDisable(true); else skip.setDisable(false);

            phaseInf.getChildren().add(cardsInput);
        }
        else {
            bPrev = (Button) phaseInf.lookup("#prev");
            bNext = (Button) phaseInf.lookup("#next");
            play = (Button) phaseInf.lookup("#playCards");
            skip = (Button) phaseInf.lookup("#skipCards");
            vb = (VBox) phaseInf.lookup("#vBoxCards");
        }

        HashMap<String,Integer> hm = Card.value(sets.get(index),p);
        for (Card c : sets.get(index)) {
            String str = (hm.keySet().contains(c.getBonusTile().getName())) ? " (+2)" : "";
            Label l = new Label(c + str);
            vb.getChildren().add(l);
        }

        bPrev.setOnMouseClicked(evt -> cardsInit(p, sets, index-1));
        bNext.setOnMouseClicked(evt -> cardsInit(p, sets, index+1));
        play.setOnMouseClicked(evt -> playCards(sets.get(index), p));
        skip.setOnMouseClicked(evt -> playCards(null, p));
    }

    public void playCards(ArrayList<Card> cards, RealAgent p) {

        if (cards==null) {
            p.setAction(null);
        }
        else {
            PlayCards pc = new PlayCards(cards, p);
            md.getDeploys().add(pc); md.getDeploys().addAll(pc.autoDeploy());
            p.setAction(md);
        }
        p.setResponse(true);
        phaseInf.getChildren().remove(1);
    }

    public void unselect() {
        selectedTiles.forEach(s -> turnOff(game.getTiles().get(s)));
        selectedTiles = new ArrayList<>();
    }



}
