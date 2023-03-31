package org.tovivi.nn;

import Jama.Matrix;

public class ActivationFunction {

    // Applies the ReLU activation function element-wise to the input matrix
    public static Matrix relu(Matrix X) {
        double[][] data = X.getArrayCopy();
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                if (data[i][j] < 0) {
                    data[i][j] = 0;
                }
            }
        }
        return new Matrix(data);
    }

    // Applies the softmax activation function to the input matrix along the rows
    public static Matrix softmax(Matrix X) {
        double[][] data = X.getArrayCopy();
        for (int i = 0; i < data.length; i++) {
            double sum = 0;
            for (int j = 0; j < data[0].length; j++) {
                data[i][j] = Math.exp(data[i][j]);
                sum += data[i][j];
            }
            for (int j = 0; j < data[0].length; j++) {
                data[i][j] /= sum;
            }
        }
        return new Matrix(data);
    }

    // Applies the sigmoid activation function element-wise to the input matrix
    public static Matrix sigmoid(Matrix X) {
        double[][] data = X.getArrayCopy();
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[0].length; j++) {
                data[i][j] = 1 / (1 + Math.exp(-data[i][j]));
            }
        }
        return new Matrix(data);
    }

}

