package org.testany.fakerpp.core.parser

import org.testany.fakerpp.core.parser.ast.ERML
import spock.lang.Specification

import java.nio.file.Paths

class ERMLParserTest extends Specification {

    def "parse a directory to ERML object"() {
        given:
        def parser = new ERMLParser(null)

        when:
        ERML erml = parser.parseDir(Paths.get("examples", "user_shop"))

        then:
        with(erml) {
            meta.getDataSourceInfos()["mysql0"] != null
            ["dt", "shop", "user", "user_detail", "user_shop"].each {
                name -> assert tables[name] != null
            }
        }
    }

}
