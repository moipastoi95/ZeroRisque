package org.tovivi.nn;

import org.tovivi.environment.TextReader;

import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AIManager {

    private String configName;
    private ArrayList<MultiLayerPerceptron> models = new ArrayList<>(); // models[0] = deploy model, models[1] = attack model, models[2] = fortify

    public AIManager(String configName) {
        //TODO
        // -Créer une classe qui à partir d'un Game créer le vecteur, et qui sait écrire les données d'entrainement
        //          ce sera typiquement juste un tableau a ecrire (La donnée d'entrée et de sortie sont sur la même ligne
        // -Implémenter le calcul des predictions
        // -Créer l'agent
        // PENSER A COPIER LE PYTHON A CHAQUE FOIS
        this.configName = configName;
    }

    public void config() throws URISyntaxException, FileNotFoundException {
        File file = new File(AIManager.class.getResource(configName).toURI());
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Skip empty lines and comment lines
                if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
                    // Split the line by the separator ":"
                    String[] parts = line.trim().split(":");
                    // Convert the second part of the line to a list of integers
                    ArrayList<Integer> shape = new ArrayList<>();
                    for (String s : parts[1].trim().split(",")) {
                        shape.add(Integer.parseInt(s));
                    }
                    // Do something with the parts and the list of integers
                    models.add(new MultiLayerPerceptron(shape, new ArrayList<>(Arrays.asList(parts[2].trim().split(","))), parts[0], getConfigName()));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public ArrayList<MultiLayerPerceptron> getModels() {
        return models;
    }

    public void setModels(ArrayList<MultiLayerPerceptron> models) {
        this.models = models;
    }

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        // Create the neural networks according to the config file

        String configName = "config";
        /*
        String mainPath = AIManager.class.getResource("ZeroRisqueNN/main.py").toURI().getPath();
        System.out.println(mainPath);
        Process p = Runtime.getRuntime().exec("python3 " + mainPath + " " + configName);
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        in.lines().forEach(System.out::println);
        */

        // Load the NN according to the config
        AIManager aim = new AIManager(configName);
        aim.config();
        System.out.println(aim.models.get(0).getName() + ":" + aim.models.get(0).getLayers().get(0).getWeights().get(0,0));
    }
}
