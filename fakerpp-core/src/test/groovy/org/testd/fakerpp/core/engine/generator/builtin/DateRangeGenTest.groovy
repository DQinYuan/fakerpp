package org.testd.fakerpp.core.engine.generator.builtin

import spock.lang.Specification

class DateRangeGenTest extends Specification {

    def "generate all date in [start, end)"() {
        given:
        def format = "yyyy-MM-dd"
        def start = "2019-09-01"
        def end = "2019-09-05"

        when:
        def dateRangeGen = new DateRangeGen(format: format, start: start, end: end)
        dateRangeGen.init(0)

        then:
        def exps = ["2019-09-01", "2019-09-02", "2019-09-03", "2019-09-04"]
        dateRangeGen.dataNum() == exps.size()
        exps.each {
            exp -> assert exp == dateRangeGen.nextData()[0]
        }
    }

    def "date range gen default param test"() {
        given:
        def gen = new DateRangeGen()
        gen.start = "end-3"

        when:
        gen.init(0)

        then:
        gen.dataNum() == 3
    }

}
