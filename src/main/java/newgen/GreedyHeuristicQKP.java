package newgen;

import java.util.*;

/**
 * Greedy heuristic for the 0-1 Quadratic Knapsack Problem based on the two-phase idea
 * described in the Wikipedia article (originally attributed to George Dantzig).
 * <p>
 * Phase 1: Build an initial solution by inserting items with the best
 *          ( p_i + \sum_{j \ne i} P_{ij} ) / w_i  ratio until the knapsack is full.
 * Phase 2: Iteratively improve by the best single add, remove, or swap move
 *          that increases the objective value while respecting capacity.
 * Stops when no improving move exists.
 * </p>
 */
public final class GreedyHeuristicQKP {

    public static class Result {
        public final double value;
        public final List<Integer> items;

        private Result(double value, List<Integer> items) {
            this.value = value;
            this.items = items;
        }

        @Override
        public String toString() {
            return "Value=" + value + ", items=" + items;
        }
    }

    /**
     * Solve QKP via greedy heuristic.
     *
     * @param w  weights (length n)
     * @param p  linear profits (length n)
     * @param P  symmetric matrix of quadratic profits (n×n)
     * @param C  capacity
     */
    public static Result solve(double[] w, double[] p, double[][] P, double C) {
        int n = w.length;
        if (n != p.length || n != P.length) throw new IllegalArgumentException("Dimension mismatch");

        // ---------- Phase 1: Greedy construction ----------------------------
        double[] score = new double[n];
        for (int i = 0; i < n; i++) {
            double sum = 0.0;
            for (int j = 0; j < n; j++) {
                if (j == i) continue;
                sum += P[i][j];
            }
            score[i] = (p[i] + sum) / w[i];
        }
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        Arrays.sort(idx, Comparator.comparingDouble((Integer i) -> -score[i])); // descending

        boolean[] inSol = new boolean[n];
        double weight = 0.0;
        for (int id : idx) {
            if (weight + w[id] <= C) {
                inSol[id] = true;
                weight += w[id];
            }
        }

        // ---------- Phase 2: Local improvement ------------------------------
        double currentValue = objective(inSol, w, p, P);
        boolean improvement = true;
        while (improvement) {
            improvement = false;
            double bestDelta = 0.0;
            int add = -1, remove = -1; // add >=0 means add-only, remove>=0 means remove-only or swap

            // Evaluate all possible additions
            for (int j = 0; j < n; j++) {
                if (inSol[j]) continue;
                if (weight + w[j] <= C) {
                    inSol[j] = true;
                    double newVal = objective(inSol, w, p, P);
                    double delta = newVal - currentValue;
                    if (delta > bestDelta) {
                        bestDelta = delta;
                        add = j;
                        remove = -1;
                    }
                    inSol[j] = false; // rollback
                }
            }

            // Evaluate all possible removals
            for (int i = 0; i < n; i++) {
                if (!inSol[i]) continue;
                inSol[i] = false;
                double newVal = objective(inSol, w, p, P);
                double delta = newVal - currentValue;
                if (delta > bestDelta) {
                    bestDelta = delta;
                    add = -1;
                    remove = i;
                }
                inSol[i] = true; // rollback
            }

            // Evaluate all possible swaps (i in solution, j outside)
            for (int i = 0; i < n; i++) {
                if (!inSol[i]) continue;
                for (int j = 0; j < n; j++) {
                    if (inSol[j]) continue;
                    double newWeight = weight - w[i] + w[j];
                    if (newWeight > C) continue;
                    inSol[i] = false;
                    inSol[j] = true;
                    double newVal = objective(inSol, w, p, P);
                    double delta = newVal - currentValue;
                    if (delta > bestDelta) {
                        bestDelta = delta;
                        add = j;
                        remove = i;
                    }
                    inSol[i] = true;
                    inSol[j] = false;
                }
            }

            // Apply best move (if any)
            if (bestDelta > 1e-9) {
                improvement = true;
                if (remove >= 0) {
                    inSol[remove] = false;
                    weight -= w[remove];
                }
                if (add >= 0) {
                    inSol[add] = true;
                    weight += w[add];
                }
                currentValue += bestDelta;
            }
        }

        // Collect chosen items
        List<Integer> items = new ArrayList<>();
        for (int i = 0; i < n; i++) if (inSol[i]) items.add(i);
        return new Result(currentValue, items);
    }

    // -----------------------------------------------------------------------
    private static double objective(boolean[] inSol, double[] w, double[] p, double[][] P) {
        int n = p.length;
        double val = 0.0;
        for (int i = 0; i < n; i++) if (inSol[i]) val += p[i];
        for (int i = 0; i < n; i++) {
            if (!inSol[i]) continue;
            for (int j = i + 1; j < n; j++) if (inSol[j]) val += P[i][j];
        }
        return val;
    }

    // ----------------------- Self-test --------------------------------------
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