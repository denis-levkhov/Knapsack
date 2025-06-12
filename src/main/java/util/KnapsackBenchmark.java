package util;

import entity.Item;
import solver.BruteForceKnapsackSolver;
import solver.DynamicProgrammingKnapsackSolver;
import solver.GreedyKnapsackSolver;
import solver.HeuristicKnapsackSolver;
import solver.KnapsackSolver;
import solver.ZeroOneKnapsackSolver;
import java.util.*;

public class KnapsackBenchmark {

    private static final int ITEM_COUNT = 30;
    private static final double MAX_WEIGHT = 50;
    private static final double MAX_VALUE = 200;
    private static final double MAX_SYNERGY = 20;
    private static final double CAPACITY = 100;

    public static void main(String[] args) {
        List<Item> items = generateRandomItems();
        double[][] synergyMatrix = generateSynergyMatrix(items);

        Map<String, KnapsackSolver> solvers = Map.of(
                "Greedy", new GreedyKnapsackSolver(),
                "Dynamic", new DynamicProgrammingKnapsackSolver(),
                "ZeroOne", new ZeroOneKnapsackSolver(),
                "Heuristic", new HeuristicKnapsackSolver(),
                "diploma.solver.BruteForce", new BruteForceKnapsackSolver()
        );

        List<Map<String, Object>> results = new ArrayList<>();

        for (Map.Entry<String, KnapsackSolver> entry : solvers.entrySet()) {
            String name = entry.getKey();
            KnapsackSolver solver = entry.getValue();

            long startTime = System.nanoTime();
            List<Item> solution = solver.solve(items, synergyMatrix, CAPACITY);
            long endTime = System.nanoTime();

            double totalValue = solution.stream().mapToDouble(Item::getValue).sum();
            double synergyBonus = calculateSynergy(solution, synergyMatrix);
            totalValue += synergyBonus;

            double totalWeight = solution.stream().mapToDouble(Item::getWeight).sum();
            double duration = (endTime - startTime) / 1_000_000.0;

            System.out.printf("%s Solution:%n", name);
            System.out.printf("Total Value: %.2f | Total Weight: %.2f | Time: %.2f ms%n",
                    totalValue, totalWeight, duration);
            System.out.println("Items: " + solution);
            System.out.println("Synergy Bonus: " + synergyBonus);
            System.out.println("----------------------------------");

            Map<String, Object> result = new HashMap<>();
            result.put("Algorithm", name);
            result.put("TotalValue", totalValue);
            result.put("TotalWeight", totalWeight);
            result.put("Time", duration);
            result.put("Items", solution);
            results.add(result);
        }

        results.sort((a, b) -> Double.compare((double) b.get("TotalValue"), (double) a.get("TotalValue")));

        System.out.println("\n========= Рейтинг алгоритмов =========");
        int rank = 1;
        for (Map<String, Object> result : results) {
            System.out.printf("%d. %s → Value: %.2f | Weight: %.2f | Time: %.2f ms\n",
                    rank++,
                    result.get("Algorithm"),
                    result.get("TotalValue"),
                    result.get("TotalWeight"),
                    result.get("Time"));
        }
        System.out.println("======================================\n");
    }

    private static List<Item> generateRandomItems() {
        List<Item> items = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < ITEM_COUNT; i++) {
            String name = "Item" + i;
            double weight = 1 + random.nextDouble() * MAX_WEIGHT;
            double value = 1 + random.nextDouble() * MAX_VALUE;
            items.add(new Item(name, weight, value));
        }

        return items;
    }

    private static double[][] generateSynergyMatrix(List<Item> items) {
        int n = items.size();
        double[][] synergyMatrix = new double[n][n];
        Random random = new Random();

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                synergyMatrix[i][j] = random.nextDouble() * MAX_SYNERGY;
                synergyMatrix[j][i] = synergyMatrix[i][j];
            }
        }

        return synergyMatrix;
    }

    private static double calculateSynergy(List<Item> items, double[][] synergyMatrix) {
        double totalSynergy = 0.0;
        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                int index1 = Integer.parseInt(items.get(i).getName().replace("Item", ""));
                int index2 = Integer.parseInt(items.get(j).getName().replace("Item", ""));
                totalSynergy += synergyMatrix[index1][index2];
            }
        }
        return totalSynergy;
    }
}
