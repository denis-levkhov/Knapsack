package diploma.solverExample;

import diploma.entity.Result;

import java.util.*;

public class TabuSearchExample {

    public static Result solve(int n, int capacity, int[] weights, int[][] P, int maxIterations, int tabuTenure) {
        Random random = new Random();

        boolean[] current = generateInitialSolution(n, capacity, weights);
        boolean[] best = Arrays.copyOf(current, n);
        int bestProfit = evaluate(current, weights, P, capacity);

        Map<String, Integer> tabuList = new HashMap<>();

        for (int iteration = 0; iteration < maxIterations; iteration++) {
            List<boolean[]> neighbors = generateNeighbors(current);
            boolean[] bestCandidate = null;
            int bestCandidateProfit = Integer.MIN_VALUE;

            for (boolean[] neighbor : neighbors) {
                if (totalWeight(neighbor, weights) > capacity) continue;

                String key = Arrays.toString(neighbor);
                int profit = evaluate(neighbor, weights, P, capacity);

                if (!tabuList.containsKey(key) || profit > bestProfit) {
                    if (profit > bestCandidateProfit) {
                        bestCandidateProfit = profit;
                        bestCandidate = neighbor;
                    }
                }
            }

            if (bestCandidate == null) break;

            current = bestCandidate;
            String moveKey = Arrays.toString(current);
            tabuList.put(moveKey, iteration + tabuTenure);

            if (bestCandidateProfit > bestProfit) {
                best = Arrays.copyOf(current, n);
                bestProfit = bestCandidateProfit;
            }

            int finalIteration = iteration;
            tabuList.entrySet().removeIf(e -> e.getValue() <= finalIteration);
        }

        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (best[i]) selected.add(i);
        }

        return new Result(bestProfit, selected);
    }

    private static boolean[] generateInitialSolution(int n, int capacity, int[] weights) {
        boolean[] solution = new boolean[n];
        int totalWeight = 0;
        for (int i = 0; i < n; i++) {
            if (totalWeight + weights[i] <= capacity) {
                solution[i] = true;
                totalWeight += weights[i];
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

    private static int evaluate(boolean[] solution, int[] weights, int[][] P, int capacity) {
        if (totalWeight(solution, weights) > capacity) return Integer.MIN_VALUE;
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

    public static void main(String[] args) {
        int n = 3;
        int capacity = 4;
        int[] weights = {2, 3, 1};
        int[][] P = {
                {10, 5, 0},
                {5, 12, 3},
                {0, 3, 8}
        };

        Result result = solve(n, capacity, weights, P, 100, 7);
        System.out.println("Tabu Search profit: " + result.getProfit());
        System.out.println("Selected items: " + result.getItems());
    }
}