package diploma.runners;

import diploma.config.ThreadPool;
import diploma.entity.Result;
import diploma.solver.*;
import diploma.util.ProfitCalculator;

import java.util.*;
import java.util.concurrent.*;

public class Bench {
    private static final int MAX_BRUTE_N = 22;
    private static final long TIME_LIMIT_MS = 20000;

    private static final List<TaskSolver> solvers = List.of(
            new BruteForce(),
            new DpFomeni(),
            new DpMultiSorted(),
            new Greedy(),
            new TabuSearch(),
            new ZeroOneClassicDp()
    );

    public static void main(String[] args) {
        QkpDataIO.LoadedInstance instance;
        try {
            instance = QkpDataIO.loadInstance("instance_20.txt");
        } catch (Exception e) {
            System.err.println("Failed to load input instance: " + e.getMessage());
            return;
        }

        int n = instance.weights.length;
        int[] weights = instance.weights;
        int[][] P = instance.P;

        for (int test = 1; test <= 5; test++) {
            int W = 50 + test * 5;
            Integer optimalProfit = null;

            if (n <= MAX_BRUTE_N) {
                Result bruteResult = runWithTimeout(new BruteForce(), n, W, weights, P, TIME_LIMIT_MS);
                optimalProfit = ProfitCalculator.calculateProfit(bruteResult.getItems(), P);
                System.out.println("\n[BruteForce optimal: " + optimalProfit + "]");
            }

            System.out.printf("\nTest %d | n = %d | maxWeight = %d\n", test, n, W);
            System.out.printf("%-20s %-10s %-10s %-10s\n", "Algorithm", "Time(ms)", "Profit", "Accuracy");

            for (TaskSolver solver : solvers) {
                if (solver instanceof BruteForce && n > MAX_BRUTE_N) {
                    System.out.printf("%-20s %-10s %-10s %-10s\n", solver.getClass().getSimpleName(), "-", "-", "-");
                    continue;
                }

                long start = System.currentTimeMillis();
                Result result = runWithTimeout(solver, n, W, weights, P, TIME_LIMIT_MS);
                long duration = System.currentTimeMillis() - start;

                int profit = ProfitCalculator.calculateProfit(result.getItems(), P);

                double accuracy = (optimalProfit == null || optimalProfit <= 0) ? -1 : (double) profit / optimalProfit;
                String accStr = (accuracy < 0) ? "-" : String.format("%.2f", accuracy);

                System.out.printf("%-20s %-10d %-10d %-10s\n",
                        solver.getClass().getSimpleName(), duration, profit, accStr);
            }
        }
        ThreadPool.shutdown();
    }

    public static Result runWithTimeout(TaskSolver solver, int n, int W, int[] weights, int[][] P, long timeoutMs) {
        try {
            CompletableFuture<Result> future = CompletableFuture.supplyAsync(() -> solver.solve(n, W, weights, P), ThreadPool.EXECUTOR);
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            System.out.println(solver.getClass().getSimpleName() + " exceeded time limit");
        } catch (Exception e) {
            System.out.println(solver.getClass().getSimpleName() + " failed: " + e.getMessage());
        }
        return new Result(-1, List.of());
    }
}