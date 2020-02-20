package org.testd.fakerpp.core.xsdgen

import javassist.ClassPool
import spock.lang.Shared
import spock.lang.Specification

class LibsTest extends Specification {

    @Shared
    Libs libs = new Libs() {
        @Override
        Object run() {
            return null
        }
    }

    def "test fieldsInFakerJar"() {
        expect:
        def fields = libs.fieldsInFakerJar(ClassPool.getDefault())
        assert fields.find { it.name == "space" }.gens.find { it.name.startsWith("nasa") }.name == "nasa-space-craft"
        assert fields.find { it.name == "number" }.gens.find { it.name == "number-between" }.paramInfos ==
                [new Libs.ParamInfo(name: "min", type: Libs.SupportParamType.INT, defaultValue: null),
                 new Libs.ParamInfo(name: "max", type: Libs.SupportParamType.INT, defaultValue: null)]
        assert fields.find { it.name == "phone-number" }.name == "phone-number"
        assert fields.find {it.name == "options"} == null
        assert fields.find {it.name == "number"}.gens.find {it.name == "random-double"}
            .paramInfos.contains(
                new Libs.ParamInfo(name:  "max-number-of-decimals", type: Libs.SupportParamType.INT, defaultValue: null))
    }

    def "test buildInField"() {
        expect:
        def field = libs.buildInField(ClassPool.getDefault())
        assert field.gens.find { it.name == "const" }.paramInfos ==
                [new Libs.ParamInfo(name: "options", type: Libs.SupportParamType.LIST, defaultValue: null)]
        assert field.gens.find { it.name == "int" }.paramInfos.contains(new Libs.ParamInfo(name: "min",
                type: Libs.SupportParamType.INT, defaultValue: 0))
        assert field.gens.find { it.name == "date" }.paramInfos.contains(new Libs.ParamInfo(name: "end",
                type: Libs.SupportParamType.STRING, defaultValue: "now"))
    }

    def "test formatField"(Libs.FakerField ff, String expect) {
        expect:
        libs.formatField(ff) == expect

        where:
        ff | expect
        new Libs.FakerField(name: "built-in",
                gens: [new Libs.Generator(name: "const",
                        paramInfos: [new Libs.ParamInfo(name: "options",
                                type: Libs.SupportParamType.LIST, defaultValue: null)]),
                       new Libs.Generator(name: "int",
                               paramInfos:[new Libs.ParamInfo(name: "min",
                                       type: Libs.SupportParamType.INT, defaultValue: 0),
                                           new Libs.ParamInfo(name: "max",
                                                   type: Libs.SupportParamType.INT, defaultValue: 100)])]) | """<xs:complexType name="built-inType"><!-- built-in generators -->
  <xs:choice>
    <xs:element name="const"><!-- const generator -->
      <xs:complexType>
        <xs:complexContent>
          <xs:extension base="baseGenType">
            <xs:sequence>
              <xs:element name="options" type="optionsType" />
            </xs:sequence>
          </xs:extension>
        </xs:complexContent>
      </xs:complexType>
    </xs:element>
    <xs:element name="int"><!-- int generator -->
      <xs:complexType>
        <xs:complexContent>
          <xs:extension base="baseGenType">
            <xs:attribute type="xs:int" name="min" default="0" />
            <xs:attribute type="xs:int" name="max" default="100" />
          </xs:extension>
        </xs:complexContent>
      </xs:complexType>
    </xs:element>
  </xs:choice>
</xs:complexType>"""
        new Libs.FakerField(name: "name",
                gens: [new Libs.Generator(name: "full-name",
                            paramInfos: [])]) | """<xs:complexType name="nameType"><!-- name generators -->
  <xs:complexContent>
    <xs:extension base="baseFakerFieldType">
      <xs:choice>
        <xs:element type="baseGenType" name="full-name" /><!-- full-name generator -->
      </xs:choice>
    </xs:extension>
  </xs:complexContent>
</xs:complexType>"""
    }

    def "test registerFields"() {
        given:
        def fs = [new Libs.FakerField(name: "built-in", gens: []), new Libs.FakerField(name: "name", gens: [])]

        when:
        def regsXsd = libs.registerFields(fs)

        then:
        regsXsd == """<xs:group name="anyOneGenerator"><!-- register all generators -->
  <xs:choice>
    <xs:element name="built-in" type="built-inType" />
    <xs:element name="name" type="nameType" />
  </xs:choice>
</xs:group>"""
    }

}
