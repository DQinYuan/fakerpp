package org.testd.fakerpp.core.xsdgen


import groovy.xml.MarkupBuilder
import javassist.ClassPool
import lombok.extern.slf4j.Slf4j
import org.testd.fakerpp.core.engine.generator.GeneratorSupplier
import org.testd.fakerpp.core.engine.generator.Generators
import org.testd.fakerpp.core.engine.generator.LogicType
import org.testd.fakerpp.core.engine.generator.faker.FakerFactory
import org.testd.fakerpp.core.engine.generator.faker.FakerInvoker
import org.testd.fakerpp.core.engine.generator.faker.Fakers

@Slf4j
abstract class Libs extends Script {

    Generators generators = new Generators(
            new Fakers(new FakerFactory(), new FakerInvoker())
    )

    static class ParamInfo {
        String name
        LogicType type
        Object defaultValue

        boolean equals(o) {
            if (this.is(o)) return true
            if (!(o instanceof ParamInfo)) return false

            ParamInfo paramInfo = (ParamInfo) o

            if (defaultValue != paramInfo.defaultValue) return false
            if (name != paramInfo.name) return false
            if (type != paramInfo.type) return false

            return true
        }

        int hashCode() {
            int result
            result = (name != null ? name.hashCode() : 0)
            result = 31 * result + (type != null ? type.hashCode() : 0)
            result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0)
            return result
        }


        @Override
        String toString() {
            return "ParamInfo{" +
                    "name='" + name + '\'' +
                    ", type=" + type +
                    ", defaultValue=" + defaultValue +
                    '}'
        }
    }

    static class Generator {
        String name
        List<ParamInfo> paramInfos
        boolean hasOption


        @Override
        String toString() {
            return "Generator{" +
                    "name='" + name + '\'' +
                    ", paramInfos=" + paramInfos +
                    '}'
        }
    }

    static class FakerField {
        String name
        List<Generator> gens

        @Override
        String toString() {
            return "FakerField{" +
                    "name='" + name + '\'' +
                    ", gens=" + gens +
                    '}'
        }
    }

    ParamInfo mapParamInfo(GeneratorSupplier.ParamInfo originInfo) {
        return new ParamInfo(
                name: originInfo.getName(),
                type: originInfo.getLogicType(),
                defaultValue: originInfo.getDefaultValue()
        )
    }

    FakerField mapFakerField(String fieldName, Map<String, GeneratorSupplier> gensMap) {
        return new FakerField(
                name: fieldName,
                gens: gensMap.collect {
                    genName, supplier ->
                        new Generator(
                                name: genName,
                                paramInfos: supplier.paramInfos().values().collect { mapParamInfo(it) },
                                hasOption: supplier.optionSetter().isPresent()
                        )
                }
        )
    }

    List<FakerField> allFields(ClassPool cp) {
        return generators.generators(cp).collect {
            mapFakerField(it.getKey(),
                    it.getValue())
        }
    }

    MarkupBuilder getXsdBuilder(Writer writer) {
        def xsd = new MarkupBuilder(writer)
        xsd.setDoubleQuotes(true)
        return xsd
    }

    // construct xsd with groovy markup template
    def formatField(FakerField field) {
        def writer = new StringWriter()
        def xsd = getXsdBuilder(writer)
        def type = "${field.name}Type".toString()


        xsd."xs:complexType"(name: type) {
            mkp.comment("${field.name} generators")
            // lazy xs:choice
            def choice = {
                "xs:choice" {
                    field.gens.each {
                        gen ->
                            if ((gen.paramInfos == null || gen.paramInfos.empty) && !gen.hasOption) {
                                "xs:element"(type: "baseGenType", name: gen.name)
                                mkp.comment("${gen.name} generator")
                                return
                            }
                            "xs:element"(name: gen.name) {
                                mkp.comment("${gen.name} generator")
                                "xs:complexType" {
                                    "xs:complexContent" {
                                        "xs:extension"(base: "baseGenType") {
                                            if (gen.hasOption) {
                                                "xs:sequence" {
                                                    "xs:element"(name: "options", type: "optionsType")
                                                }
                                            }
                                            gen.paramInfos.each {
                                                ParamInfo pInfo ->
                                                    def attr = [type: pInfo.type.xsdType, name: pInfo.name]
                                                    if (pInfo.defaultValue == null) {
                                                        attr["use"] = "required"
                                                    } else {
                                                        attr["default"] = pInfo.defaultValue.toString()
                                                    }
                                                    "xs:attribute"(attr)
                                            }
                                        }
                                    }
                                }
                            }
                    }
                }
            }

            field.name == "built-in" ?
                    choice() :
                    "xs:complexContent"() {
                        "xs:extension"(base: "baseFakerFieldType") {
                            choice()
                        }
                    }

        }

        return writer.toString()
    }

    def registerFields(List<FakerField> fields) {
        def writer = new StringWriter()
        def xsd = getXsdBuilder(writer)
        xsd."xs:group"(name: "anyOneGenerator") {
            mkp.comment("register all generators")
            "xs:choice" {
                fields.each {
                    "xs:element"(name: it.name, type: "${it.name}Type")
                }
            }
        }

        return writer.toString()
    }
}
