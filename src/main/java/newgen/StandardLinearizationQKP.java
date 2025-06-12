package newgen;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * Standard linearization formulation (LP1) for the 0-1 Quadratic Knapsack Problem.
 * <p>
 *    max  \sum_i p_i x_i  +  \sum_{i<j} P_{ij} z_{ij}
 * <br> s.t.  \sum_i w_i x_i ≤ C
 * <br>      z_{ij} ≤ x_i            ∀ i<j
 * <br>      z_{ij} ≤ x_j            ∀ i<j
 * <br>      x_i + x_j − z_{ij} ≤ 1  ∀ i<j   ( ⇔  x_i + x_j − 1 ≤ z_{ij})
 * <br>      x_i ∈ {0,1},  z_{ij} ∈ [0,1]
 * </p>
 * The model is built with Google OR-Tools (open-source, Apache 2.0).
 */
public final class StandardLinearizationQKP {

    public static class Result {
        public final double bestValue;
        public final List<Integer> chosenItems;

        private Result(double bestValue, List<Integer> chosenItems) {
            this.bestValue = bestValue;
            this.chosenItems = chosenItems;
        }

        @Override
        public String toString() {
            return "BestValue=" + bestValue + ", items=" + chosenItems;
        }
    }

    /**
     * Solves QKP via standard linearization + MILP solver.
     *
     * @param weights           w_i array, length n
     * @param linearProfits     p_i array, length n
     * @param quadraticProfits  symmetric n×n matrix P_{ij}
     * @param capacity          knapsack capacity C
     * @return optimal objective value and chosen item indices
     */
    public static Result solve(double[] weights,
                               double[] linearProfits,
                               double[][] quadraticProfits,
                               double capacity) {
        Loader.loadNativeLibraries();
        int n = weights.length;
        if (n != linearProfits.length || n != quadraticProfits.length) {
            throw new IllegalArgumentException("Input dimension mismatch");
        }

        // --- Create solver ---------------------------------------------------
        MPSolver solver = MPSolver.createSolver("SCIP"); // fallback to CBC if SCIP not built
        if (solver == null) {
            throw new IllegalStateException("Could not create MILP solver (SCIP/CBC)");
        }

        // --- Variables -------------------------------------------------------
        MPVariable[] x = new MPVariable[n];
        for (int i = 0; i < n; i++) {
            x[i] = solver.makeIntVar(0, 1, "x_" + i);
        }

        MPVariable[][] z = new MPVariable[n][n]; // only i<j used
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                z[i][j] = solver.makeNumVar(0.0, 1.0, "z_" + i + "_" + j);
            }
        }

        // --- Capacity constraint -------------------------------------------
        MPConstraint capacityCt = solver.makeConstraint(Double.NEGATIVE_INFINITY, capacity, "capacity");
        for (int i = 0; i < n; i++) {
            capacityCt.setCoefficient(x[i], weights[i]);
        }

        // --- Linearization constraints -------------------------------------
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                MPVariable zij = z[i][j];
                // z_ij <= x_i
                MPConstraint c1 = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0, "c1_" + i + "_" + j);
                c1.setCoefficient(zij, 1);
                c1.setCoefficient(x[i], -1);

                // z_ij <= x_j
                MPConstraint c2 = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0, "c2_" + i + "_" + j);
                c2.setCoefficient(zij, 1);
                c2.setCoefficient(x[j], -1);

                // x_i + x_j - z_ij <= 1   (equivalent to x_i + x_j - 1 <= z_ij)
                MPConstraint c3 = solver.makeConstraint(Double.NEGATIVE_INFINITY, 1, "c3_" + i + "_" + j);
                c3.setCoefficient(x[i], 1);
                c3.setCoefficient(x[j], 1);
                c3.setCoefficient(zij, -1);
            }
        }

        // --- Objective -------------------------------------------------------
        MPObjective obj = solver.objective();
        for (int i = 0; i < n; i++) {
            obj.setCoefficient(x[i], linearProfits[i]);
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                obj.setCoefficient(z[i][j], quadraticProfits[i][j]);
            }
        }
        obj.setMaximization();

        // --- Solve -----------------------------------------------------------
        MPSolver.ResultStatus status = solver.solve();
        if (status != MPSolver.ResultStatus.OPTIMAL && status != MPSolver.ResultStatus.FEASIBLE) {
            throw new IllegalStateException("No feasible solution found (status = " + status + ")");
        }

        double bestValue = obj.value();
        List<Integer> items = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (x[i].solutionValue() > 0.5) items.add(i);
        }

        return new Result(bestValue, items);
    }

    // ----------------------- Mini-demo --------------------------------------
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