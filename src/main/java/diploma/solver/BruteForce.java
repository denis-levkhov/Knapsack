package diploma.solver;

import diploma.entity.Result;

import java.util.ArrayList;
import java.util.List;

public class BruteForce implements TaskSolver {

    @Override
    public Result solve(int n, int W, int[] weights, int[][] P) {
        long totalCombinations = 1L << n;
        int bestProfit = Integer.MIN_VALUE;
        int[] selectedArray = new int[n];

        for (int mask = 0; mask < totalCombinations; mask++) {
            int currentWeight = 0;
            int currentProfit = 0;
            int[] currentItems = new int[n];

            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    currentItems[i] = 1;
                    currentWeight += weights[i];
                    currentProfit += P[i][i];
                }
            }

            if (currentWeight > W) continue;

            for (int i = 0; i < n; i++) {
                if (currentItems[i] == 1) {
                    for (int j = i + 1; j < n; j++) {
                        if (currentItems[j] == 1) {
                            currentProfit += 2 * P[i][j];
                        }
                    }
                }
            }

            if (currentProfit > bestProfit) {
                bestProfit = currentProfit;
                System.arraycopy(currentItems, 0, selectedArray, 0, n);
            }
        }

        int[] resultItems = new int[n];
        int count = 0;
        for (int i = 0; i < n; i++) {
            if (selectedArray[i] == 1) {
                resultItems[count++] = i;
            }
        }

        int[] finalItems = new int[count];
        System.arraycopy(resultItems, 0, finalItems, 0, count);

        List<Integer> selectedList = new ArrayList<>();
        for (int finalItem : finalItems) {
            selectedList.add(finalItem);
        }

        return Result.builder()
                .profit(bestProfit)
                .items(selectedList)
                .build();
    }
}