package org.testd.ui.model

import org.testd.fakerpp.core.parser.ast.DataSourceInfo
import org.testd.fakerpp.core.parser.ast.Meta
import org.testd.ui.util.XmlUtil
import spock.lang.Specification

import javax.xml.transform.OutputKeys

class MetaPropertyTest extends Specification {

    def "test dataSources serialize"() {
        given:
        def mp = MetaProperty.map(
                new Meta("", [
                        "mysql": new DataSourceInfo(
                                "mysql",
                                "mysql",
                                "default",
                                100,
                                "u1",
                                "root",
                                "111"
                        ),
                        "postgre": new DataSourceInfo(
                                "postgre",
                                "postgre",
                                "default",
                                100,
                                "u1",
                                "ddd",
                                "1234"
                        ),
                ])
        )

        when:
        def document = XmlUtil.newDocument()
        def dataSourcesElement = mp.serialDataSources(document)
        document.appendChild(dataSourcesElement)
        def result = XmlUtil.serialize(document, {
            it.setOutputProperty(OutputKeys.INDENT, "yes")
            it.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            it.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        })

        then:
        result == """<datasources>
  <datasource batch-size="100" name="mysql" storer="default" type="mysql">
    <url>u1</url>
    <user>root</user>
    <passwd>111</passwd>
  </datasource>
  <datasource batch-size="100" name="postgre" storer="default" type="postgre">
    <url>u1</url>
    <user>ddd</user>
    <passwd>1234</passwd>
  </datasource>
</datasources>
"""
    }

}
