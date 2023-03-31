package org.tovivi.nn;


import Jama.Matrix;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class MultiLayerPerceptron {

    private String name;

    private ArrayList<Layer> layers = new ArrayList<>();

    public MultiLayerPerceptron(ArrayList<String> activations, String name, String configName) {
        setName(name);
        setLayers(loadLayers(configName, activations));
    }

    public ArrayList<Layer> loadLayers(String configName, ArrayList<String> activations) {

        ArrayList<Layer> res = new ArrayList<>();
        for (int i=0; i<activations.size(); i++) {
            String path = configName + "_models/" + getName() + "/layer_"+i + "/";
            res.add(new Layer(path, activations.get(i)));
        }
        return res;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Layer> getLayers() {
        return layers;
    }

    public void setLayers(ArrayList<Layer> layers) {
        this.layers = layers;
    }

    @Override
    public String toString() {
        String str = "MultiLayerPerceptron{" + getName() + ":";
        for (Layer l : layers) {
            str += l.toString();
        }
        return str + "}";
    }

    /**
     * give a output prediction according to the input matrix
     * @param X the input matrix (suppose to have the shape that fits the shape of the first layer)
     * @return the matrix of the prediction
     */
    public Matrix predict(Matrix X) {
        Matrix R = new Matrix(X.getArray());
        for (Layer l : getLayers()) {
            R = l.predictLayer(R);
        }
        return R;
    }
}
