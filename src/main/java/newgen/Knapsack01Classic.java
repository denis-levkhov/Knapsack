package newgen;

import java.util.ArrayList;
import java.util.List;

/**
 * Classic 0‒1 Knapsack (linear profits, no quadratic terms).
 * Dynamic-programming solution in O(n·C) time and O(n·C) memory, where
 * C is the integral capacity, n – number of items.
 */
public final class Knapsack01Classic {

    public static class Result {
        public final double bestValue;
        public final List<Integer> chosenItems;
        Result(double bestValue, List<Integer> chosenItems) {
            this.bestValue = bestValue;
            this.chosenItems = chosenItems;
        }
        @Override public String toString() {
            return "Best value=" + bestValue + ", items=" + chosenItems;
        }
    }

    /**
     * Solves classic knapsack.
     *
     * @param weights integer weights w_i ≥ 0
     * @param profits values p_i (double)
     * @param capacity integer capacity C ≥ 0
     * @return optimal value and indices of chosen items (0-based)
     */
    public static Result solve(int[] weights, double[] profits, int capacity) {
        int n = weights.length;
        if (n != profits.length) throw new IllegalArgumentException("Dimension mismatch");

        double[][] dp = new double[n + 1][capacity + 1];
        boolean[][] take = new boolean[n + 1][capacity + 1];

        // DP fill
        for (int i = 1; i <= n; i++) {
            int w = weights[i - 1];
            double p = profits[i - 1];
            for (int c = 0; c <= capacity; c++) {
                // option 1: skip item i
                double best = dp[i - 1][c];
                boolean chosen = false;
                // option 2: take item i if it fits
                if (w <= c) {
                    double cand = dp[i - 1][c - w] + p;
                    if (cand > best) {
                        best = cand;
                        chosen = true;
                    }
                }
                dp[i][c] = best;
                take[i][c] = chosen;
            }
        }

        // reconstruct chosen items
        List<Integer> items = new ArrayList<>();
        int c = capacity;
        for (int i = n; i >= 1; i--) {
            if (take[i][c]) {
                items.add(i - 1); // original index
                c -= weights[i - 1];
            }
        }

        return new Result(dp[n][capacity], items);
    }

    // ------------------------- Self-test ------------------------------------
    public static void main(String[] args) {
        int[] w = {2, 3, 4, 5};
        double[] p = {3, 4, 5, 6};
        int C = 7;
        Result res = solve(w, p, C);
        System.out.println(res);
    }
}