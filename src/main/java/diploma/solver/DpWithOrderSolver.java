package diploma.solver;

import diploma.entity.Result;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DpWithOrderSolver implements TaskSolver {
    private final Comparator<Integer> comparator;
    private final String strategyName;

    public DpWithOrderSolver(Comparator<Integer> comparator, String strategyName) {
        this.comparator = comparator;
        this.strategyName = strategyName;
    }

    @Override
    public Result solve(int n, int W, int[] weights, int[][] P) {
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < n; i++) order.add(i);
        order.sort(comparator);
        return DpMultiSorted.solveDP(order, W, weights, P);
    }

    @Override
    public String toString() {
        return strategyName;
    }
}