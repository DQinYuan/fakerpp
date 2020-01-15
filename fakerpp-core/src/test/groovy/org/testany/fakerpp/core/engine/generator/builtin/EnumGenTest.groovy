package org.testany.fakerpp.core.engine.generator.builtin

import spock.lang.Specification

class EnumGenTest extends Specification {

    def "generate data from some optins"() {
        given:
        def options = [["a"], ["b"], ["c"]]

        when:
        def enumGen = new EnumGen(options: options)

        then:
        5.times {
            assert options.contains(enumGen.nextData())
        }

    }

}
