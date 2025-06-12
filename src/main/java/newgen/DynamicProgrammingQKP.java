package newgen;

import java.util.*;

/**
 * Dynamic programming heuristic for the 0-1 Quadratic Knapsack Problem (QKP).
 * <p>
 * Works with <strong>integral</strong> weights w_i and capacity C (pseudo-polynomial).
 * Complexity: O(C · n²) time, O(C · n) memory (bit-set stored for every weight).
 * </p>
 * <p>
 * For each item index m (1 … n) and every weight w ≤ C we keep the best objective
 * value achievable, plus the actual chosen set encoded as a BitSet.  When trying
 * to add item m we can compute its quadratic contribution by scanning the BitSet
 * of the previous state.
 * </p>
 * <p>Warning: memory can explode for large C (capacity) and n.</p>
 */
public final class DynamicProgrammingQKP {

    /** Container for state value & chosen items. */
    private static final class State {
        double value;   // best objective value
        BitSet items;   // chosen items

        State(double value, BitSet items) {
            this.value = value;
            this.items = items;
        }
    }

    public static class Result {
        public final double value;
        public final List<Integer> items;
        Result(double value, List<Integer> items) { this.value = value; this.items = items; }
        @Override public String toString() { return "Value=" + value + ", items=" + items; }
    }

    /**
     * Solve QKP via DP heuristic (integral weights).
     *
     * @param weights  w_i (int, non-negative)
     * @param linear   p_i  (double)
     * @param quad     P_{ij} symmetric matrix (double)
     * @param capacity C (int)
     */
    public static Result solve(int[] weights, double[] linear, double[][] quad, int capacity) {
        int n = weights.length;
        if (n != linear.length || n != quad.length) throw new IllegalArgumentException("Dimension mismatch");

        // Initial DP arrays (index by weight)
        State[] dp = new State[capacity + 1];
        dp[0] = new State(0.0, new BitSet(n));
        for (int w = 1; w <= capacity; w++) dp[w] = null; // unreachable yet

        // Process items sequentially
        for (int m = 0; m < n; m++) {
            int w_m = weights[m];
            State[] next = Arrays.copyOf(dp, dp.length); // start with previous solutions (skip item)

            for (int w = 0; w <= capacity; w++) {
                State st = dp[w];
                if (st == null) continue;
                int newW = w + w_m;
                if (newW > capacity) continue;

                // Compute incremental value of adding item m
                double inc = linear[m];
                BitSet chosen = st.items;
                for (int i = chosen.nextSetBit(0); i >= 0; i = chosen.nextSetBit(i + 1)) {
                    inc += quad[i][m];
                }
                double candVal = st.value + inc;

                State best = next[newW];
                if (best == null || candVal > best.value) {
                    BitSet newSet = (BitSet) chosen.clone();
                    newSet.set(m);
                    next[newW] = new State(candVal, newSet);
                }
            }
            dp = next; // move to next item
        }

        // Extract best over all weights
        double bestVal = Double.NEGATIVE_INFINITY;
        BitSet bestSet = new BitSet(n);
        for (State st : dp) {
            if (st != null && st.value > bestVal) {
                bestVal = st.value;
                bestSet = st.items;
            }
        }
        List<Integer> items = new ArrayList<>();
        for (int i = bestSet.nextSetBit(0); i >= 0; i = bestSet.nextSetBit(i + 1)) items.add(i);
        return new Result(bestVal, items);
    }

    // ------------------------ quick demo ------------------------------------
    public static void main(String[] args) {
        int[] w = {2, 3, 4, 5};
        double[] p = {3, 4, 5, 6};
        double[][] P = {
                {0, 1, 0, 0},
                {1, 0, 2, 0},
                {0, 2, 0, 1},
                {0, 0, 1, 0}
        };
        int C = 7;

        Result res = solve(w, p, P, C);
        System.out.println(res);
    }
}