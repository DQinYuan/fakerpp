package org.testany.fakerpp.core.engine


import org.testany.fakerpp.core.engine.generator.Generators
import org.testany.fakerpp.core.engine.generator.builtin.EnumGen
import org.testany.fakerpp.core.engine.generator.joins.LeftJoinGen
import org.testany.fakerpp.core.engine.generator.joins.RightJoinGen
import org.testany.fakerpp.core.parser.ast.DataSourceInfo
import org.testany.fakerpp.core.parser.ast.ERML
import org.testany.fakerpp.core.parser.ast.Meta
import org.testany.fakerpp.core.parser.ast.Table
import spock.lang.Shared
import spock.lang.Specification

class ERMLEngineTest extends Specification {

    @Shared
    ERMLEngine engine

    def setupSpec() {
        engine = new ERMLEngine(null, new Generators())
    }

    def "parse erml to tableExec"() {
        given:
        def m0Ds = new DataSourceInfo("mysql0", "mysql",
                "s", "mysql0Url",
                "mysql0User", "123456")
        def meta = new Meta(dataSourceInfos: [mysql0: m0Ds])
        def tables = [
                "user_shop": new Table(
                        "user_shop",
                        "mysql0",
                        0,
                        new Table.Joins(
                                [new Table.Join(["id": "user_id"], "user")],
                                [new Table.Join(["dt": "dt"], "dt"),
                                 new Table.Join(["id": "shop_id"], "shop")]
                        ),
                        [new Table.ColFamily(
                                ["oppp"],
                                "built-in",
                                "enum",
                                [:],
                                [options: ["sdd", "dds"]]
                        )],
                        ["id"]
                ),
                "user"     : new Table(
                        "user",
                        "mysql0",
                        15,
                        new Table.Joins([], []),
                        [],
                        ["id"]
                ),
                "dt"       : new Table(
                        "dt",
                        "",
                        0,
                        new Table.Joins([], []),
                        [new Table.ColFamily(
                                ["dt"],
                                "built-in",
                                "date-range",
                                [:],
                                [:]
                        )],
                        []
                ),
                "shop"     : new Table(
                        "shop",
                        "mysql0",
                        22,
                        new Table.Joins([], []),
                        [],
                        ["id"]
                )
        ]
        def erml = new ERML(
                meta: meta,
                tables: tables
        )

        expect:
        def execMap = engine.getTableExecMap(erml)
        with(execMap["user_shop"]) {
            name == "user_shop"
            num == 0
            dataSourceInfo == m0Ds
            criticalColFamilies.size() == 1
            criticalColFamilies[0].generator instanceof RightJoinGen
            criticalColFamilies[0].generator.dependColExecs*.name == ["dt", "id"]
            colFamilies.size() == 2
            colFamilies[0].generator instanceof EnumGen
            colFamilies[1].generator instanceof LeftJoinGen
            colFamilies[1].generator.dependColExecs*.name == ["id"]
            ["user_id", "dt", "shop_id", "oppp"].each {
                col -> assert columns.containsKey(col)
            }
            excludes.size() == 1
            excludes.containsKey("id")
        }

        with(execMap["dt"]) {
            dataSourceInfo == null
        }
    }

}
