package org.testany.fakerpp.core.util

import spock.lang.Specification

class SeedableThreadLocalRandomTest extends Specification {

    def "test nextLong(bound)"(long bound) {
        expect:
        10.times {
            def nextLong = SeedableThreadLocalRandom.nextLong(bound)
            assert nextLong >= 0
            assert nextLong < bound
        }

        where:
        bound                         | _
        8                             | _
        32434434                      | _
        1                             | _
        2                             | _
        (long) Integer.MAX_VALUE + 19 | _
    }

    def "test nextInt(low, bound)"() {
        expect:
        def low = 9
        def bound = 11
        10.times {
            def nextInt = SeedableThreadLocalRandom.nextInt(low, bound)
            assert nextInt >= low
            assert nextInt < bound
        }
    }

    def "test nextDouble(origin, bound)"(double origin, double bound) {
        expect:
        5.times {
            def nextDouble = SeedableThreadLocalRandom.nextDouble(origin, bound)
            assert nextDouble >= origin
            assert nextDouble < bound
        }

        where:
        origin | bound
        -3     | -1
        0      |  3
        -1     |  0
        324    |  345
    }

}
