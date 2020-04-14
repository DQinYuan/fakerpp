package org.testd.fakerpp.core.engine.generator.faker

import org.apache.commons.lang3.ClassUtils
import spock.lang.Shared
import spock.lang.Specification

class FakersTest extends Specification {

    @Shared
    Fakers fakers

    def setupSpec() {
        fakers = new Fakers(new FakerFactory(), new FakerInvoker())
    }

    def "get faker gen"(String field, String generator,
                        Map<String, String> attrs, Map exp) {
        expect:
        def fakerGens = fakers.fakerGenerators()
        def fakerGen = fakerGens[field][generator].getGenerator("en", attrs, [])
        def pInfos = fakerGens[field][generator].paramInfos()
        // only expect it will not throw exception
        fakerGen.nextData()
        exp.forEach({ k, v ->
            assert pInfos[k] != null
            assert ClassUtils.primitiveToWrapper(pInfos[k].type).isAssignableFrom(v.class)
        })


        where:
        field    | generator       | attrs                                                   | exp
        "number" | "random-double" | ["max-number-of-decimals": "3", "min": "1", "max": "9"] | ["max-number-of-decimals": 3,
                                                                                                "min"                   : 1, "max": 9]
        "beer"   | "name"          | [:]                                                     | [:]
        "name"   | "full-name"     | [:]                                                     | [:]

    }

}
