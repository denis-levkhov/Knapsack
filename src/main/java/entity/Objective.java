package entity;

import java.util.List;

public final class Objective {

    /** полная ценность: сумма стоимостей + синергия между парами  */
    public static double value(List<Item> set,
                               double[][] s,
                               List<Item> universe) {

        double v = 0;
        for (int a = 0; a < set.size(); a++) {
            Item ia = set.get(a);
            v += ia.getValue();
            for (int b = a + 1; b < set.size(); b++) {
                int i = universe.indexOf(ia);
                int j = universe.indexOf(set.get(b));
                v += s[i][j];
            }
        }
        return v;
    }
}