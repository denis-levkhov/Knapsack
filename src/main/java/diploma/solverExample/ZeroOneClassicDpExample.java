package diploma.solverExample;

import java.util.ArrayList;
import java.util.List;


// основная суть запоминать лучшие варианты на каждом шаге - это и есть динамическое программирование
// поэтому мы используем таблицу, чтобы запоминать лучшие варианты на каждом шаге
public class ZeroOneClassicDpExample {

    int n = 3; // количество предметов, добавим потом как вводить с клавиатуры
    int maxWeight = 4;// максимальный вес нашего рюкзака
    int[] weights = {2, 3, 1}; // веса предметов.
    int[] values = {10, 12, 8}; // ценность предметов или полезность
    int[][] dp;
    // шаг 1 инит
    // создаётся таблица dp[n+1][W+1] - все ячейки по умолчанию равны 0
    // строки - 0...Weight максимальный вес
    // столбцы - 0...n количество элементов

    /*шаг 2 заполняем таблицу
    * рассматриваем два вариант, не брать предмет т.е. просто копируем значение из предыдущей строки
    * взять элемент т.е. идём назад по wi по весу и добавляем vi
    * сравниваем эти два варианта и берём максимум */
    public int solve(int n, int[] weights, int[] values, int maxWeight) {
        this.dp = new int[n + 1][maxWeight + 1]; // строит таблицу dp[i][w], которая говорит, что у нас есть i предметов
        // и у нас есть w свободного веса, то какая максимальная ценность возможна
        // далее как заполняем
        /*
        * 1. сможем ли мы взять этот предмет при текущем весе?
        * 2. если не можем - оставляем старое значение (предмет не берём)
        * 3. если можем - сравниваем -
        *  а что будет если не брать предмет?
        *  а что буде если взять его и добавить его ценность к тому, что было до него
        * Выбираем лучший вариант из двух*/
        for (int i = 1; i <= n; i++) {
            int weight = weights[i - 1];
            int value = values[i - 1];

            for (int w = 0; w <= maxWeight; w++) {
                if (weight > w) {
                    dp[i][w] = dp[i - 1][w];
                } else {
                    dp[i][w] = Math.max(dp[i - 1][w], dp[i - 1][w - weight] + value);
                }
            }
        }
        return dp[n][maxWeight];
    }

    // восстановление элементов, пока не подключал
    public static List<Integer> getSelectedItems(int[][] dp, int[] weights, int n, int W) {
        List<Integer> selected = new ArrayList<>();
        int w = W;

        for (int i = n; i > 0; i--) {
            if (dp[i][w] != dp[i - 1][w]) {
                selected.add(i - 1);
                w -= weights[i - 1];
            }
        }

        return selected; // содержит индексы выбранных предметов
    }

    public static void main(String[] args) {
        ZeroOneClassicDpExample zeroOneClassicDp = new ZeroOneClassicDpExample();
        int result = zeroOneClassicDp.solve(zeroOneClassicDp.n, zeroOneClassicDp.weights, zeroOneClassicDp.values, zeroOneClassicDp.maxWeight);
        System.out.println(result);

        List<Integer> selectedItems = getSelectedItems(zeroOneClassicDp.dp, zeroOneClassicDp.weights, zeroOneClassicDp.n, zeroOneClassicDp.maxWeight);
        System.out.print("Выбранные предметы: ");
        for (int index : selectedItems) {
            System.out.print(index + " ");
        }
        System.out.println();
    }
}
