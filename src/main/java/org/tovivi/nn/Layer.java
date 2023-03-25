package org.tovivi.nn;

import Jama.Matrix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

public class Layer {

    private Matrix weights;

    private Matrix bias;

    public Layer(String path) {
        try {
            loadLayer(path);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    public Layer(Matrix weights, Matrix bias) {
        this.weights = weights;
        this.bias = bias;
    }

    public void loadLayer(String path) throws URISyntaxException {
        File weightsFile = new File(Layer.class.getResource(path + "weights").toURI());
        File biasFile = new File(Layer.class.getResource(path + "bias").toURI());
        File[] files = {weightsFile, biasFile};
        ArrayList<double[]> weightsArr = new ArrayList<>();
        ArrayList<double[]> biasArr = new ArrayList<>();

        for (int i=0; i<files.length; i++) {
            try (BufferedReader br = new BufferedReader(new FileReader(files[i]))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // Split the line into an array of doubles
                    String[] parts = line.trim().split(",");
                    double[] row = Arrays.stream(parts).mapToDouble(Double::parseDouble).toArray();
                    if (i==0) weightsArr.add(row);
                    else biasArr.add(row);
                }
                ArrayList<double[]> rows;
                if (i==0) rows = weightsArr;
                else rows = biasArr;

                int numRows = rows.size();
                int numCols = rows.get(0).length;
                double[][] data = new double[numRows][numCols];
                for (int j = 0; j < numRows; j++) {
                    data[j] = rows.get(j);
                }
                if (i==0) setWeights(new Matrix(data));
                else setBias(new Matrix(data));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Matrix getWeights() {
        return weights;
    }

    public void setWeights(Matrix weights) {
        this.weights = weights;
    }

    public Matrix getBias() {
        return bias;
    }

    public void setBias(Matrix bias) {
        this.bias = bias;
    }
}
