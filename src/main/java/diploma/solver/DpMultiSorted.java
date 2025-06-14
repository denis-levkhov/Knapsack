package diploma.solver;

import diploma.entity.Result;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DpMultiSorted implements TaskSolver {

    @Override
    public Result solve(int n, int W, int[] weights, int[][] P) {
        List<Comparator<Integer>> strategies = List.of(
                Comparator.comparingInt(i -> weights[i]), // по весу (возр.)
                (i1, i2) -> Integer.compare(weights[i2], weights[i1]), // по весу (убыв.)
                (i1, i2) -> Double.compare(
                        (double) P[i2][i2] / weights[i2],
                        (double) P[i1][i1] / weights[i1]) // по ценности на вес
        );

        int bestProfit = Integer.MIN_VALUE;
        List<Integer> selectedItems = new ArrayList<>();

        for (Comparator<Integer> strategy : strategies) {
            List<Integer> order = new ArrayList<>();
            for (int i = 0; i < n; i++) order.add(i);
            order.sort(strategy);

            Result current = solveDP(order, W, weights, P);
            if (current.getProfit() > bestProfit) {
                bestProfit = current.getProfit();
                selectedItems = current.getItems();
            }
        }

        return Result.builder()
                .profit(bestProfit)
                .items(selectedItems)
                .build();
    }

    private static Result solveDP(List<Integer> order, int W, int[] weights, int[][] P) {
        int n = order.size();
        int[][] dp = new int[n + 1][W + 1];
        List<Integer>[][] selected = new ArrayList[n + 1][W + 1];

        for (int i = 0; i <= n; i++) {
            for (int w = 0; w <= W; w++) {
                selected[i][w] = new ArrayList<>();
            }
        }

        for (int i = 1; i <= n; i++) {
            int item = order.get(i - 1);
            int itemWeight = weights[item];

            for (int w = 0; w <= W; w++) {
                dp[i][w] = dp[i - 1][w];
                selected[i][w] = new ArrayList<>(selected[i - 1][w]);

                if (w >= itemWeight) {
                    int prevWeight = w - itemWeight;
                    List<Integer> prevItems = selected[i - 1][prevWeight];

                    int profit = P[item][item];
                    for (int j : prevItems) {
                        profit += P[j][item];
                    }

                    int totalProfit = dp[i - 1][prevWeight] + profit;
                    if (totalProfit > dp[i][w]) {
                        dp[i][w] = totalProfit;
                        selected[i][w] = new ArrayList<>(prevItems);
                        selected[i][w].add(item);
                    }
                }
            }
        }

        int bestProfit = Integer.MIN_VALUE;
        List<Integer> selectedItems = new ArrayList<>();
        for (int w = 0; w <= W; w++) {
            if (dp[n][w] > bestProfit) {
                bestProfit = dp[n][w];
                selectedItems = selected[n][w];
            }
        }

        return Result.builder()
                .profit(bestProfit)
                .items(selectedItems)
                .build();
    }
}