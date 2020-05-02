package org.testd.fakerpp.core.engine.generator.builtin

import spock.lang.Specification

class TimeGenTest extends Specification {

    def "test gen random time"() {
        given:
        def timeGen = new TimeGen()

        when:
        timeGen.init(1)

        then:
        5.times {
            timeGen.nextData()
        }
    }

}
