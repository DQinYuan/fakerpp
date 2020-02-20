package org.testd.fakerpp.core.util;

import com.github.javafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Random;

public class SeedableThreadLocalRandom {

    private static ThreadLocal<Random> threadLocal = ThreadLocal.withInitial(Random::new);

    public static void setSeed(long seed) {
        threadLocal.get().setSeed(seed);
    }

    public static long nextLong(long bound) {
        Random random = threadLocal.get();
        long r = next63bit(random);
        long m = bound - 1;
        for (long u = r;
             u - (r = u % bound) + m < 0;
             u = next63bit(random))
            ;
        return r;
    }

    public static double nextDouble() {
        return threadLocal.get().nextDouble();
    }

    /**
     * @param origin the least value returned
     * @param bound  the upper bound (exclusive)
     * @return a pseudorandom {@code double} value between the origin
     * (inclusive) and the bound (exclusive)
     * @throws IllegalArgumentException if {@code origin} is greater than
     *                                  or equal to {@code bound}
     */
    public static double nextDouble(double origin, double bound) {
        double r = nextDouble();
        r = r * (bound - origin) + origin;
        if (r >= bound) // correct for rounding
            r = Math.nextDown(bound);
        return r;
    }

    /**
     * @param random
     * @return positive random long
     */
    private static long next63bit(Random random) {
        return (random.nextLong() << 1) >>> 1;
    }

    public static int nextInt(int bound) {
        return threadLocal.get().nextInt(bound);
    }

    /**
     * @param origin the least value returned
     * @param bound  the upper bound (exclusive)
     * @return a pseudorandom {@code int} value between the origin
     * (inclusive) and the bound (exclusive)
     * @throws IllegalArgumentException if {@code origin} is greater than
     *                                  or equal to {@code bound}
     */
    public static int nextInt(int origin, int bound) {
        return origin + threadLocal.get().nextInt(bound - origin);
    }

    /**
     * <p>Creates a random string whose length is the number of characters
     * specified.</p>
     *
     * <p>Characters will be chosen from the set of Latin alphabetic
     * characters (a-z, A-Z) and the digits 0-9.</p>
     *
     * @param count the length of random string to create
     * @return the random string
     */
    public static String randomAlphanumeric(final int count) {
        return RandomStringUtils.random(count, 0, 0, true,
                true, null, threadLocal.get());
    }

    public static Faker newFaker(String lang) {
        return new Faker(new Locale(lang), threadLocal.get());
    }

    public static <T> T randomItemInList(List<T> list) {
        return list.get(threadLocal.get().nextInt(list.size()));
    }
}
