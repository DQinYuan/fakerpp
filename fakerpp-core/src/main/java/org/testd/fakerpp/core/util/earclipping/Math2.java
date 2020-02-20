package org.testd.fakerpp.core.util.earclipping;

public final class Math2 {

    public static boolean approximatelyEqual(double v1, double v2) {
        return Math.abs(v2 - v1) < 3 * Math.ulp(v1);
    }
}