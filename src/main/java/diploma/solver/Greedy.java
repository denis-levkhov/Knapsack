package diploma.solver;

import diploma.entity.Result;

import java.util.ArrayList;
import java.util.List;

public class Greedy implements TaskSolver {

    @Override
    public Result solve(int n, int capacity, int[] weights, int[][] P) {
        boolean[] used = new boolean[n]; // выбран или нет
        List<Integer> selected = new ArrayList<>(); // список включенных элементов
        int currentWeight = 0; // сколько занято веса в рюкзаке
        int totalProfit = 0; // текущая суммарная ценность

        while (true) {
            int bestItem = -1;
            double bestRatio = -1;

            for (int i = 0; i < n; i++) { // перебираем неиспользованные предметы, не превысив ценность
                if (used[i] || weights[i] + currentWeight > capacity) continue;

                int profit = P[i][i]; // собственная ценность
                for (int j : selected) {
                    profit += P[i][j] + P[j][i]; // взаимодействие с уже выбранными
                }

                double ratio = (double) profit / weights[i];
                if (ratio > bestRatio) {
                    bestRatio = ratio;
                    bestItem = i;
                }
            }

            if (bestItem == -1) break;

            used[bestItem] = true;
            selected.add(bestItem);
            currentWeight += weights[bestItem];

            // Добавляем вклад предмета в общий профит
            totalProfit += P[bestItem][bestItem];
            for (int j : selected) {
                if (j != bestItem) {
                    totalProfit += P[bestItem][j] + P[j][bestItem];
                }
            }
        }

        return new Result(totalProfit, selected);
    }
}
