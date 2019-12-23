package org.testany.fakerpp.core.engine.generator.builtin

import spock.lang.Specification

class StrGenTest extends Specification {

    def "generate random string with prefix, suffix and fix length"() {
        given:
        def prefix = "???"
        def suffix = "+++"
        def strGen = new StrGen(prefix: prefix, suffix: suffix, len: 3)
        strGen.init()

        expect:
        5.times {
            def data = strGen.nextData()
            assert data != null
            assert data.size() == 9
            assert data.startsWith(prefix)
            assert data.endsWith(suffix)
        }



    }

}
