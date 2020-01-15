package org.testany.fakerpp.core.engine.generator

import org.testany.fakerpp.core.engine.generator.builtin.DateGen
import org.testany.fakerpp.core.engine.generator.builtin.DateRangeGen
import org.testany.fakerpp.core.engine.generator.builtin.EnumGen
import org.testany.fakerpp.core.engine.generator.builtin.IntGen
import spock.lang.Shared
import spock.lang.Specification

class GeneratorsTest extends Specification {

    @Shared
    Generators generators

    def setupSpec() {
        generators = new Generators()
    }

    def "get built-in class by tag name"() {
        expect:
        def genConsHandler = generators.getConsByBuiltInTag("date-range")
        // can not invoke 'invokeExact' in Groovy
        // so we use invokeWithArguments instead
        def genor = genConsHandler.mh.invokeWithArguments()
        genor instanceof DateRangeGen
    }

    def "get built-in class field setter"() {
        given:
        def gField = generators.getFieldSetter(clazz, fieldName)
        gField.mh.invokeWithArguments(instance, setVal)

        expect:
        instance.@"${fieldName}" == setVal

        where:
        clazz         | instance      | fieldName | setVal
        IntGen.class  | new IntGen()  | "min"     | 10001
        DateGen.class | new DateGen() | "end"     | "noenow"
        EnumGen.class | new EnumGen() | "options" | ["aaa", "bbb"]
    }

    def "get built in generator object"(String tag, Map attrs, List options, Map exp) {
        given:
        def generator = generators.builtInGenerator(tag, attrs, options)

        expect:
        exp.each {
            k, v ->
                assert generator.@"${k}" == v
        }

        where:
        tag          | attrs                        | options          | exp
        "int"        | [min: "10", max: "11"]       | []               | [min: 10, max: 11]
        "date-range" | [start: "end-1", end: "now"] | []               | [start: "end-1", end: "now"]
        "enum"       | [:]                          | [["a"], ["poi"]] | ["options": [["a"], ["poi"]]]
    }

}
