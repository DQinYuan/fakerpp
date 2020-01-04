package org.testany.fakerpp.core.util

import spock.lang.Specification

class MyStringUtilTest extends Specification {

    def "convert line delimit string to camel"() {
        given:
        def origin = "data-range-ab"

        when:
        def upper = MyStringUtil.delimit2Camel(origin, true)
        def lower = MyStringUtil.delimit2Camel(origin, false)

        then:
        upper == "DataRangeAb"
        lower == "dataRangeAb"
    }

}
