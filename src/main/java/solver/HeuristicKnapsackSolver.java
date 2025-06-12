package solver;

import entity.Item;

import java.util.*;

/**
 * Heuristic solver for the Quadratic 0-1 Knapsack Problem.
 * Strategy:  GRASP construction  +  1-swap local search
 */
public class HeuristicKnapsackSolver implements KnapsackSolver {

    /** доля предметов в RCL (0…1) */
    private static final double ALPHA = 0.2;
    /** сколько раз перезапускать GRASP */
    private static final int GRASP_ITERS = 30;

    private static final Random rnd = new Random();

    @Override
    public List<Item> solve(List<Item> items,
                            double[][] syn,
                            double capacity) {

        int n = items.size();
        List<Item>   bestList = new ArrayList<>();
        double       bestGain = 0;

        for (int run = 0; run < GRASP_ITERS; run++) {

            /* ---------- 1.  GRASP-construction ---------- */
            boolean[] used = new boolean[n];
            List<Integer> picked = new ArrayList<>();
            double weight = 0;
            double gain   = 0;

            while (true) {
                // собираем маржинальные «выгода/вес» для всех доступных вещей
                List<int[]> ranks = new ArrayList<>();
                for (int i = 0; i < n; i++) if (!used[i]) {
                    Item it = items.get(i);
                    if (weight + it.getWeight() > capacity) continue;

                    double delta = it.getValue();
                    for (int k : picked)
                        delta += syn[i][k];

                    ranks.add(new int[]{ i, delta <= 0 ? 0 : 1 }); // индекс + заглушка
                    // фактическое ratio храним отдельно, чтобы сортировать с double
                    ranks.get(ranks.size() - 1)[1] = 0;             // not used
                }

                if (ranks.isEmpty()) break;

                // сортируем по ratio (delta / w) у себя в массиве
                ranks.sort((a, b) -> Double.compare(
                        (items.get(b[0]).getValue() + synergySum(b[0], picked, syn))
                                / items.get(b[0]).getWeight(),
                        (items.get(a[0]).getValue() + synergySum(a[0], picked, syn))
                                / items.get(a[0]).getWeight()));

                // RCL-размер
                int rclSize = Math.max(1, (int) Math.round(ALPHA * ranks.size()));
                int choose  = ranks.get(rnd.nextInt(rclSize))[0];

                /* добавляем выбранный предмет */
                Item it = items.get(choose);
                picked.add(choose);
                used[choose] = true;
                weight += it.getWeight();
                gain   += it.getValue();
                for (int k : picked)
                    if (k != choose) gain += syn[choose][k];
            }

            /* ---------- 2.  Local 1-swap search ---------- */
            boolean improved = true;
            while (improved) {
                improved = false;

                for (int outIdx = 0; outIdx < picked.size() && !improved; outIdx++) {
                    int out = picked.get(outIdx);

                    // remove contribution of 'out'
                    double wOut = items.get(out).getWeight();
                    double vOut = items.get(out).getValue();
                    double synOut = 0;
                    for (int k : picked) if (k != out) synOut += syn[out][k];

                    for (int in = 0; in < n && !improved; in++) if (!used[in]) {
                        Item itIn = items.get(in);
                        double newW = weight - wOut + itIn.getWeight();
                        if (newW > capacity) continue;

                        double vIn  = itIn.getValue();
                        double synIn = 0;
                        for (int k : picked) if (k != out) synIn += syn[in][k];

                        double newGain = gain - vOut - synOut + vIn + synIn;

                        if (newGain > gain) {
                            // perform swap
                            used[out] = false; used[in] = true;
                            picked.set(outIdx, in);
                            weight = newW;
                            gain   = newGain;
                            improved = true;
                        }
                    }
                }
            }

            /* ---------- 3.  remember the best ---------- */
            if (gain > bestGain) {
                bestGain = gain;
                bestList.clear();
                for (int idx : picked) bestList.add(items.get(idx));
            }
        }
        return bestList;
    }

    /** helper: sum synergy of i with already picked set */
    private static double synergySum(int i, List<Integer> picked, double[][] syn) {
        double s = 0;
        for (int k : picked) s += syn[i][k];
        return s;
    }
}