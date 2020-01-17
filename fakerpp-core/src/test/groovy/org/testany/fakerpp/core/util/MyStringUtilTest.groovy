package org.testany.fakerpp.core.util

import spock.lang.Specification

class MyStringUtilTest extends Specification {

    def "test delimit2Camel"() {
        given:
        def origin = "data-range-ab"

        when:
        def upper = MyStringUtil.delimit2Camel(origin, true)
        def lower = MyStringUtil.delimit2Camel(origin, false)

        then:
        upper == "DataRangeAb"
        lower == "dataRangeAb"
    }

    def "test prepareSQL"(){
        given:
        def tableName = "test"
        def cols = ["a", "b", "c"]
        def recordNum = 3

        when:
        def pSql = MyStringUtil.prepareInsertSQL(tableName, cols, recordNum)

        then:
        pSql == "INSERT INTO test(a,b,c) values (?,?,?),(?,?,?),(?,?,?)"
    }

}
