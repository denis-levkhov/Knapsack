package diploma.runners;

import java.io.*;
import java.util.*;

public class QkpDataIO {

    public static void saveInstance(int[] weights, int[][] P, String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            int n = weights.length;
            writer.write(n + "\n");

            for (int w : weights) {
                writer.write(w + " ");
            }
            writer.write("\n");

            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    writer.write(P[i][j] + " ");
                }
                writer.write("\n");
            }
        }
    }

    public static LoadedInstance loadInstance(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            int n = Integer.parseInt(reader.readLine());
            int[] weights = Arrays.stream(reader.readLine().trim().split(" "))
                    .mapToInt(Integer::parseInt).toArray();

            int[][] P = new int[n][n];
            for (int i = 0; i < n; i++) {
                String[] parts = reader.readLine().trim().split(" ");
                for (int j = 0; j < n; j++) {
                    P[i][j] = Integer.parseInt(parts[j]);
                }
            }
            return new LoadedInstance(weights, P);
        }
    }

    public static class LoadedInstance {
        public final int[] weights;
        public final int[][] P;

        public LoadedInstance(int[] weights, int[][] P) {
            this.weights = weights;
            this.P = P;
        }
    }

    public static void main(String[] args) throws IOException {
        Random random = new Random(123);
        int n = 20;
        int[] weights = new int[n];
        int[][] P = new int[n][n];

        for (int i = 0; i < n; i++) {
            weights[i] = 1 + random.nextInt(50);
            for (int j = i; j < n; j++) {
                int val = random.nextInt(50);
                P[i][j] = P[j][i] = val;
            }
        }

        saveInstance(weights, P, "instance_20.txt");
        LoadedInstance loaded = loadInstance("instance_20.txt");

        System.out.println("Loaded " + loaded.weights.length + " weights and matrix of size " + loaded.P.length);
    }
}