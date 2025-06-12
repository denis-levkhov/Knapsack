package newgen;

import java.util.*;

/**
 * Tabu Search heuristic for the 0-1 Quadratic Knapsack Problem.
 *
 * <p>Нисходящий поиск с набором ходов «add / remove / swap».
 * Предыдущие ходы заносятся в tabu-лист на фиксированный срок (tenure),
 * что предотвращает циклы и позволяет выходить из локальных минимумов.
 * «Аспирация»: tabu-ход разрешён, если приводит к глобально лучшему
 * значению.</p>
 */
public final class TabuSearchQKP {

    public static class Result {
        public final double value;
        public final List<Integer> items;
        Result(double value, List<Integer> items) { this.value = value; this.items = items; }
        @Override public String toString() { return "Value=" + value + ", items=" + items; }
    }

    // ---------------------------------------------------------------------
    public static Result solve(double[] w, double[] p, double[][] P, double C,
                               int maxIter, int tenure) {
        int n = w.length;
        if (n != p.length || n != P.length) throw new IllegalArgumentException("Dimension mismatch");
        // --- начальное решение: возьмём жадное ----------------------------------
        GreedyHeuristicQKP.Result g0 = GreedyHeuristicQKP.solve(w, p, P, C);
        boolean[] current = new boolean[n];
        double currentWeight = 0;
        for (int idx : g0.items) {
            current[idx] = true;
            currentWeight += w[idx];
        }
        double currentValue = g0.value;

        boolean[] best = Arrays.copyOf(current, n);
        double bestValue = currentValue;

        // tabu arrays: iteration when item becomes free again
        int[] tabuAddUntil = new int[n]; // добавлять нельзя пока iter < tabuAddUntil[i]
        int[] tabuRemUntil = new int[n]; // удалять нельзя

        Random rnd = new Random(42);

        for (int iter = 1; iter <= maxIter; iter++) {
            Move bestMove = null;
            double bestMoveDelta = Double.NEGATIVE_INFINITY;

            // --- enumerate neighbours -------------------------------------
            for (int i = 0; i < n; i++) {
                if (current[i]) {
                    // remove i
                    double newWeight = currentWeight - w[i];
                    double delta = -p[i];
                    for (int k = 0; k < n; k++) if (current[k] && k != i) delta -= P[i][k];
                    boolean tabu = iter < tabuRemUntil[i];
                    boolean aspired = (currentValue + delta) > bestValue;
                    if ((!tabu || aspired) && delta > bestMoveDelta) {
                        bestMoveDelta = delta;
                        bestMove = new Move(-1, i); // remove only
                    }

                    // swaps i -> j
                    for (int j = 0; j < n; j++) {
                        if (current[j]) continue;
                        double wChange = -w[i] + w[j];
                        if (currentWeight + wChange > C) continue;
                        double swapDelta = -p[i] + p[j];
                        for (int k = 0; k < n; k++) {
                            if (k == i) continue;
                            if (current[k]) swapDelta -= P[i][k];
                        }
                        for (int k = 0; k < n; k++) {
                            if (current[k] && k != i) swapDelta += P[j][k];
                        }
                        boolean tabuAdd = iter < tabuAddUntil[j];
                        boolean tabuRem = iter < tabuRemUntil[i];
                        tabu = tabuAdd || tabuRem;
                        aspired = (currentValue + swapDelta) > bestValue;
                        if ((!tabu || aspired) && swapDelta > bestMoveDelta) {
                            bestMoveDelta = swapDelta;
                            bestMove = new Move(j, i); // add j, remove i
                        }
                    }
                } else { // not in solution
                    // add j
                    if (currentWeight + w[i] > C) continue;
                    double delta = p[i];
                    for (int k = 0; k < n; k++) if (current[k]) delta += P[i][k];
                    boolean tabu = iter < tabuAddUntil[i];
                    boolean aspired = (currentValue + delta) > bestValue;
                    if ((!tabu || aspired) && delta > bestMoveDelta) {
                        bestMoveDelta = delta;
                        bestMove = new Move(i, -1); // add only
                    }
                }
            }

            if (bestMove == null) break; // no feasible moves (shouldn't happen)

            // --- apply best move ------------------------------------------
            if (bestMove.remove >= 0) {
                current[bestMove.remove] = false;
                currentWeight -= w[bestMove.remove];
                currentValue -= p[bestMove.remove];
                for (int k = 0; k < n; k++) if (current[k]) currentValue -= P[bestMove.remove][k];
                tabuAddUntil[bestMove.remove] = iter + tenure; // forbid re-add soon
            }
            if (bestMove.add >= 0) {
                current[bestMove.add] = true;
                for (int k = 0; k < n; k++) if (current[k] && k != bestMove.add) currentValue += P[bestMove.add][k];
                currentValue += p[bestMove.add];
                currentWeight += w[bestMove.add];
                tabuRemUntil[bestMove.add] = iter + tenure; // forbid immediate removal
            }

            // update global best
            if (currentValue > bestValue + 1e-9) {
                bestValue = currentValue;
                best = Arrays.copyOf(current, n);
            }
        }

        List<Integer> chosen = new ArrayList<>();
        for (int i = 0; i < n; i++) if (best[i]) chosen.add(i);
        return new Result(bestValue, chosen);
    }

    private static final class Move {
        final int add;    // index to add, or -1
        final int remove; // index to remove, or -1
        Move(int add, int remove) { this.add = add; this.remove = remove; }
    }

    // ------------------------- Demo -----------------------------------------
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

        Result res = solve(w, p, P, C, /*maxIter=*/500, /*tenure=*/3);
        System.out.println(res);
    }
}