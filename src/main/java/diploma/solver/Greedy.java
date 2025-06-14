package diploma.solver;

import diploma.entity.Result;

import java.util.ArrayList;
import java.util.List;

public class Greedy implements TaskSolver {

    @Override
    public Result solve(int n, int W, int[] weights, int[][] P) {
        boolean[] used = new boolean[n];
        List<Integer> selectedItems = new ArrayList<>();
        int currentWeight = 0;
        int totalProfit = 0;

        while (true) {
            int bestItem = -1;
            double bestRatio = -1;

            for (int i = 0; i < n; i++) {
                if (used[i] || weights[i] + currentWeight > W) continue;

                int profit = P[i][i];
                for (int j : selectedItems) {
                    profit += (j < i) ? P[j][i] : P[i][j];
                }

                double ratio = (double) profit / weights[i];
                if (ratio > bestRatio) {
                    bestRatio = ratio;
                    bestItem = i;
                }
            }

            if (bestItem == -1) break;

            used[bestItem] = true;
            selectedItems.add(bestItem);
            currentWeight += weights[bestItem];

            totalProfit += P[bestItem][bestItem];
            for (int j : selectedItems) {
                if (j != bestItem) {
                    totalProfit += (j < bestItem) ? P[j][bestItem] : P[bestItem][j];
                }
            }
        }

        return Result.builder()
                .profit(totalProfit)
                .items(selectedItems)
                .build();
    }
}