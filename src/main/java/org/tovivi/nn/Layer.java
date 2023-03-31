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
    private String activationName;


    public Layer(String path, String activationName) {
        try {
            setActivationName(activationName);
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
                else setBias((new Matrix(data)).transpose());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Function that apply "act" on the matrix X
     * @param X matrix on which to apply the function
     */
    public Matrix apply (Matrix X) {

        Matrix R = new Matrix(X.getArray());
        switch (activationName) {
            case "relu":
                R = ActivationFunction.relu(X);
                break;
            case "softmax":
                R = ActivationFunction.softmax(X);
                break;
            default:
                R = ActivationFunction.sigmoid(X);
        }
        return R;
    }

    /**
     * Used to make a forwardPropagation on this layer
     * @param X input Matrix
     * @return The matrix that result from the forward propagation on this layer
     */
    public Matrix predictLayer(Matrix X) {
        Matrix P = new Matrix(X.getArray());
        P = getBias().plus(P.times(getWeights())) ;
        return apply(P);
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

    public String getActivationName() {
        return activationName;
    }

    public void setActivationName(String activationName) {
        this.activationName = activationName;
    }

    @Override
    public String toString() {
        return "Layer{" +
                "shape=" + weights.getRowDimension() + "x" + weights.getColumnDimension() +
                ", activationName='" + activationName + '\'' +
                '}';
    }
}
