package solver;

import entity.Item;

import java.util.*;

/** exact 0-1 knapsack (quadratic objective) for n ≤ 32 */
public class ZeroOneKnapsackSolver implements KnapsackSolver {

    private static final int SCALE = 100;              // 0.01 кг

    /** контейнер мемоизации */
    private static final class Memo {
        double gain;
        int    nextMask;
    }

    /** склейка (idx,mask) ⇒ long без пересечений битов */
    private static long key(int idx, int mask) {
        return (((long) idx) << 32) | (mask & 0xFFFF_FFFFL);
    }

    @Override
    public List<Item> solve(List<Item> items, double[][] syn, double capacity) {

        final int n     = items.size();
        if (n > 32)
            throw new IllegalArgumentException("Exact 0-1 solver supports up to 32 items");

        final int Wlim  = (int) Math.round(capacity * SCALE);
        Map<Long, Memo> memo = new HashMap<>(1 << 20); // достаточно ≈ 1 М записей

        /* рекурсия + memo --------------------------------------------------- */
        class Rec {
            double dfs(int idx, int mask, int w) {
                if (idx == n) return 0;

                long k = key(idx, mask);
                Memo m = memo.get(k);
                if (m != null) return m.gain;

                double bestGain = dfs(idx + 1, mask, w); // skip
                int    bestNext = mask;

                Item cur = items.get(idx);
                int w2   = w + (int) Math.round(cur.getWeight() * SCALE);
                if (w2 <= Wlim) {
                    double delta = cur.getValue();
                    for (int k2 = 0; k2 < n; k2++)
                        if ((mask & (1 << k2)) != 0)
                            delta += syn[idx][k2];

                    double cand = delta + dfs(idx + 1, mask | (1 << idx), w2);
                    if (cand > bestGain) {
                        bestGain = cand;
                        bestNext = mask | (1 << idx);
                    }
                }
                Memo put = new Memo(); put.gain = bestGain; put.nextMask = bestNext;
                memo.put(k, put);
                return bestGain;
            }
        }
        new Rec().dfs(0, 0, 0);

        /* восстановление ---------------------------------------------------- */
        List<Item> res = new ArrayList<>();
        int idx = 0, mask = 0, w = 0;
        while (idx < n) {
            Memo m = memo.get(key(idx, mask));
            if (m == null) break;
            if (m.nextMask != mask) {          // предмет idx выбран
                res.add(items.get(idx));
                w += (int) Math.round(items.get(idx).getWeight() * SCALE);
                mask = m.nextMask;
            }
            idx++;
        }
        return res;
    }
}