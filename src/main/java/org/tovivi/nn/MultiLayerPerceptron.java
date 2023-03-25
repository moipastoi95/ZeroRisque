package org.tovivi.nn;


import Jama.Matrix;
import org.apache.commons.math3.analysis.differentiation.UnivariateDifferentiableFunction;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class MultiLayerPerceptron {

    private ArrayList<Integer> shape;

    private ArrayList<String> activations;

    private String name;

    private ArrayList<Layer> layers = new ArrayList<>();

    public MultiLayerPerceptron(ArrayList<Integer> shape, ArrayList<String> activations, String name, String configName) {
        setShape(shape);
        setActivations(activations);
        setName(name);
        setLayers(loadLayers(configName));
    }

    public ArrayList<Layer> loadLayers(String configName) {

        ArrayList<Layer> res = new ArrayList<>();
        for (int i=0; i<getActivations().size(); i++) {
            String path = configName + "_models/" + getName() + "/layer_"+i + "/";
            res.add(new Layer(path));
        }
        return res;
    }

    /**
     * Function that apply "act" on the matrix X
     * @param act activation function
     * @param X matrix on which to apply the function
     */
    public static void apply (UnivariateDifferentiableFunction act, Matrix X) {

        for (int i=0; i<X.getRowDimension(); i++) {
            X.set(i, 0, (act.value(X.get(i, 0))));
        }
    }

    public ArrayList<Integer> getShape() {
        return shape;
    }

    public void setShape(ArrayList<Integer> shape) {
        this.shape = shape;
    }

    public ArrayList<String> getActivations() {
        return activations;
    }

    public void setActivations(ArrayList<String> activations) {
        this.activations = activations;
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
        return "MultiLayerPerceptron{" +
                "shape=" + shape +
                ", activations=" + activations +
                ", name='" + name + '\'' +
                '}';
    }
}
