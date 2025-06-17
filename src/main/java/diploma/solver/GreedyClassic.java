package diploma.solver;

import diploma.entity.Result;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GreedyClassic implements TaskSolver {
    @Override
    public Result solve(int n, int W, int[] weights, int[][] P) {
        List<Integer> selectedItems = new ArrayList<>();
        int currentWeight = 0;

        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < n; i++) order.add(i);
        order.sort(Comparator.comparingDouble(i -> -1.0 * P[i][i] / weights[i]));

        for (int i : order) {
            if (currentWeight + weights[i] <= W) {
                selectedItems.add(i);
                currentWeight += weights[i];
            }
        }

        int totalProfit = 0;
        for (int i : selectedItems) {
            totalProfit += P[i][i];
            for (int j : selectedItems) {
                if (i < j) totalProfit += P[i][j];
            }
        }

        return Result.builder()
                .profit(totalProfit)
                .items(selectedItems)
                .build();
    }
}