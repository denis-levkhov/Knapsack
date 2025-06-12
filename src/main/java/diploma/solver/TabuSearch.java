package diploma.solver;

import diploma.entity.Result;

import java.util.*;

public class TabuSearch implements TaskSolver {

    @Override
    public Result solve(int n, int W, int[] weights, int[][] P) {
        final int maxIterations = 100;
        final int tabuTenure = 7;

        boolean[] currentSolution = generateInitialSolution(n, W, weights);
        boolean[] bestSolution = Arrays.copyOf(currentSolution, n);
        int bestProfit = evaluate(currentSolution, weights, P, W);

        Map<String, Integer> tabuList = new HashMap<>();

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            List<boolean[]> neighbors = generateNeighbors(currentSolution);
            boolean[] bestCandidate = null;
            int bestCandidateProfit = Integer.MIN_VALUE;

            for (boolean[] neighbor : neighbors) {
                if (totalWeight(neighbor, weights) > W) continue;

                String key = Arrays.toString(neighbor);
                int profit = evaluate(neighbor, weights, P, W);

                if (!tabuList.containsKey(key) || profit > bestProfit) {
                    if (profit > bestCandidateProfit) {
                        bestCandidateProfit = profit;
                        bestCandidate = neighbor;
                    }
                }
            }

            if (bestCandidate == null) break;

            currentSolution = bestCandidate;
            String moveKey = Arrays.toString(currentSolution);
            tabuList.put(moveKey, iteration + tabuTenure);

            if (bestCandidateProfit > bestProfit) {
                bestSolution = Arrays.copyOf(currentSolution, n);
                bestProfit = bestCandidateProfit;
            }

            int finalIteration = iteration;
            tabuList.entrySet().removeIf(e -> e.getValue() <= finalIteration);
        }

        List<Integer> selectedItems = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (bestSolution[i]) selectedItems.add(i);
        }

        return Result.builder()
                .profit(bestProfit)
                .items(selectedItems)
                .build();
    }

    private static boolean[] generateInitialSolution(int n, int W, int[] weights) {
        boolean[] solution = new boolean[n];
        int total = 0;
        for (int i = 0; i < n; i++) {
            if (total + weights[i] <= W) {
                solution[i] = true;
                total += weights[i];
            }
        }
        return solution;
    }

    private static List<boolean[]> generateNeighbors(boolean[] solution) {
        List<boolean[]> neighbors = new ArrayList<>();
        for (int i = 0; i < solution.length; i++) {
            boolean[] neighbor = Arrays.copyOf(solution, solution.length);
            neighbor[i] = !neighbor[i];
            neighbors.add(neighbor);
        }
        return neighbors;
    }

    private static int evaluate(boolean[] solution, int[] weights, int[][] P, int W) {
        if (totalWeight(solution, weights) > W) return Integer.MIN_VALUE;
        int profit = 0;
        for (int i = 0; i < solution.length; i++) {
            if (!solution[i]) continue;
            profit += P[i][i];
            for (int j = i + 1; j < solution.length; j++) {
                if (solution[j]) profit += P[i][j] + P[j][i];
            }
        }
        return profit;
    }

    private static int totalWeight(boolean[] solution, int[] weights) {
        int w = 0;
        for (int i = 0; i < solution.length; i++) {
            if (solution[i]) w += weights[i];
        }
        return w;
    }
}