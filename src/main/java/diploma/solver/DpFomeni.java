package diploma.solver;

import diploma.entity.Result;

import java.util.ArrayList;
import java.util.List;

public class DpFomeni implements TaskSolver {


    @Override
    public Result solve(int n, int W, int[] weights, int[][] P) {
        int[][] dp = new int[n + 1][W + 1];
        // как и в обычном dp инициализируем таблицу для получения максимальной прибыли
        List<Integer>[][] selected = new ArrayList[n + 1][W + 1];
        // сюда мы будем записывать выбранные элементы.

        for (int k = 0; k <= n; k++) {
            for (int w = 0; w <= W; w++) {
                selected[k][w] = new ArrayList<>();
            }
        }



        for (int predmet = 1; predmet <= n; predmet++) {
            int vesTekushegoPredmeta = weights[predmet - 1];

            for (int ves = 0; ves <= W; ves++) {
                // 1 вариант - Не берём предмет k
                // просто копируем значение, если мы элемент не берём
                dp[predmet][ves] = dp[predmet - 1][ves];
                selected[predmet][ves] = new ArrayList<>(selected[predmet - 1][ves]);

                // 2 вариант - взять предмет k

                if (ves >= vesTekushegoPredmeta) { // если предмет помещается в рюкзак, при остатке веса r, мы рассматриваем его для включения
                    int predidushayYacheyka = ves - vesTekushegoPredmeta; // предполагаем, что на предыдущем шаге мы уже заполнили рюкзак до r - weight
                    List<Integer> itemPredStep = selected[predmet - 1][predidushayYacheyka];// это те предметы, которые привели к максимальной ценности при весе r - weight без учёта k

                    // Вычисляем вклад предмета k с уже выбранными
                    int profit = P[predmet - 1][predmet - 1]; // профит нашего предмета, берём значение по диагонали
                    for (int i : itemPredStep) { // для выбранных вещей мы сравниваем пары
                        profit += P[i][predmet - 1] + P[predmet - 1][i]; // считаем профит от всех взаимодействий
                    }
                    int totalProfit = dp[predmet - 1][predidushayYacheyka] + profit; // итоговый профит будет профит

                    if (totalProfit > dp[predmet][ves]) {
                        dp[predmet][ves] = totalProfit;
                        selected[predmet][ves] = new ArrayList<>(itemPredStep);
                        selected[predmet][ves].add(predmet - 1);
                    }
                }
            }
        }

        // Поиск наилучшего результата
        int bestProfit = 0;
        List<Integer> bestItems = new ArrayList<>();
        for (int r = 0; r <= W; r++) {
            if (dp[n][r] > bestProfit) {
                bestProfit = dp[n][r];
                bestItems = selected[n][r];
            }
        }

        return new Result(bestProfit, bestItems);
    }
}
