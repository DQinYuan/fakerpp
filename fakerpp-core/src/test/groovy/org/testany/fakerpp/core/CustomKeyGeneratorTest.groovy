package org.testany.fakerpp.core

import spock.lang.Specification

class CustomKeyGeneratorTest extends Specification {

    def "generate key"() {
        given:
        def keyGen = new CustomKeyGenerator()
        def s = "Hello "
        def method = String.class.getMethod("concat", String.class)

        when:
        def key = keyGen.generate(s, method, "World", CustomKeyGeneratorTest.class)

        then:
        key == "java.lang.String_concat_World_org.testany.fakerpp.core.CustomKeyGeneratorTest"
    }

}
