package diploma.solver;


import diploma.entity.Result;

import java.util.ArrayList;
import java.util.List;

public class BruteForce implements TaskSolver {

    @Override
    public Result solve(int n, int W, int[] weights, int[][] P) {
        int totalCombinations = 1 << n;
        int bestValue = Integer.MIN_VALUE;
        int[] bestSolution = new int[n];

        for (int mask = 0; mask < totalCombinations; mask++) {
            int currentWeight = 0;
            int currentValue = 0;
            int[] x = new int[n];

            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    x[i] = 1;
                    currentWeight += weights[i];
                    currentValue += P[i][i];
                }
            }

            if (currentWeight > W) continue;

            for (int i = 0; i < n; i++) {
                if (x[i] == 1) {
                    for (int j = i + 1; j < n; j++) {
                        if (x[j] == 1) {
                            currentValue += 2 * P[i][j];
                        }
                    }
                }
            }

            if (currentValue > bestValue) {
                bestValue = currentValue;
                System.arraycopy(x, 0, bestSolution, 0, n);
            }
        }

        List<Integer> selectedItems = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (bestSolution[i] == 1) {
                selectedItems.add(i);
            }
        }

        System.out.println("лучшее значение: " + bestValue);
        return Result.builder()
                .profit(bestValue)
                .items(selectedItems)
                .build();
    }
}
