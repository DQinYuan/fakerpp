package org.testd.fakerpp.core.engine.generator.builtin

import org.testd.fakerpp.core.ERMLException
import spock.lang.Specification

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DateGenTest extends Specification {

    void assertInRange(DateGen dateGen, DateTimeFormatter formatter, long rangeLeft, long rangeRight) {
        5.times {
            def data = dateGen.nextData()[0]
            def dataEpoch = LocalDate.parse(data, formatter).toEpochDay()
            assert dataEpoch >= rangeLeft
            assert dataEpoch < rangeRight
        }
    }

    def "generate date in range [end-60, now)"() {
        given:
        def format = "yyyy-MM-dd"
        def start = "end-60"
        def end = "now"

        when:
        def dateGen = new DateGen(format:format, start: start, end:end)
        dateGen.init(0)
        def rangeRight = LocalDate.now().toEpochDay()
        def rangeLeft = rangeRight - 60
        def formatter = DateTimeFormatter.ofPattern(format)

        then:
        assertInRange(dateGen, formatter, rangeLeft, rangeRight)
    }

    def "generate date in range [yyyy-MM-dd1, yy-MM-dd2)"() {
        given:
        def format = "yyyy-MM-dd"
        def start = "2018-10-10"
        def end = "2018-10-13"

        when:
        def dateGen = new DateGen(format:format, start: start, end:end)
        dateGen.init(0)
        def formatter = DateTimeFormatter.ofPattern(format)
        def rangeRight = LocalDate.parse(end, formatter).toEpochDay()
        def rangeLeft = LocalDate.parse(start, formatter).toEpochDay()


        then:
        assertInRange(dateGen, formatter, rangeLeft, rangeRight)
    }

    def "custom format"() {
        given:
        def format = "yyyyMMdd"
        def start = "20181010"
        def end = "20181013"

        when:
        def dateGen = new DateGen(format:format, start: start, end:end)
        dateGen.init(0)
        def formatter = DateTimeFormatter.ofPattern(format)
        def rangeRight = LocalDate.parse(end, formatter).toEpochDay()
        def rangeLeft = LocalDate.parse(start, formatter).toEpochDay()


        then:
        assertInRange(dateGen, formatter, rangeLeft, rangeRight)
    }

    def "end date earlier than start will throw exception"() {
        given:
        def format = "yyyyMMdd"
        def start = "20181010"
        def end = "20181010"

        when:
        def dateGen = new DateGen(format:format, start: start, end:end)
        dateGen.init(0)

        then:
        ERMLException e = thrown()
        e.getMessage() == "<date> end date can not be earlier than or equal start date"
    }

    def "if end expr is illegal, it will throw exception"() {
        given:
        def format = "yyyyMMdd"
        def start = " endd - 0"
        def end = "now"

        when:
        def dateGen = new DateGen(format:format, start: start, end:end)
        dateGen.init(0)

        then:
        ERMLException e = thrown()
        e.getMessage() == "<date start='...'> expression left expected to be 'end', not 'endd'"
    }

}
