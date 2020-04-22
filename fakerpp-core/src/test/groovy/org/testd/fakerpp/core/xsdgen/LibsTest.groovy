package org.testd.fakerpp.core.xsdgen


import org.testd.fakerpp.core.engine.generator.LogicType
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

    def "test formatField"(Libs.FakerField ff, String expect) {
        expect:
        libs.formatField(ff) == expect

        where:
        ff                                                                                                  | expect
        new Libs.FakerField(name: "built-in",
                gens: [new Libs.Generator(name: "const",
                        paramInfos: [],
                        hasOption: true),
                       new Libs.Generator(name: "int",
                               paramInfos: [new Libs.ParamInfo(name: "min",
                                       type: LogicType.IntType.getInstance(), defaultValue: 0),
                                            new Libs.ParamInfo(name: "max",
                                                    type: LogicType.IntType.getInstance(), defaultValue: 100)])]) | """<xs:complexType name="built-inType"><!-- built-in generators -->
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
                        paramInfos: [])])                                                                   | """<xs:complexType name="nameType"><!-- name generators -->
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
