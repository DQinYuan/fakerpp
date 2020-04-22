package org.testd.fakerpp.core.engine.generator


import org.testd.fakerpp.core.engine.generator.builtin.DateGen
import org.testd.fakerpp.core.engine.generator.builtin.EnumGen
import org.testd.fakerpp.core.engine.generator.builtin.IntGen
import spock.lang.Shared
import spock.lang.Specification

class GeneratorsTest extends Specification {

    @Shared
    Generators generators

    def setupSpec() {
        generators = new Generators()
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

    def mockParamInfo(String name, Class<?> relType, Object defaultValue) {
        return new GeneratorSupplier.ParamInfo(name, relType, defaultValue) {
            @Override
            void setValue(Generator generator, String value) {
            }
        }
    }

    def "get built in generator object"(String tag, Map attrs, List<List<String>> options, Map exp,
                                        boolean optionSetterNull,
                                        Map<String, GeneratorSupplier.ParamInfo> expParamInfo) {
        given:
        def builtInGens = generators.builtInGenerators()
        def genSupplier = builtInGens[tag]
        def generator = genSupplier.getInitedGenerator("", attrs, options)
        Map<String, GeneratorSupplier.ParamInfo> pInfos = genSupplier.paramInfos()

        expect:
        pInfos == expParamInfo
        genSupplier.optionSetter().isPresent() == !optionSetterNull
        exp.each {
            k, v ->
                assert generator.@"${k}" == v
        }


        where:
        tag          | attrs                        | options          | exp                           | optionSetterNull |
                expParamInfo
        "int"        | [min: "10", max: "11"]       | []               | [min: 10, max: 11]            | true             | [
                "min": mockParamInfo("min", int.class, 0L),
                "max": mockParamInfo("max", int.class, 100L)]
        "date-range" | [start: "end-1", end: "now"] | []               | [start: "end-1", end: "now"]  | true             | [
                "format": mockParamInfo("format", String.class, "yyyy-MM-dd"),
                "start" : mockParamInfo("start", String.class, "end-60"),
                "end"   : mockParamInfo("end", String.class, "now")
        ]
        "enum"       | [:]                          | [["a"], ["poi"]] | ["options": [["a"], ["poi"]]] | false            | [:]
    }

}
