package diploma.input;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Scanner;

public class InputData {

    public static class QkpInstance {
        public final int n;
        public final int W;
        public final int[] weights;
        public final int[][] profits;

        public QkpInstance(int n, int W, int[] weights, int[][] profits) {
            this.n = n;
            this.W = W;
            this.weights = weights;
            this.profits = profits;
        }
    }

    public static QkpInstance readQkpFromFile(String path) throws IOException {
        try (Scanner scanner = new Scanner(new File(path))) {
            scanner.useLocale(Locale.US);

            scanner.next();
            int n = scanner.nextInt();
            int m = scanner.nextInt();

            int[] weights = new int[n];
            for (int i = 0; i < n; i++) {
                weights[i] = (int) Math.round(scanner.nextDouble());
            }

            int[] linearProfits = new int[n];
            for (int i = 0; i < n; i++) {
                linearProfits[i] = (int) Math.round(scanner.nextDouble());
            }

            int[][] profits = new int[n][n];
            for (int i = 0; i < n; i++) {
                profits[i][i] = linearProfits[i];
            }

            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (!scanner.hasNextDouble()) {
                        throw new IOException("Недостаточно значений в матрице взаимодействий");
                    }
                    int interaction = (int) Math.round(scanner.nextDouble());
                    profits[i][j] += interaction;
                    profits[j][i] += interaction;
                }
            }

            int W = (int) Math.round(Arrays.stream(weights).sum() * 0.5);

            return new QkpInstance(n, W, weights, profits);
        }
    }
}