package solver;

import entity.Item;

import java.util.ArrayList;
import java.util.List;

public class GreedyKnapsackSolver implements KnapsackSolver {

    @Override
    public List<Item> solve(List<Item> items,
                            double[][] synergy,
                            double capacity) {

        final int n = items.size();
        boolean[] used = new boolean[n];

        List<Item>  chosen = new ArrayList<>();
        List<Integer> idx  = new ArrayList<>();   // индексы выбранных предметов
        double currWeight  = 0.0;

        /* ---- итеративно добираем «лучшую выгоду-на-вес» ---- */
        while (true) {
            int    best = -1;
            double bestRatio = -1;

            for (int i = 0; i < n; i++) {
                if (used[i]) continue;

                Item it = items.get(i);
                double newW = currWeight + it.getWeight();
                if (newW > capacity) continue;   // не помещается

                /* маржинальная ценность вещи i относительно текущего S */
                double delta = it.getValue();
                if (synergy != null) {
                    for (int k : idx) delta += synergy[i][k];
                }

                double ratio = delta / it.getWeight();
                if (ratio > bestRatio) {
                    bestRatio = ratio;
                    best      = i;
                }
            }

            /* ничего не нашли — выходим */
            if (best == -1) break;

            /* добавляем найденную вещь в рюкзак */
            used[best] = true;
            idx.add(best);
            chosen.add(items.get(best));
            currWeight += items.get(best).getWeight();
        }

        return chosen;
    }
}