package newgen;

import java.util.*;
import java.util.stream.IntStream;

/**
 * BenchmarkQKP – компактная экспериментальная среда для 0‑1 Quadratic
 * Knapsack Problem (QKP).
 * <p>
 * ▸ Генерирует случайные экземпляры (см. {@link #randomInstance}).<br>
 * ▸ Запускает шесть алгоритмов из пакета <code>newgen</code> и меряет время.<br>
 * ▸ Печатает <strong>CSV‑строки</strong> для дальнейшего анализа <em>и</em>
 * сводную <strong>ASCII‑таблицу</strong> со средними результатами – удобно
 * вставить прямо в доклад или методичку.
 * </p>
 * <p><b>Компиляция</b> (нужны *.java + OR‑Tools если используете LINEAR):
 * <pre>javac -cp .:ortools-java/lib/* newgen/*.java</pre>
 * <b>Запуск</b>:
 * <pre>java -cp .:ortools-java/lib/* newgen.BenchmarkQKP -sizes 10,12,14 -inst 15</pre>
 * </p>
 */
public final class BenchmarkQKP {

    /* ------------------------- data class ----------------------------- */

    /** Случайный экземпляр QKP. */
    public static final class Instance {
        public final int[] weights;
        public final double[] linear;
        public final double[][] quad;
        public final int capacity;
        Instance(int[] w, double[] p, double[][] Q, int C) {
            this.weights = w; this.linear = p; this.quad = Q; this.capacity = C;
        }
    }

    /* --------------------- результат одного солвера ------------------- */

    public static final class SolverResult {
        public final double value;       // целевая функция
        public final List<Integer> items;// индексы предметов
        public final long timeNs;        // время, нс
        SolverResult(double v, List<Integer> it, long t) {
            this.value = v; this.items = it; this.timeNs = t;
        }
    }

    /* -------------- функциональный интерфейс-обёртка ------------------ */
    @FunctionalInterface
    public interface QKPSolver {
        SolverResult solve(Instance inst);
    }

    /* ---------------- helper: int[] ➜ double[] ------------------------ */
    private static double[] toDouble(int[] a) {
        double[] d = new double[a.length];
        for (int i = 0; i < a.length; i++) d[i] = a[i];
        return d;
    }

    /* ---------------- random instance generator ----------------------- */
    public static Instance randomInstance(int n, double capacityRatio,
                                          int maxWeight, double maxLinear, double maxQuad,
                                          Random rnd) {
        int[] w = new int[n];
        double[] p = new double[n];
        double[][] Q = new double[n][n];
        int sumW = 0;
        for (int i = 0; i < n; i++) {
            w[i] = 1 + rnd.nextInt(maxWeight);
            p[i] = rnd.nextDouble() * maxLinear;
            sumW += w[i];
        }
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double v = rnd.nextDouble() * maxQuad;
                Q[i][j] = Q[j][i] = v;
            }
        }
        int C = (int) Math.round(capacityRatio * sumW);
        return new Instance(w, p, Q, C);
    }

    /* ---------------- конкретные обёртки ------------------------------ */
    private static final QKPSolver SOLVER_DP = inst -> {
        long t0 = System.nanoTime();
        DynamicProgrammingQKP.Result r = DynamicProgrammingQKP.solve(
                inst.weights, inst.linear, inst.quad, inst.capacity);
        long t1 = System.nanoTime();
        return new SolverResult(r.value, r.items, t1 - t0);
    };

    private static final QKPSolver SOLVER_GREEDY = inst -> {
        long t0 = System.nanoTime();
        GreedyHeuristicQKP.Result r = GreedyHeuristicQKP.solve(
                toDouble(inst.weights), inst.linear, inst.quad, inst.capacity);
        long t1 = System.nanoTime();
        return new SolverResult(r.value, r.items, t1 - t0);
    };

    private static final QKPSolver SOLVER_TABU = inst -> {
        long t0 = System.nanoTime();
        TabuSearchQKP.Result r = TabuSearchQKP.solve(
                toDouble(inst.weights), inst.linear, inst.quad, inst.capacity,
                /*maxIter=*/1000, /*tenure=*/5);
        long t1 = System.nanoTime();
        return new SolverResult(r.value, r.items, t1 - t0);
    };

    private static final QKPSolver SOLVER_LINEAR = inst -> {
        try {
            long t0 = System.nanoTime();
            StandardLinearizationQKP.Result r = StandardLinearizationQKP.solve(
                    toDouble(inst.weights), inst.linear, inst.quad, inst.capacity);
            long t1 = System.nanoTime();
            return new SolverResult(r.bestValue, r.chosenItems, t1 - t0);
        } catch (Throwable ex) { // OR‑Tools не найден – пропускаем
            return new SolverResult(Double.NaN, List.of(), 0);
        }
    };

    private static final QKPSolver SOLVER_CLASSIC = inst -> {
        long t0 = System.nanoTime();
        Knapsack01Classic.Result r = Knapsack01Classic.solve(
                inst.weights, inst.linear, inst.capacity);
        long t1 = System.nanoTime();
        return new SolverResult(r.bestValue, r.chosenItems, t1 - t0);
    };

    private static final QKPSolver SOLVER_BRUTE = inst -> {
        long t0 = System.nanoTime();
        QuadraticKnapsackBruteForce.Result r = QuadraticKnapsackBruteForce.solve(
                toDouble(inst.weights), inst.linear, inst.quad, inst.capacity);
        long t1 = System.nanoTime();
        return new SolverResult(r.bestValue, r.bestSubset, t1 - t0);
    };

    /* ---------------- реестр для удобного обхода ---------------------- */
    private static final Map<String, QKPSolver> ALL_SOLVERS;
    static {
        Map<String,QKPSolver> m = new LinkedHashMap<>();
        m.put("DP",      SOLVER_DP);
        m.put("GREEDY",  SOLVER_GREEDY);
        m.put("TABU",    SOLVER_TABU);
        m.put("LINEAR",  SOLVER_LINEAR);
        m.put("CLASSIC", SOLVER_CLASSIC);
        m.put("BRUTE",   SOLVER_BRUTE);
        ALL_SOLVERS = Collections.unmodifiableMap(m);
    }

    /* -------------------- статистика для сводки ---------------------- */
    private static final class Stat {
        double timeSum = 0;
        double gapSum  = 0;
        int    nTime   = 0;
        int    nGap    = 0;
        void add(double timeMs, Double gap) {
            timeSum += timeMs; nTime++;
            if (!gap.isNaN()) { gapSum += gap; nGap++; }
        }
        double meanTime() { return timeSum / Math.max(1, nTime); }
        String meanGap()  { return nGap == 0 ? "-" : String.format(Locale.US, "%.2f", gapSum / nGap); }
    }

    /* ---------------- experiment runner -------------------------------- */
    public static void main(String[] args) {
        int[] sizes = {10,12,14,16,18,20,22,24};
        int instancesPerSize = 10;
        double capacityRatio = 0.5;
        long seed = 42;

        // простой парсер аргументов
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-sizes": sizes = Arrays.stream(args[++i].split(",")).mapToInt(Integer::parseInt).toArray(); break;
                case "-inst":  instancesPerSize = Integer.parseInt(args[++i]); break;
                case "-ratio": capacityRatio = Double.parseDouble(args[++i]); break;
                case "-seed":  seed = Long.parseLong(args[++i]); break;
                default:        System.err.println("Неизвестный аргумент: " + args[i]);
            }
        }

        System.out.println("n,instance,solver,value,time_ms,gap_to_brute_%");
        Random rnd = new Random(seed);

        // summary[n][solver] -> Stat
        Map<Integer, Map<String, Stat>> summary = new TreeMap<>();

        for (int n : sizes) {
            for (int instId = 0; instId < instancesPerSize; instId++) {
                Instance inst = randomInstance(n, capacityRatio, 20, 10.0, 2.0, rnd);

                SolverResult brute = (n <= 22) ? ALL_SOLVERS.get("BRUTE").solve(inst) : null;
                double opt = brute != null ? brute.value : Double.NaN;

                for (Map.Entry<String,QKPSolver> e : ALL_SOLVERS.entrySet()) {
                    String name = e.getKey();
                    if (name.equals("BRUTE") && brute == null) continue; // large n – пропускаем

                    SolverResult res = name.equals("BRUTE") ? brute : e.getValue().solve(inst);
                    double timeMs = res.timeNs / 1e6;
                    double gap = Double.NaN;
                    if (brute != null && !Double.isNaN(res.value)) {
                        gap = (res.value - opt) / Math.max(1e-9, Math.abs(opt)) * 100.0;
                    }
                    System.out.printf(Locale.US, "%d,%d,%s,%.4f,%.3f,%s%n",
                            n, instId, name, res.value, timeMs,
                            Double.isNaN(gap) ? "" : String.format(Locale.US,"%.2f", gap));

                    // -- обновляем summary --
                    summary.computeIfAbsent(n, k -> new LinkedHashMap<>())
                            .computeIfAbsent(name, k -> new Stat())
                            .add(timeMs, gap);
                }
            }
        }

        // -------------------- печать красивой сводки ---------------------
        System.out.println("\n========= СРЕДНИЕ ПО " + instancesPerSize + " ЭКЗЕМПЛЯРАМ =========");
        System.out.printf("%4s | %-7s | %12s | %12s\n", "n", "solver", "gap_%", "time_ms");
        System.out.println("-----+---------+--------------+--------------");
        for (Map.Entry<Integer, Map<String, Stat>> en : summary.entrySet()) {
            int n = en.getKey();
            Map<String, Stat> map = en.getValue();
            for (String solver : ALL_SOLVERS.keySet()) {
                if (!map.containsKey(solver)) continue; // например BRUTE не считался
                Stat st = map.get(solver);
                System.out.printf(Locale.US, "%4d | %-7s | %12s | %12.3f\n",
                        n, solver, st.meanGap(), st.meanTime());
            }
        }
    }
}
