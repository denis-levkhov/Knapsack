package diploma.runners;

import diploma.config.ThreadPool;
import diploma.entity.Result;
import diploma.solver.*;
import diploma.util.ProfitCalculator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Bench {
    private static final int MAX_BRUTE_N = 22;
    private static final long TIME_LIMIT_MS = 20000;
    private static final Random RANDOM = new Random(1);

    private static final List<TaskSolver> solvers = List.of(
            new BruteForce(),
            new Dp(),
            new DpMultiSorted(),
            new Greedy(),
            new TabuSearch(),
            new TabuSearchClassic(),
            new ZeroOneClassicDp(),
            new GreedyClassic()
    );

    public static void main(String[] args) throws IOException {
        int n = 100;
        int[] weights = generateWeights(n);
        int[][] P = generateProfitMatrix(n);
        int[] dpReferenceProfits = new int[6];

//        InputData.QkpInstance data = readQkpFromFile("qmkp_100_100_10_001.txt");
//        int n = data.n;
//        int[] weights = data.weights;
//        int[][] P = data.profits;

        try (FileWriter writer = new FileWriter("benchmark_results.csv")) {
            writer.write("Test,Algorithm,Time(μs),Profit,Accuracy\n");

            for (int test = 1; test <= 5; test++) {
                int W = (int) (Arrays.stream(weights).sum() * 0.4);
//                int W = data.W;
                Integer optimalProfit = null;

                boolean useBruteForce = n <= MAX_BRUTE_N;
                if (useBruteForce) {
                    Result bruteResult = runWithTimeout(new BruteForce(), n, W, weights, P, TIME_LIMIT_MS);
                    optimalProfit = ProfitCalculator.calculateProfit(bruteResult.getItems(), P);
                    System.out.println("\n[Полный перебор: " + optimalProfit + "]");
                }

                System.out.printf("\nТест %d | n = %d | W = %d\n", test, n, W);
                System.out.printf("%-20s %-10s %-10s %-10s\n", "Алгоритм", "Время(μs)", "Ценность", "Точность");

                for (TaskSolver solver : solvers) {
                    String solverName = solver.getClass().getSimpleName();

                    if (solver instanceof BruteForce && !useBruteForce) {
                        continue;
                    }

                    long startNano = System.nanoTime();
                    Result result = runWithTimeout(solver, n, W, weights, P, TIME_LIMIT_MS);
                    long durationMicros = (System.nanoTime() - startNano) / 1000;

                    int profit = ProfitCalculator.calculateProfit(result.getItems(), P);

                    if (solver instanceof Dp) {
                        dpReferenceProfits[test] = profit;
                    }

                    int reference = (optimalProfit != null && optimalProfit > 0)
                            ? optimalProfit
                            : dpReferenceProfits[test];

                    double accuracy = (reference <= 0) ? -1 : (double) profit / reference;
                    String accStr = (accuracy < 0) ? "-" : String.format(Locale.US, "%.2f", accuracy);

                    System.out.printf("%-20s %-10d %-10d %-10s\n",
                            solverName, durationMicros, profit, accStr);
                    writer.write(String.format(Locale.US, "%d,%s,%d,%d,%s\n",
                            test, solverName, durationMicros, profit, accStr));
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка записи в CSV файл: " + e.getMessage());
        }

        ThreadPool.shutdown();
    }

    public static int[] generateWeights(int n) {
        int[] weights = new int[n];
        for (int i = 0; i < n; i++) {
            weights[i] = 5 + RANDOM.nextInt(3);
        }
        return weights;
    }

    public static int[][] generateProfitMatrix(int n) {
        int[][] P = new int[n][n];
        for (int i = 0; i < n; i++) {
            P[i][i] = 10 + RANDOM.nextInt(21);
            for (int j = i + 1; j < n; j++) {
                int interaction = 10 + RANDOM.nextInt(91);
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
            System.out.println(solver.getClass().getSimpleName() + " вышел за временной лимит");
        } catch (Exception e) {
            System.out.println(solver.getClass().getSimpleName() + " failed: " + e.getMessage());
        }
        return new Result(-1, List.of());
    }
}