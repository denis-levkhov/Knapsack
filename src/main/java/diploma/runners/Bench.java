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
    private static final Random RANDOM = new Random(123);

    private static final List<TaskSolver> solvers = List.of(
            new BruteForce(),
            new DpFomeni(),
            new DpMultiSorted(),
            new Greedy(),
            new TabuSearch(),
            new ZeroOneClassicDp()
    );

    public static void main(String[] args) {
        int n = 20;
        int[] weights = generateWeights(n);
        int[][] P = generateProfitMatrix(n);

        for (int test = 1; test <= 5; test++) {
            int W = 50 + test * 10;
            Integer optimalProfit = null;

            if (n <= MAX_BRUTE_N) {
                Result bruteResult = runWithTimeout(new BruteForce(), n, W, weights, P, TIME_LIMIT_MS);
                optimalProfit = ProfitCalculator.calculateProfit(bruteResult.getItems(), P);
                System.out.println("\n[BruteForce optimal: " + optimalProfit + "]");
            }

            System.out.printf("\nTest %d | n = %d | maxWeight = %d\n", test, n, W);
            System.out.printf("%-20s %-10s %-10s %-10s\n", "Algorithm", "Time(μs)", "Profit", "Accuracy");

            for (TaskSolver solver : solvers) {
                if (solver instanceof BruteForce && n > MAX_BRUTE_N) {
                    System.out.printf("%-20s %-10s %-10s %-10s\n", solver.getClass().getSimpleName(), "-", "-", "-");
                    continue;
                }

                long startNano = System.nanoTime();
                Result result = runWithTimeout(solver, n, W, weights, P, TIME_LIMIT_MS);
                long durationMicros = (System.nanoTime() - startNano) / 1000;

                int profit = ProfitCalculator.calculateProfit(result.getItems(), P);
                double accuracy = (optimalProfit == null || optimalProfit <= 0)
                        ? -1
                        : (double) profit / optimalProfit;
                String accStr = (accuracy < 0) ? "-" : String.format("%.2f", accuracy);

                System.out.printf("%-20s %-10d %-10d %-10s\n",
                        solver.getClass().getSimpleName(), durationMicros, profit, accStr);
            }
        }
        ThreadPool.shutdown();
    }

    public static int[] generateWeights(int n) {
        int[] weights = new int[n];
        for (int i = 0; i < n; i++) {
            weights[i] = 1 + RANDOM.nextInt(10);
        }
        return weights;
    }

    public static int[][] generateProfitMatrix(int n) {
        int[][] P = new int[n][n];
        for (int i = 0; i < n; i++) {
            P[i][i] = RANDOM.nextInt(30);
            for (int j = i + 1; j < n; j++) {
                int interaction = RANDOM.nextInt(15);
                P[i][j] = interaction;
                P[j][i] = interaction;
            }
        }
        return P;
    }

    public static Result runWithTimeout(TaskSolver solver, int n, int W, int[] weights, int[][] P, long timeoutMs) {
        try {
            CompletableFuture<Result> future = CompletableFuture.supplyAsync(
                    () -> solver.solve(n, W, weights, P), ThreadPool.EXECUTOR
            );
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            System.out.println(solver.getClass().getSimpleName() + " exceeded time limit");
        } catch (Exception e) {
            System.out.println(solver.getClass().getSimpleName() + " failed: " + e.getMessage());
        }
        return new Result(-1, List.of());
    }
}