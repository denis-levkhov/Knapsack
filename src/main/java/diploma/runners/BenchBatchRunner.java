package diploma.runners;

import diploma.config.ThreadPool;
import diploma.entity.Result;
import diploma.input.InputData;
import diploma.solver.*;
import diploma.util.ProfitCalculator;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

import static diploma.input.InputData.readQkpFromFile;

public class BenchBatchRunner {
    private static final int MAX_BRUTE_N = 22;
    private static final long TIME_LIMIT_MS = 20000;

    private static final List<TaskSolver> solvers = List.of(
            new BruteForce(),
            new Dp(),
            new DpMultiSorted(),
            new Greedy(),
            new TabuSearchClassic(),
            new ZeroOneClassicDp(),
            new GreedyClassic()
    );

    public static void main(String[] args) throws IOException {
        String folderPath = "billionnet";
        Files.list(Paths.get(folderPath))
                .filter(path -> path.toString().endsWith(".txt"))
                .sorted()
                .forEach(BenchBatchRunner::runBenchmarkOnFile);

        ThreadPool.shutdown();
    }

    private static void runBenchmarkOnFile(Path filePath) {
        System.out.println("\n===============================");
        System.out.println("Тест из файла: " + filePath.getFileName());
        System.out.println("===============================");

        try {
            InputData.QkpInstance data = readQkpFromFile(filePath.toString());

            int n = data.n;
            int[] weights = data.weights;
            int[][] P = data.profits;
            int W = data.W;
            Integer optimalProfit = null;
            int dpReferenceProfit = -1;

            boolean useBruteForce = n <= MAX_BRUTE_N;

            if (useBruteForce) {
                Result bruteResult = runWithTimeout(new BruteForce(), n, W, weights, P, TIME_LIMIT_MS);
                optimalProfit = ProfitCalculator.calculateProfit(bruteResult.getItems(), P);
                System.out.println("[Полный перебор]: " + optimalProfit);
            }

            System.out.printf("n = %d | W = %d\n", n, W);
            System.out.printf("%-20s %-10s %-10s %-10s\n", "Алгоритм", "Время(μs)", "Ценность", "Точность");

            FileWriter writer = new FileWriter("results_" + filePath.getFileName() + ".csv");
            writer.write("Algorithm,Time(μs),Profit,Accuracy\n");

            for (TaskSolver solver : solvers) {
                if (solver instanceof BruteForce && !useBruteForce) continue;

                long startNano = System.nanoTime();
                Result result = runWithTimeout(solver, n, W, weights, P, TIME_LIMIT_MS);
                long durationMicros = (System.nanoTime() - startNano) / 1000;

                int profit = ProfitCalculator.calculateProfit(result.getItems(), P);

                if (solver instanceof Dp) {
                    dpReferenceProfit = profit;
                }

                int reference = (optimalProfit != null && optimalProfit > 0)
                        ? optimalProfit
                        : dpReferenceProfit;

                double accuracy = (reference <= 0) ? -1 : (double) profit / reference;
                String accStr = (accuracy < 0) ? "-" : String.format(Locale.US, "%.2f", accuracy);

                System.out.printf("%-20s %-10d %-10d %-10s\n",
                        solver.getClass().getSimpleName(), durationMicros, profit, accStr);
                writer.write(String.format(Locale.US, "%s,%d,%d,%s\n",
                        solver.getClass().getSimpleName(), durationMicros, profit, accStr));
            }

            writer.close();
        } catch (Exception e) {
            System.err.println("Ошибка при обработке файла " + filePath.getFileName() + ": " + e.getMessage());
        }
    }

    private static Result runWithTimeout(TaskSolver solver, int n, int W, int[] weights, int[][] P, long timeoutMs) {
        try {
            CompletableFuture<Result> future = CompletableFuture.supplyAsync(
                    () -> solver.solve(n, W, weights, P), ThreadPool.EXECUTOR
            );
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            System.out.println(solver.getClass().getSimpleName() + " превысил лимит времени");
        } catch (Exception e) {
            System.out.println(solver.getClass().getSimpleName() + " ошибка: " + e.getMessage());
        }
        return new Result(-1, List.of());
    }
}