package org.testany.fakerpp.core.parser


import org.testany.fakerpp.core.parser.ast.DataSourceInfo
import org.testany.fakerpp.core.parser.ast.Meta
import org.testany.fakerpp.core.parser.ast.Table
import org.w3c.dom.Document
import spock.lang.Specification

import javax.xml.validation.Schema
import java.nio.file.Paths

class FileParserTest extends Specification {

    private Document getDocFromClassPath(String path, Schema schema) {
        return FileParser
                .getDocument(Paths.get(getClass().getResource(path).toURI()), schema)
    }

    def "parse meta.xml to Meta Object"() {
        expect:
        def metaBuilder = Meta.builder()
        metaBuilder.lang("en")
        metaBuilder.appendDataSourceInfo(new DataSourceInfo("mysql0", "mysql",
                "s", "mysql0Url",
                "mysql0User", "123456"))
        metaBuilder.appendDataSourceInfo(new DataSourceInfo(
                "redis", "redis",
                "default", "redisUrl",
                "redisUser", "redis123"))
        FileParser.parseMetaXml(getDocFromClassPath("meta.xml", MetaSchema.getInstance())) ==
                metaBuilder.build()
    }

    def "parse table.xml to Table object"() {
        expect:
        def real = FileParser.parseTableXml(getDocFromClassPath("table.xml",
                FakerppSchema.getInstance()))
        real == new Table(
                        "user_shop",
                        "mysql0",
                        0,
                        new Table.Joins(
                                [new Table.Join(["id": "user_id"], "user", false)],
                                [new Table.Join(["dt": "dt"], "dt", false),
                                 new Table.Join(["id": "shop_id"], "shop", true)]
                        ),

                        [new Table.ColFamily(
                                ["amount", "mmmm"],
                                [new Table.GeneratorInfo(
                                        "number",
                                        "zh-CN",
                                        "random-double",
                                        ["max-number-of-decimals": "2", min: "90", max: "10000"],
                                        [],
                                        1
                                )]
                        ),
                         new Table.ColFamily(
                                 ["name"],
                                 [new Table.GeneratorInfo(
                                         "name",
                                         "default",
                                         "full-name",
                                         [:],
                                         [],
                                         1
                                 )]
                         ),
                         new Table.ColFamily(
                                 ["oppp"],
                                 [new Table.GeneratorInfo(
                                         "built-in",
                                         "",
                                         "enum",
                                         [:],
                                         [["sdd", "opo"], ["dds"]],
                                         1
                                 )]
                         )],
                        ["id"]
                )
    }
}
