package diploma.solver;

import diploma.entity.Result;

public interface TaskSolver {

    Result solve(int n, int W, int[] weights, int[][] P);
}
