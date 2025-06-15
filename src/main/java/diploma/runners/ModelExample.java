package diploma.runners;

import diploma.entity.Result;
import diploma.solver.Dp;
import diploma.util.QkpSolutionValidator;

public class ModelExample {

    public static void main(String[] args) {

        int n = 6;
        int W = 10;

        int[] weights = {2, 3, 1, 4, 2, 5};

        int[][] P = {
                {5, 2, 1, 0, 3, 2},
                {2, 6, 2, 1, 0, 1},
                {1, 2, 4, 1, 2, 0},
                {0, 1, 1, 7, 2, 3},
                {3, 0, 2, 2, 5, 1},
                {2, 1, 0, 3, 1, 6}
        };

        Dp dpFomeni = new Dp();
        Result result = dpFomeni.solve(n, W, weights, P);
        System.out.println(result);

        boolean valid = QkpSolutionValidator.isValid(weights, result.getItems(), W);
        int realProfit = QkpSolutionValidator.calculateTotalProfit(weights, P, result.getItems(), W);
        System.out.println("Valid: " + valid + ", Profit: " + realProfit);
    }
}
