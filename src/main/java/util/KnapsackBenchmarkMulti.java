package util;

import entity.Item;
import solver.*;

import java.util.*;

/**
 * Многократный бенч-раннер: генерирует N случайных задач и
 * агрегирует средние значения + подсчитывает, сколько раз
 * каждый алгоритм оказался лучшим по стоимости.
 */
public class KnapsackBenchmarkMulti {

    /* ---------------- параметризация ---------------- */
    private static final int    ITERATIONS   = 30;  // сколько кейсов прогнать
    private static final int    ITEM_COUNT   = 20;
    private static final double MAX_WEIGHT   = 50;
    private static final double MAX_VALUE    = 200;
    private static final double MAX_SYNERGY  = 20;
    private static final double CAPACITY     = 100;
    /* ------------------------------------------------ */

    /** агрегатор метрик */
    private static class Stats {
        double sumValue = 0, sumWeight = 0, sumTime = 0;
        int wins = 0;
        void add(double v, double w, double t) {
            sumValue  += v;  sumWeight += w;  sumTime += t;
        }
    }

    public static void main(String[] args) {

        Map<String, KnapsackSolver> solvers = Map.of(
                "Greedy",     new GreedyKnapsackSolver(),
                "Heuristic",  new HeuristicKnapsackSolver(),
                "Dynamic",    new DynamicProgrammingKnapsackSolver(),
                "ZeroOne",    new ZeroOneKnapsackSolver(),   // ↖ классика 0-1 (Branch&Bound)
                "diploma.solver.BruteForce", new BruteForceKnapsackSolver()
        );

        Map<String, Stats> overall = new LinkedHashMap<>();
        solvers.keySet().forEach(k -> overall.put(k, new Stats()));

        Random rnd = new Random();

        /* ==================== главный цикл ==================== */
        for (int it = 1; it <= ITERATIONS; it++) {

            List<Item> items          = generateRandomItems(rnd);
            double[][] synergyMatrix  = generateSynergyMatrix(items, rnd);

            /* запустим все алгоритмы на одном и том же экземпляре */
            double bestValueThisRun = Double.NEGATIVE_INFINITY;

            Map<String, Double> thisRunValues = new HashMap<>();

            for (var e : solvers.entrySet()) {
                String name          = e.getKey();
                KnapsackSolver alg   = e.getValue();

                long t0 = System.nanoTime();
                List<Item> picked = alg.solve(items, synergyMatrix, CAPACITY);
                long t1 = System.nanoTime();

                double value  = picked.stream().mapToDouble(Item::getValue).sum()
                        + calculateSynergy(picked, synergyMatrix);
                double weight = picked.stream().mapToDouble(Item::getWeight).sum();
                double timeMs = (t1 - t0) / 1_000_000.0;

                overall.get(name).add(value, weight, timeMs);

                thisRunValues.put(name, value);
                bestValueThisRun = Math.max(bestValueThisRun, value);
            }

            /* отметим победителей в этом раунде */
            double finalBest = bestValueThisRun;
            thisRunValues.forEach((name, v) -> {
                if (Double.compare(v, finalBest) == 0)
                    overall.get(name).wins++;
            });
        }

        /* ===================== вывод отчёта ===================== */
        System.out.println("========= Итог по " + ITERATIONS + " случайным задачам =========");
        System.out.printf("%-10s | %-10s | %-10s | %-9s | %-5s%n",
                "Algorithm", "AvgValue", "AvgWeight", "AvgTime", "Wins");
        System.out.println("-----------------------------------------------------------");

        /* сортируем по средней стоимости убыв. */
        overall.entrySet().stream()
                .sorted((a,b)-> Double.compare(
                        b.getValue().sumValue / ITERATIONS,
                        a.getValue().sumValue / ITERATIONS))
                .forEach(e -> {
                    Stats s = e.getValue();
                    System.out.printf("%-10s | %10.2f | %10.2f | %8.2f | %5d%n",
                            e.getKey(),
                            s.sumValue  / ITERATIONS,
                            s.sumWeight / ITERATIONS,
                            s.sumTime   / ITERATIONS,
                            s.wins);
                });
        System.out.println("===========================================================");
    }

    /* ----------------- генерация данных ----------------- */

    private static List<Item> generateRandomItems(Random rnd) {
        List<Item> list = new ArrayList<>();
        for (int i = 0; i < ITEM_COUNT; i++) {
            list.add(new Item("Item"+i,
                    1 + rnd.nextDouble()*MAX_WEIGHT,
                    1 + rnd.nextDouble()*MAX_VALUE));
        }
        return list;
    }

    private static double[][] generateSynergyMatrix(List<Item> items, Random rnd) {
        int n = items.size();
        double[][] q = new double[n][n];
        for (int i=0;i<n;i++)
            for (int j=i+1;j<n;j++) {
                q[i][j] = q[j][i] = rnd.nextDouble()*MAX_SYNERGY;
            }
        return q;
    }

    private static double calculateSynergy(List<Item> set, double[][] q) {
        double res = 0;
        for (int i = 0; i < set.size(); i++)
            for (int j = i+1; j < set.size(); j++) {
                int id1 = Integer.parseInt(set.get(i).getName().replace("Item",""));
                int id2 = Integer.parseInt(set.get(j).getName().replace("Item",""));
                res += q[id1][id2];
            }
        return res;
    }
}