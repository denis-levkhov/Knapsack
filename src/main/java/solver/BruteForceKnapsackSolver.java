package solver;

import entity.Item;

import java.util.ArrayList;
import java.util.List;

public class BruteForceKnapsackSolver implements KnapsackSolver {

    @Override
    public List<Item> solve(List<Item> items,
                            double[][] synergy,
                            double capacity) {

        int n          = items.size();
        int totalMasks = 1 << n;          // 2^n подмножеств

        double bestValue = 0.0;
        int    bestMask  = 0;

        for (int mask = 1; mask < totalMasks; mask++) {

            double weight = 0.0;
            double value  = 0.0;

            /* 1) базовые вес и ценность */
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    Item it = items.get(i);
                    weight += it.getWeight();
                    value  += it.getValue();
                }
            }
            if (weight > capacity) continue;      // перегруз – сразу пропускаем

            /* 2) синергия для пар (i,j), i<j */
            if (synergy != null) {
                for (int i = 0; i < n; i++) {
                    if ((mask & (1 << i)) == 0) continue;
                    for (int j = i + 1; j < n; j++) {
                        if ((mask & (1 << j)) != 0)
                            value += synergy[i][j];
                    }
                }
            }

            /* 3) обновляем optimum */
            if (value > bestValue) {
                bestValue = value;
                bestMask  = mask;
            }
        }

        /* 4) восстанавливаем предметы по bestMask */
        List<Item> result = new ArrayList<>();
        for (int i = 0; i < n; i++)
            if ((bestMask & (1 << i)) != 0)
                result.add(items.get(i));

        return result;
    }
}