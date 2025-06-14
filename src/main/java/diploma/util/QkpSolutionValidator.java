package diploma.util;

import java.util.List;

public class QkpSolutionValidator {

    public static int calculateTotalWeight(int[] weights, List<Integer> items) {
        int total = 0;
        for (int i : items) {
            total += weights[i];
        }
        return total;
    }

    public static int calculateTotalProfit(int[] weights, int[][] P, List<Integer> items, int capacity) {
        int totalWeight = calculateTotalWeight(weights, items);
        if (totalWeight > capacity) {
            throw new IllegalArgumentException("Solution exceeds capacity!");
        }

        int profit = 0;
        for (int i = 0; i < items.size(); i++) {
            int itemI = items.get(i);
            profit += P[itemI][itemI];

            for (int j = i + 1; j < items.size(); j++) {
                int itemJ = items.get(j);
                profit += P[itemI][itemJ];
            }
        }
        return profit;
    }

    public static boolean isValid(int[] weights, List<Integer> items, int capacity) {
        return calculateTotalWeight(weights, items) <= capacity;
    }
}