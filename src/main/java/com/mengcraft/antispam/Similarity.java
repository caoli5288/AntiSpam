package com.mengcraft.antispam;

/**
 * Created on 16-10-11.
 */
public class Similarity {

    private int distance;
    private double similarity;

    private Similarity() {
    }

    public int getDistance() {
        return distance;
    }

    public double get() {
        return similarity;
    }

    private static int min(int i, int j, int k) {
        return Math.min(Math.min(i, j), k);
    }

    private static int ld(String x, String y) {
        int n = x.length();
        int m = y.length();
        if (n == 0) {
            return m;
        }
        if (m == 0) {
            return n;
        }
        int d[][] = new int[n + 1][m + 1];
        int i;
        int j;
        int k;
        for (i = 0; i <= n; i++) {
            d[i][0] = i;
        }
        for (j = 0; j <= m; j++) {
            d[0][j] = j;
        }
        char u;
        char r;
        for (i = 1; i <= n; i++) {
            u = x.charAt(i - 1);
            for (j = 1; j <= m; j++) {
                r = y.charAt(j - 1);
                if (u == r) {
                    k = 0;
                } else {
                    k = 1;
                }
                d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + k);
            }
        }
        return d[n][m];
    }

    private static double similarity(int ld, double length) {
        return 1 - ld / length;
    }

    public static Similarity process(String i, String j) {
        Similarity similarity = new Similarity();
        if (i == null || j == null) return similarity;
        int ld = ld(i, j);
        similarity.distance = ld;
        similarity.similarity = similarity(ld, Math.max(i.length(), j.length()));
        return similarity;
    }

}
