package newgen;

import java.util.ArrayList;
import java.util.List;

/**
 * Brute-force solver for the Quadratic Knapsack Problem (QKP).
 * <p>Maximises:  sum_i p_i x_i + sum_{i<j} p_{ij} x_i x_j
 * <br>subject to: sum_i w_i x_i \le C,  x_i \in {0,1}</p>
 */
public final class QuadraticKnapsackBruteForce {

    public static class Result {
        public final double bestValue;
        public final List<Integer> bestSubset;

        private Result(double bestValue, List<Integer> bestSubset) {
            this.bestValue = bestValue;
            this.bestSubset = bestSubset;
        }

        @Override
        public String toString() {
            return "Best value=" + bestValue + " | subset=" + bestSubset;
        }
    }

    /**
     * Solves the QKP by enumerating all 2^n subsets (feasible for small n ≤ 20).
     *
     * @param weights           w_i array, length n
     * @param linearProfits     p_i array, length n
     * @param quadraticProfits  symmetric n×n matrix of p_{ij}
     * @param capacity          knapsack capacity C
     * @return optimal objective value and item indices (0-based)
     */
    public static Result solve(double[] weights,
                               double[] linearProfits,
                               double[][] quadraticProfits,
                               double capacity) {
        int n = weights.length;
        if (n != linearProfits.length || n != quadraticProfits.length) {
            throw new IllegalArgumentException("Input dimension mismatch");
        }

        double bestValue = Double.NEGATIVE_INFINITY;
        List<Integer> bestSubset = List.of();

        long totalSubsets = 1L << n; // 2^n
        for (long mask = 0; mask < totalSubsets; mask++) {
            double weightSum = 0.0;
            double value = 0.0;
            // Linear part & weight
            for (int i = 0; i < n; i++) {
                if ((mask & (1L << i)) != 0) {
                    weightSum += weights[i];
                    value += linearProfits[i];
                    if (weightSum > capacity) {
                        break; // infeasible, prune early
                    }
                }
            }
            if (weightSum > capacity) continue;

            // Quadratic interactions
            for (int i = 0; i < n; i++) {
                if ((mask & (1L << i)) == 0) continue;
                for (int j = i + 1; j < n; j++) {
                    if ((mask & (1L << j)) != 0) {
                        value += quadraticProfits[i][j];
                    }
                }
            }

            if (value > bestValue) {
                bestValue = value;
                bestSubset = extractSubset(mask, n);
            }
        }
        return new Result(bestValue, bestSubset);
    }

    private static List<Integer> extractSubset(long mask, int n) {
        List<Integer> subset = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if ((mask & (1L << i)) != 0) subset.add(i);
        }
        return subset;
    }

    // --- Minimal self-test ---------------------------------------------------
    public static void main(String[] args) {
        double[] w = {2, 3, 4, 5};
        double[] p = {3, 4, 5, 6};
        double[][] P = {
                {0, 1, 0, 0},
                {1, 0, 2, 0},
                {0, 2, 0, 1},
                {0, 0, 1, 0}
        };
        double C = 7;

        Result res = solve(w, p, P, C);
        System.out.println(res);
    }
}