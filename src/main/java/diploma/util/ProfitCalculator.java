package diploma.util;

import java.util.List;

public class ProfitCalculator {
    public static int calculateProfit(List<Integer> items, int[][] P) {
        int profit = 0;
        for (int i = 0; i < items.size(); i++) {
            int u = items.get(i);
            profit += P[u][u];
            for (int j = 0; j < i; j++) {
                int v = items.get(j);
                profit += P[u][v];
            }
        }
        return profit;
    }
}