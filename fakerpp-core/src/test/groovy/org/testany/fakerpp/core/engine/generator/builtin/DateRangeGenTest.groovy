package org.testany.fakerpp.core.engine.generator.builtin

import spock.lang.Specification

class DateRangeGenTest extends Specification {

    def "generate all date in [start, end)"() {
        given:
        def format = "yyyy-MM-dd"
        def start = "2019-09-01"
        def end = "2019-09-05"

        when:
        def dateRangeGen = new DateRangeGen(format: format, start: start, end: end)
        dateRangeGen.init()

        then:
        def exps = ["2019-09-01", "2019-09-02", "2019-09-03", "2019-09-04"]
        dateRangeGen.dataNum() == exps.size()
        exps.each {
            exp -> assert exp == dateRangeGen.nextData()[0]
        }
    }

}
