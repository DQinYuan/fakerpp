package org.testany.fakerpp.core.engine.generator.faker


import spock.lang.Shared
import spock.lang.Specification

class FakersTest extends Specification {

    @Shared
    Fakers fakers

    def setupSpec() {
        fakers = new Fakers(new FakerFactory(), new FakerInvoker())
    }

    def "get faker gen"(String field, String generator,
                        Map<String, String> attrs, Map<String, List<String>> listAttrs) {
        expect:
        def fakerGen = fakers.fakerGenerator("en", field,
                generator,
                attrs,
                listAttrs
        )
        fakerGen.nextData()

        where:
        field    | generator       | attrs                                          | listAttrs
        "number" | "random-double" | ["maxNumberOfDecimals": 3, "min": 1, "max": 9] | [:]
        "beer"   | "name"          | [:]                                            | [:]
        "name"   | "full-name"     | [:]                                            | [:]

    }

}
