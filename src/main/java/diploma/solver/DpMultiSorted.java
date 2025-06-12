package diploma.solver;

import diploma.entity.Result;

import java.util.*;

public class DpMultiSorted implements TaskSolver {

    @Override
    public Result solve(int n, int W, int[] weights, int[][] P) {
        List<Comparator<Integer>> strategies = List.of(
                Comparator.comparingInt(i -> weights[i]),                                 // по весу (возр.)
                (i1, i2) -> Integer.compare(weights[i2], weights[i1]),                     // по весу (убыв.)
                (i1, i2) -> Double.compare((double)(P[i2][i2]) / weights[i2],
                        (double)(P[i1][i1]) / weights[i1])               // по ценности на вес
        );

        Result bestResult = new Result(0, new ArrayList<>());

        for (Comparator<Integer> strategy : strategies) {
            List<Integer> order = new ArrayList<>();
            for (int i = 0; i < n; i++) order.add(i);
            order.sort(strategy);

            Result current = solveDP(order, W, weights, P);
            if (current.getProfit() > bestResult.getProfit()) {
                bestResult = current;
            }
        }

        return bestResult;
    }

    private static Result solveDP(List<Integer> order, int W, int[] weights, int[][] P) {
        int n = order.size();
        int[][] dp = new int[n + 1][W + 1];
        List<Integer>[][] selected = new ArrayList[n + 1][W + 1];
        for (int i = 0; i <= n; i++) for (int w = 0; w <= W; w++) selected[i][w] = new ArrayList<>();

        for (int k = 1; k <= n; k++) {
            int item = order.get(k - 1);
            int weight = weights[item];

            for (int r = 0; r <= W; r++) {
                dp[k][r] = dp[k - 1][r];
                selected[k][r] = new ArrayList<>(selected[k - 1][r]);

                if (r >= weight) {
                    int prevWeight = r - weight;
                    List<Integer> prevItems = selected[k - 1][prevWeight];

                    int profit = P[item][item];
                    for (int i : prevItems) {
                        profit += P[i][item] + P[item][i];
                    }

                    int totalProfit = dp[k - 1][prevWeight] + profit;
                    if (totalProfit > dp[k][r]) {
                        dp[k][r] = totalProfit;
                        selected[k][r] = new ArrayList<>(prevItems);
                        selected[k][r].add(item);
                    }
                }
            }
        }

        int bestProfit = 0;
        List<Integer> bestItems = new ArrayList<>();
        for (int r = 0; r <= W; r++) {
            if (dp[n][r] > bestProfit) {
                bestProfit = dp[n][r];
                bestItems = selected[n][r];
            }
        }

        return new Result(bestProfit, bestItems);
    }
}