package org.tovivi.nn;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MultiLayerPerceptron {

    public static void main(String[] args) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec("python3 /home/cytech/Desktop/testTF.py");
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        in.lines().forEach(System.out::println);
    }
}
