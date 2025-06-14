package diploma.solver;

import diploma.entity.Result;

import java.util.*;

public class TabuSearch implements TaskSolver {

    @Override
    public Result solve(int n, int W, int[] weights, int[][] P) {
        final int maxIterations = 1000;
        final int tabuTenure = 100;

        boolean[] currentSolution = generateGreedyInitialSolution(n, W, weights, P);
        boolean[] bestSolution = Arrays.copyOf(currentSolution, n);
        int bestProfit = evaluate(currentSolution, weights, P, W);

        Set<String> tabuList = new HashSet<>();
        Deque<String> tabuQueue = new ArrayDeque<>();

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            List<boolean[]> neighbors = generateNeighbors(currentSolution);
            boolean[] bestCandidate = null;
            int bestCandidateProfit = Integer.MIN_VALUE;

            for (boolean[] neighbor : neighbors) {
                if (totalWeight(neighbor, weights) > W) continue;

                String key = Arrays.toString(neighbor);
                int profit = evaluate(neighbor, weights, P, W);

                if (!tabuList.contains(key) || profit > bestProfit) {
                    if (profit > bestCandidateProfit) {
                        bestCandidateProfit = profit;
                        bestCandidate = neighbor;
                    }
                }
            }

            if (bestCandidate == null) break;

            currentSolution = bestCandidate;
            String moveKey = Arrays.toString(currentSolution);
            tabuList.add(moveKey);
            tabuQueue.add(moveKey);

            if (tabuQueue.size() > tabuTenure) {
                String oldest = tabuQueue.poll();
                tabuList.remove(oldest);
            }

            if (bestCandidateProfit > bestProfit) {
                bestSolution = Arrays.copyOf(currentSolution, n);
                bestProfit = bestCandidateProfit;
            }
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

    private static boolean[] generateGreedyInitialSolution(int n, int W, int[] weights, int[][] P) {
        boolean[] used = new boolean[n];
        int currentWeight = 0;
        List<Integer> selected = new ArrayList<>();

        while (true) {
            int bestItem = -1;
            double bestRatio = -1;

            for (int i = 0; i < n; i++) {
                if (used[i] || weights[i] + currentWeight > W) continue;

                int profit = P[i][i];
                for (int j : selected) {
                    profit += P[i][j]; // односторонняя синергия
                }

                double ratio = (double) profit / weights[i];
                if (ratio > bestRatio) {
                    bestRatio = ratio;
                    bestItem = i;
                }
            }

            if (bestItem == -1) break;

            used[bestItem] = true;
            selected.add(bestItem);
            currentWeight += weights[bestItem];
        }

        boolean[] result = new boolean[n];
        for (int idx : selected) result[idx] = true;
        return result;
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
            for (int j = 0; j < i; j++) {
                if (solution[j]) profit += P[i][j]; // однократный учёт синергии
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