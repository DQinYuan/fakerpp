package org.testany.fakerpp.core.util

import spock.lang.Specification

class MyListUtilTest extends Specification {
    def "test transposeView"() {
        given:
        List<List<Integer>> testList = [[1,2],[3,4]]

        when:
        def view = MyListUtil.transposeView(testList)

        then:
        view == [[1,3], [2,4]]
    }
}
