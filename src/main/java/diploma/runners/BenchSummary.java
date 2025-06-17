package diploma.runners;

import diploma.config.ThreadPool;
import diploma.entity.Result;
import diploma.input.InputData;
import diploma.solver.*;
import diploma.util.ProfitCalculator;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

import static diploma.input.InputData.readQkpFromFile;

public class BenchSummary {
    private static final int MAX_BRUTE_N = 22;
    private static final long TIME_LIMIT_MS = 20000;

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

    private static final Map<String, List<Double>> accuracyMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        String folderPath = "billionnet";

        Files.list(Paths.get(folderPath))
                .filter(path -> path.toString().endsWith(".txt"))
                .sorted()
                .forEach(BenchSummary::runBenchmarkOnFile);

        printAveragedAccuracy();
        ThreadPool.shutdown();
    }

    private static void runBenchmarkOnFile(Path filePath) {
        System.out.println("Обработка файла: " + filePath.getFileName());

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
                System.out.println(" -> Запускается полный перебор...");
                Result bruteResult = runWithTimeout(new BruteForce(), n, W, weights, P, TIME_LIMIT_MS);
                optimalProfit = ProfitCalculator.calculateProfit(bruteResult.getItems(), P);
                System.out.println(" -> Полный перебор завершён. Оптимум: " + optimalProfit);
            }

            for (TaskSolver solver : solvers) {
                if (solver instanceof BruteForce && !useBruteForce) continue;

                System.out.println(" -> Запуск " + solver.getClass().getSimpleName() + "...");
                Result result = runWithTimeout(solver, n, W, weights, P, TIME_LIMIT_MS);
                int profit = ProfitCalculator.calculateProfit(result.getItems(), P);
                System.out.println("    ✓ " + solver.getClass().getSimpleName() + " завершён. Прибыль: " + profit);

                if (solver instanceof Dp) {
                    dpReferenceProfit = profit;
                }

                int reference = (optimalProfit != null && optimalProfit > 0)
                        ? optimalProfit
                        : dpReferenceProfit;

                if (reference <= 0) continue;

                double accuracy = (double) profit / reference;

                String solverName = solver.getClass().getSimpleName();
                accuracyMap.computeIfAbsent(solverName, k -> new ArrayList<>()).add(accuracy);
            }

        } catch (Exception e) {
            System.err.println("Ошибка при обработке файла " + filePath.getFileName() + ": " + e.getMessage());
        }

        System.out.println("Файл " + filePath.getFileName() + " завершён.\n");
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

    private static void printAveragedAccuracy() {
        System.out.println("\n===== Средняя точность по всем тестам =====");
        accuracyMap.forEach((solver, accuracies) -> {
            double avg = accuracies.stream().mapToDouble(d -> d).average().orElse(-1);
            System.out.printf("%-20s: %.4f\n", solver, avg);
        });
    }
}