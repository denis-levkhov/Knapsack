package diploma.solverExample;

import java.util.ArrayList;
import java.util.List;

public class DpExample {

    static class Item {
        int index;
        int weight;
        int value;

        Item(int index, int weight, int value) {
            this.index = index;
            this.weight = weight;
            this.value = value;
        }

        public static void main(String[] args) {
            int n = 3;
            int maxWeight = 4;

            int[] weights = {2, 3, 1};
            int[] values = {10, 12, 8};

            int[][] P = {
                    {0, 5, 0},
                    {5, 0, 3},
                    {0, 3, 0}
            };

            int[][] dp = new int[n + 1][maxWeight + 1];
            List<Integer>[][] track = new ArrayList[n + 1][maxWeight + 1];
            
            for (int i = 0; i <= n; i++) {
                for (int w = 0; w <= maxWeight; w++) {
                    track[i][w] = new ArrayList<>();
                }
            }

            for (int i = 1; i <= n; i++) {
                int w_i = weights[i - 1];
                int v_i = values[i - 1];
                for (int w = 0; w <= maxWeight; w++) {
                    if (w_i > w) {
                        dp[i][w] = dp[i - 1][w];
                        track[i][w] = new ArrayList<>(track[i - 1][w]);
                    } else {
                        int without = dp[i - 1][w];
                        int withItem = dp[i - 1][w - w_i] + v_i;
                        if (withItem > without) {
                            dp[i][w] = withItem;
                            track[i][w] = new ArrayList<>(track[i - 1][w - w_i]);
                            track[i][w].add(i - 1);
                        } else {
                            dp[i][w] = without;
                            track[i][w] = new ArrayList<>(track[i - 1][w]);
                        }
                    }
                }
            }

            int bestValue = 0;
            List<Integer> bestCombo = new ArrayList<>();

            for (int w = 0; w <= maxWeight; w++) {
                List<Integer> items = track[n][w];
                int baseValue = 0;
                int interactionValue = 0;

                for (int i : items) baseValue += values[i];
                for (int i = 0; i < items.size(); i++) {
                    for (int j = i + 1; j < items.size(); j++) {
                        interactionValue += P[items.get(i)][items.get(j)];
                    }
                }

                int totalValue = baseValue + interactionValue;
                if (totalValue > bestValue) {
                    bestValue = totalValue;
                    bestCombo = new ArrayList<>(items);
                }
            }

            System.out.println("Максимальная ценность с учётом взаимодействий: " + bestValue);
            System.out.println("Выбранные предметы (индексы): " + bestCombo);
            for (int index : bestCombo) {
                System.out.println("Индекс: " + index + ", Вес: " + weights[index] + ", Ценность: " + values[index]);
            }
        }

    }
}
