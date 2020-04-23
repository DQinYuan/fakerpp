package org.testd.fakerpp.core.engine.generator.faker

import javassist.ClassPool
import org.testd.fakerpp.core.engine.generator.Generator
import org.testd.fakerpp.core.engine.generator.GeneratorSupplier
import spock.lang.Shared
import spock.lang.Specification

class FakersTest extends Specification {

    @Shared
    Fakers fakers

    def setupSpec() {
        fakers = new Fakers(new FakerFactory(), new FakerInvoker())
    }

    def mockParamInfo(String name, Class<?> relType, Object defaultValue) {
        return new GeneratorSupplier.ParamInfo(name, relType, defaultValue, false) {
            @Override
            void setValue(Generator generator, String value) {
            }
        }
    }

    def "get faker gen"(String field, String generator,
                        Map<String, String> attrs, Map exp,
                        Map<String, GeneratorSupplier.ParamInfo> expParamInfo) {
        expect:
        def fakerGens = fakers.fakerGenerators(ClassPool.getDefault())
        GeneratorSupplier fakerGenSupplier = fakerGens[field][generator]
        def fakerGen = fakerGenSupplier.getInitedGenerator("en", attrs, [])
        def pInfos = fakerGenSupplier.paramInfos()
        // only expect it will not throw exception
        fakerGen.nextData()
        assert pInfos == expParamInfo

        where:
        field    | generator       | attrs                                                   | exp                                     | expParamInfo
        "number" | "random-double" | ["max-number-of-decimals": "3", "min": "1", "max": "9"] | ["max-number-of-decimals": 3,
                                                                                                "min"                   : 1, "max": 9] |
                ["max-number-of-decimals": mockParamInfo("max-number-of-decimals", int.class,
                        null),
                 "min"                   : mockParamInfo("min", int.class, null),
                 "max"                   : mockParamInfo("max", int.class, null)]
        "beer"   | "name"          | [:]                                                     | [:] | [:]
        "name"   | "full-name"     | [:]                                                     | [:] | [:]
    }

    def "there is no field without generator"() {
        when:
        def fakerGens = fakers.fakerGenerators(ClassPool.getDefault())

        then:
        fakerGens.each {
            fieldName, gens ->
                assert !gens.isEmpty() : "Field '${fieldName}' without generator"
        }
    }

}
