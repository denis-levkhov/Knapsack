package diploma.runners;

import diploma.entity.Result;
import diploma.solver.BruteForce;
import diploma.solver.Dp;
import diploma.solver.DpMultiSorted;
import diploma.solver.Greedy;
import diploma.solver.TabuSearch;
import diploma.solver.TaskSolver;
import diploma.solver.ZeroOneClassicDp;

public class LocalExample {

    public static void main(String[] args) {
        int n = 3;
        int W = 4;
        int[] weights = {2, 3, 1};

        int[][] P = {
                {10, 5, 0},
                {5, 12, 3},
                {0, 3, 8}
        };

        TaskSolver bruteForce = new BruteForce();
        Result result = bruteForce.solve(n, W, weights, P);
        System.out.println("bruteforce: " + result);

        TaskSolver dpFomeni = new Dp();
        result = dpFomeni.solve(n, W, weights, P);
        System.out.println("fomeni: " + result);

        TaskSolver dpMultiSorted = new DpMultiSorted();
        result = dpMultiSorted.solve(n, W, weights, P);
        System.out.println("multisorted: " + result);

        TaskSolver greedy = new Greedy();
        result = greedy.solve(n, W, weights, P);
        System.out.println("greedy: " + result);

        TaskSolver tabu = new TabuSearch();
        result = tabu.solve(n, W, weights, P);
        System.out.println("tabu: " + result);

        TaskSolver zo = new ZeroOneClassicDp();
        result = zo.solve(n, W, weights, P);
        System.out.println("classic: " + result);
    }
}
