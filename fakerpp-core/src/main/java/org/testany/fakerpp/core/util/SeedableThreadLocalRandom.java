package org.testany.fakerpp.core.util;

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

    /**
     *
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
     *
     * @param origin the least value returned
     * @param bound the upper bound (exclusive)
     * @return a pseudorandom {@code int} value between the origin
     *         (inclusive) and the bound (exclusive)
     * @throws IllegalArgumentException if {@code origin} is greater than
     *         or equal to {@code bound}
     */
    public static int nextInt(int origin, int bound) {
        return origin + threadLocal.get().nextInt(bound - origin);
    }


}
