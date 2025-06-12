package diploma.solver;

import diploma.entity.Result;

import java.util.ArrayList;
import java.util.List;

public class ZeroOneClassicDp implements TaskSolver {

    @Override
    public Result solve(int n, int W, int[] weights, int[][] P) {
        int[][] dp = new int[n + 1][W + 1];

        // Заполняем таблицу DP
        for (int i = 1; i <= n; i++) {
            int weight = weights[i - 1];
            int value = P[i - 1][i - 1]; // ценность = диагональ матрицы P

            for (int w = 0; w <= W; w++) {
                if (weight > w) {
                    dp[i][w] = dp[i - 1][w];
                } else {
                    dp[i][w] = Math.max(dp[i - 1][w], dp[i - 1][w - weight] + value);
                }
            }
        }

        List<Integer> selectedItems = new ArrayList<>();
        int w = W;
        for (int i = n; i > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                selectedItems.add(i - 1);
                w -= weights[i - 1];
            }
        }

        return Result.builder()
                .profit(dp[n][W])
                .items(selectedItems)
                .build();
    }
}