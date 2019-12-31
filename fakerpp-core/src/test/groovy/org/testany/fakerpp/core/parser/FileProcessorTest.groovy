package org.testany.fakerpp.core.parser


import org.testany.fakerpp.core.parser.ast.DataSourceInfo
import org.testany.fakerpp.core.parser.ast.Meta
import org.testany.fakerpp.core.parser.ast.Table
import org.w3c.dom.Document
import spock.lang.Specification

import javax.xml.validation.Schema
import java.nio.file.Paths

class FileProcessorTest extends Specification {

    private Document getDocFromClassPath(String path, Schema schema) {
        return FileProcessor
                .getDocument(Paths.get(getClass().getResource(path).toURI()), schema)
    }

    def "parse meta.xml to Meta Object"() {
        expect:
        def meta = new Meta()
        meta.appendDataSourceInfo(new DataSourceInfo("mysql0", "mysql",
                "s", "mysql0Url",
                "mysql0User", "123456"))
        meta.appendDataSourceInfo(new DataSourceInfo(
                "redis", "redis",
                "default", "redisUrl",
                "redisUser", "redis123"))
        FileProcessor.parseMetaXml(getDocFromClassPath("meta.xml", MetaSchema.getInstance())) ==
                meta
    }

    def "parse table.xml to Table object"() {
        expect:
        FileProcessor.parseTableXml(getDocFromClassPath("table.xml",
                FakerppSchema.getInstance())) ==
                new Table(
                        "user_shop",
                        "mysql0",
                        0,
                        new Table.Joins(
                                [new Table.Join(["id":"user_id"], "user", false)],
                                [new Table.Join(["dt":"dt"], "dt", false),
                                 new Table.Join(["id":"shop_id"], "shop", true)]
                        ),

                        [new Table.ColFamily(
                                ["amount", "mmmm"],
                                "number",
                                "random-double",
                                ["max-number-of-decimals": "2", min: "90", max: "10000"],
                                [:]
                        ),
                         new Table.ColFamily(
                                 ["oppp"],
                                 "built-in",
                                 "enum",
                                 [:],
                                 [options: ["sdd", "dds"]]
                         )],
                        ["id"]
                )
    }
}