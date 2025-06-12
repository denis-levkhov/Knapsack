/*
package diploma.solver;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import diploma.entity.Result;

import java.util.ArrayList;
import java.util.List;

public class StandardLinearOrTools implements TaskSolver {

    @Override
    public Result solve(int n, int W, int[] weights, int[][] P) {
        Loader.loadNativeLibraries();

        double[] w = new double[n];
        double[] p = new double[n];
        double[][] Q = new double[n][n];

        for (int i = 0; i < n; i++) {
            w[i] = weights[i];
            p[i] = P[i][i];
            for (int j = 0; j < n; j++) {
                Q[i][j] = P[i][j];
            }
        }

        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) throw new IllegalStateException("Solver not found");

        MPVariable[] x = new MPVariable[n];
        MPVariable[][] z = new MPVariable[n][n];

        for (int i = 0; i < n; i++) {
            x[i] = solver.makeIntVar(0, 1, "x_" + i);
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                z[i][j] = solver.makeNumVar(0.0, 1.0, "z_" + i + "_" + j);
            }
        }

        MPConstraint capacityCt = solver.makeConstraint(Double.NEGATIVE_INFINITY, W);
        for (int i = 0; i < n; i++) {
            capacityCt.setCoefficient(x[i], w[i]);
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                MPConstraint c1 = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0);
                c1.setCoefficient(z[i][j], 1);
                c1.setCoefficient(x[i], -1);

                MPConstraint c2 = solver.makeConstraint(Double.NEGATIVE_INFINITY, 0);
                c2.setCoefficient(z[i][j], 1);
                c2.setCoefficient(x[j], -1);

                MPConstraint c3 = solver.makeConstraint(Double.NEGATIVE_INFINITY, 1);
                c3.setCoefficient(x[i], 1);
                c3.setCoefficient(x[j], 1);
                c3.setCoefficient(z[i][j], -1);
            }
        }

        MPObjective obj = solver.objective();
        for (int i = 0; i < n; i++) {
            obj.setCoefficient(x[i], p[i]);
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                obj.setCoefficient(z[i][j], Q[i][j]);
            }
        }
        obj.setMaximization();

        MPSolver.ResultStatus status = solver.solve();
        if (status != MPSolver.ResultStatus.OPTIMAL && status != MPSolver.ResultStatus.FEASIBLE) {
            return Result.builder().profit(0).items(List.of()).build();
        }

        double bestValue = obj.value();
        List<Integer> items = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (x[i].solutionValue() > 0.5) items.add(i);
        }

        return Result.builder()
                .profit((int) Math.round(bestValue))
                .items(items)
                .build();
    }
}*/
