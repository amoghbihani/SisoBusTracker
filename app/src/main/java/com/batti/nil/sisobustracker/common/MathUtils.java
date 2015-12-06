package com.batti.nil.sisobustracker.common;

public class MathUtils {
    public static double max(double a, double b) {
        return Math.max(a, b);
    }

    public static double min(double a, double b) {
        return Math.min(a, b);
    }

    public static double max(double a, double b, double c) {
        double max = a;
        if (max < b) max = b;
        if (max < c) max = c;
        return max;
    }

    public static double min(double a, double b, double c) {
        double min = a;
        if (min > b) min = b;
        if (min > c) min = c;
        return min;
    }
}
