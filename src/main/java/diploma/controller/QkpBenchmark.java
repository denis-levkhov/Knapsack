package diploma.controller;

import diploma.entity.Result;
import diploma.solver.*;

import java.util.*;
import java.util.concurrent.*;

public class QkpBenchmark {
    static final int MAX_BRUTE_N = 20;
    static final long TIME_LIMIT_MS = 5000;

    static final List<TaskSolver> solvers = List.of(
            new BruteForce(),
            new DpFomeni(),
            new DpMultiSorted(),
            new Greedy(),
            new TabuSearch(),
            new ZeroOneClassicDp()
    );

    public static void main(String[] args) {
        for (int test = 1; test <= 5; test++) {
            int n = 100 + test * 2;
            int maxWeight = 20 + test * 3;

            int[] weights = generateWeights(n);
            int[][] P = generateProfitMatrix(n);

            Integer optimalProfit = null;
            if (n <= MAX_BRUTE_N) {
                optimalProfit = runWithTimeout(new BruteForce(), n, maxWeight, weights, P, TIME_LIMIT_MS).getProfit();
                System.out.println("\n[BruteForce optimal: " + optimalProfit + "]");
            }

            System.out.printf("\nTest %d | n = %d | maxWeight = %d\n", test, n, maxWeight);
            System.out.printf("%-20s %-10s %-10s %-10s\n", "Algorithm", "Time(ms)", "Profit", "Accuracy");

            for (TaskSolver solver : solvers) {
                if (solver instanceof BruteForce && n > MAX_BRUTE_N) {
                    System.out.printf("%-20s %-10s %-10s %-10s\n", solver.getClass().getSimpleName(), "-", "-", "-");
                    continue;
                }

                long start = System.currentTimeMillis();
                Result result = runWithTimeout(solver, n, maxWeight, weights, P, TIME_LIMIT_MS);
                long duration = System.currentTimeMillis() - start;

                double accuracy = (optimalProfit == null || optimalProfit <= 0) ? -1 : (double) result.getProfit() / optimalProfit;
                String accStr = (accuracy < 0) ? "-" : String.format("%.2f", accuracy);

                System.out.printf("%-20s %-10d %-10d %-10s\n",
                        solver.getClass().getSimpleName(), duration, result.getProfit(), accStr);
            }
        }
    }

    public static Result runWithTimeout(TaskSolver solver, int n, int W, int[] weights, int[][] P, long timeoutMs) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Result> future = executor.submit(() -> solver.solve(n, W, weights, P));
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            System.out.println(solver.getClass().getSimpleName() + " exceeded time limit");
        } catch (Exception e) {
            System.out.println(solver.getClass().getSimpleName() + " failed: " + e.getMessage());
        } finally {
            executor.shutdownNow();
        }
        return new Result(-1, List.of());
    }

    public static int[] generateWeights(int n) {
        Random random = new Random();
        int[] weights = new int[n];
        for (int i = 0; i < n; i++) {
            weights[i] = 1 + random.nextInt(10);
        }
        return weights;
    }

    public static int[][] generateProfitMatrix(int n) {
        Random random = new Random();
        int[][] P = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                int value = random.nextInt(10);
                P[i][j] = value;
                P[j][i] = value; // симметричная матрица
            }
        }
        return P;
    }
}