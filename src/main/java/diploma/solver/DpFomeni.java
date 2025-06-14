package diploma.solver;

import diploma.entity.Result;

import java.util.ArrayList;
import java.util.List;

public class DpFomeni implements TaskSolver {

    @Override
    public Result solve(int n, int W, int[] weights, int[][] P) {
        int[][] dp = new int[n + 1][W + 1];
        List<Integer>[][] selected = new ArrayList[n + 1][W + 1];

        for (int i = 0; i <= n; i++) {
            for (int w = 0; w <= W; w++) {
                selected[i][w] = new ArrayList<>();
            }
        }

        for (int i = 1; i <= n; i++) {
            int itemWeight = weights[i - 1];

            for (int w = 0; w <= W; w++) {
                dp[i][w] = dp[i - 1][w];
                selected[i][w] = new ArrayList<>(selected[i - 1][w]);

                if (w >= itemWeight) {
                    int prevWeight = w - itemWeight;
                    List<Integer> prevItems = selected[i - 1][prevWeight];

                    int profit = P[i - 1][i - 1];

                    for (int j : prevItems) {
                        if (j < i - 1) {
                            profit += P[i - 1][j];
                        }
                    }

                    int totalProfit = dp[i - 1][prevWeight] + profit;

                    if (totalProfit > dp[i][w]) {
                        dp[i][w] = totalProfit;
                        selected[i][w] = new ArrayList<>(prevItems);
                        selected[i][w].add(i - 1);
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