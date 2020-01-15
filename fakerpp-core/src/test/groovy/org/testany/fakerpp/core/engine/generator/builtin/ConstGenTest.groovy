package org.testany.fakerpp.core.engine.generator.builtin

import spock.lang.Specification

class ConstGenTest extends Specification {

    def "generate constant data"() {
        given:
        def data = [["a", "b"], ["c", "d"], ["e", "f"]]

        when:
        def gen = new ConstGen()
        gen.options = data
        gen.init()

        then:
        gen.dataNum() == data.size()
        data.each {
            d -> assert d == gen.nextData()
        }
    }


}
