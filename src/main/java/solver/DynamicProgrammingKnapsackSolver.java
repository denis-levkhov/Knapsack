package solver;

import entity.Item;

import java.util.ArrayList;
import java.util.List;

public class DynamicProgrammingKnapsackSolver implements KnapsackSolver {

    @Override
    public List<Item> solve(List<Item> items,
                            double[][] synergy,
                            double capacity) {

        final int n      = items.size();
        final int total  = 1 << n;          // 2^n подмножеств

        double[] w = new double[total];     // вес поднабора
        double[] v = new double[total];     // ценность поднабора
        int bestMask = 0;                   // индекс оптимального подмножества

        /* ---  основная динамика  --- */
        for (int mask = 1; mask < total; mask++) {

            /* 1. предмет, который «добавили» */
            int lsb  = Integer.numberOfTrailingZeros(mask); // позиция младшего установленного бита
            int prev = mask & (mask - 1);                   // та же маска без lsb

            /* 2. обновляем вес и базовую ценность                  */
            Item it = items.get(lsb);
            w[mask] = w[prev] + it.getWeight();
            v[mask] = v[prev] + it.getValue();

            /* 3. добавляем синергию пары (lsb, k) для всех k ∈ prev */
            if (synergy != null) {
                int m = prev;
                while (m != 0) {
                    int k = Integer.numberOfTrailingZeros(m);
                    v[mask] += synergy[lsb][k];
                    m &= m - 1;             // удаляем младший установленный бит
                }
            }

            /* 4. проверяем ограничение по весу и обновляем optimum */
            if (w[mask] <= capacity && v[mask] > v[bestMask])
                bestMask = mask;
        }

        /* ---  восстановление ответа  --- */
        List<Item> result = new ArrayList<>();
        for (int i = 0; i < n; i++)
            if ((bestMask & (1 << i)) != 0)
                result.add(items.get(i));

        return result;
    }
}