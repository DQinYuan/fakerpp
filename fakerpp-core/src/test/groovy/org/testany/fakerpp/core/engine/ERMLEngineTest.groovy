package org.testany.fakerpp.core.engine


import org.testany.fakerpp.core.engine.generator.Generators
import org.testany.fakerpp.core.engine.generator.builtin.EnumGen
import org.testany.fakerpp.core.engine.generator.faker.FakerFactory
import org.testany.fakerpp.core.engine.generator.faker.FakerInvoker
import org.testany.fakerpp.core.engine.generator.faker.Fakers
import org.testany.fakerpp.core.engine.generator.joins.LeftJoinGen
import org.testany.fakerpp.core.engine.generator.joins.RightJoinGen
import org.testany.fakerpp.core.parser.ast.DataSourceInfo
import org.testany.fakerpp.core.parser.ast.ERML
import org.testany.fakerpp.core.parser.ast.Meta
import org.testany.fakerpp.core.parser.ast.Table
import org.testany.fakerpp.core.util.SeedableThreadLocalRandom
import spock.lang.Shared
import spock.lang.Specification

class ERMLEngineTest extends Specification {

    @Shared
    ERMLEngine engine

    def setupSpec() {
        engine = new ERMLEngine(null, new Generators(), new Fakers(new FakerFactory(),
                new FakerInvoker()))
    }

    def "parse erml to tableExec"() {
        given:
        def m0Ds = new DataSourceInfo("mysql0", "mysql",
                "s", "mysql0Url",
                "mysql0User", "123456")
        def metaBuilder = Meta.builder()
        metaBuilder.appendDataSourceInfo(m0Ds)
        metaBuilder.lang("en")
        def tables = [
                "user_shop": new Table(
                        "user_shop",
                        "mysql0",
                        0,
                        new Table.Joins(
                                [new Table.Join(["id": "user_id"], "user", false)],
                                [new Table.Join(["dt": "dt"], "dt", false),
                                 new Table.Join(["id": "shop_id"], "shop", true)]
                        ),
                        [new Table.ColFamily(
                                ["oppp"],
                                "built-in",
                                "default",
                                "enum",
                                [:],
                                [["sdd"], ["dds"]]
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
                                "",
                                "date-range",
                                [:],
                                []
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
        def ermlBuilder = ERML.builder()
        ermlBuilder.meta(metaBuilder.build())
        tables.each { k, v -> ermlBuilder.appendTable(v) }
        def erml = ermlBuilder.build()

        expect:
        def execMap = engine.getTableExecMap(erml)
        with(execMap["user_shop"]) {
            name == "user_shop"
            num == 0
            dataSourceInfo == m0Ds
            criticalColFamilies.size() == 1
            criticalColFamilies[0].generator instanceof RightJoinGen
            criticalColFamilies[0].cols*.name == ["shop_id", "dt"]
            colFamilies.size() == 2
            colFamilies[0].generator instanceof EnumGen
            colFamilies[1].generator instanceof LeftJoinGen
            ["user_id", "dt", "shop_id", "oppp"].each {
                col -> assert columns.containsKey(col)
            }
            excludes.size() == 1
            excludes.containsKey("id")

        }

        with(execMap["dt"]) {
            dataSourceInfo == null
            criticalColFamilies.size() == 1
        }
    }

    def assertTi(TableIter ti, List<String> expColumns,
                 DataSourceInfo expDi, List<List<String>> expRecords) {
        assert ti.columns() == expColumns
        assert ti.dataSourceInfo().orElse(null) == expDi
        assertRecord(ti, expRecords)

        ti.feedEachExclude(
                {
                    feeder ->
                        feeder.addAll((1..ti.recordNum())*.toString())
                })
    }

    def assertRecord(TableIter ti, List<List<String>> exps) {
        assert ti.recordNum() == exps.size()
        int count = 0
        ti.forEach({
            record ->
                assert record == exps[count++]
        })
        assert count == exps.size()
    }

    def "test engine layer"() {
        given:
        def m0Ds = new DataSourceInfo("mysql0", "mysql",
                "s", "mysql0Url",
                "mysql0User", "123456")
        def metaBuilder = Meta.builder()
        metaBuilder.appendDataSourceInfo(m0Ds)
        metaBuilder.lang("en")
        def tables = [
                "user_shop": new Table(
                        "user_shop",
                        "mysql0",
                        0,
                        new Table.Joins(
                                [new Table.Join(["a": "a", "b": "b"], "r", false)],
                                [new Table.Join(["dt": "dt"], "dt", false),
                                 new Table.Join(["id"  : "shop_id",
                                                 "name": "shop_name"], "shop", false),
                                 new Table.Join(["id": "user_id"],
                                         "user", true)]
                        ),
                        [new Table.ColFamily(
                                ["amount"],
                                "number",
                                "default",
                                "random-double",
                                ["max-number-of-decimals": "2",
                                 "min"                   : "90",
                                 "max"                   : "10000"],
                                []
                        )],
                        ["id"]
                ),
                "user"     : new Table(
                        "user",
                        "mysql0",
                        2,
                        new Table.Joins([new Table.Join(["a": "a", "b": "b"], "r", false)],
                                []),
                        [new Table.ColFamily(
                                ["sex"],
                                "built-in",
                                "",
                                "enum",
                                [:],
                                [["male"], ["female"]]
                        )],
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
                                "",
                                "date-range",
                                [start: "end-3", end: "2020-01-09"],
                                []
                        )],
                        []
                ),
                "shop"     : new Table(
                        "shop",
                        "mysql0",
                        3,
                        new Table.Joins([], []),
                        [new Table.ColFamily(
                                ["name"],
                                "built-in",
                                "",
                                "str",
                                [len: "3", prefix: "Shop_"],
                                []
                        )],
                        ["id"]
                ),
                "r"        : new Table(
                        "r",
                        "",
                        0,
                        new Table.Joins([], []),
                        [new Table.ColFamily(
                                ["a", "b"],
                                "built-in",
                                "",
                                "const",
                                [:],
                                [["a1", "b1"], ["a2", "b2"], ["a3", "b3"]]
                        )],
                        []
                )
        ]
        def ermlBuilder = ERML.builder()
        ermlBuilder.meta(metaBuilder.build())
        tables.each { k, v -> ermlBuilder.appendTable(v) }
        def erml = ermlBuilder.build()

        when:
        SeedableThreadLocalRandom.setSeed(0)
        def scheduler = engine.getScheduler(erml)

        then:
        scheduler.forEach(
                { ti ->
                    switch (ti.name()) {
                        case "dt":
                            assertTi(ti, ["dt"], null, [["2020-01-06"],
                                                        ["2020-01-07"],
                                                        ["2020-01-08"]])
                            break
                        case "r":
                            assertTi(ti, ["a", "b"], null,
                                    [["a1", "b1"], ["a2", "b2"], ["a3", "b3"]])
                            break
                        case "shop":
                            assertTi(ti, ["name"], m0Ds, [["Shop_Lxv"],
                                                          ["Shop_GCh"],
                                                          ["Shop_W07"]])
                            break
                        case "user":
                            assertTi(ti, ["sex", "a", "b"], m0Ds,
                                    [["male", "a2", "b2"], ["female", "a3", "b3"]])
                            break
                        case "user_shop":
                            assertTi(
                                    ti, ["user_id", "dt", "shop_id", "shop_name", "amount", "a", "b"],
                                    m0Ds,
                                    [["2", "2020-01-06", "1", "Shop_Lxv", "2902.55", "a3", "b3"],
                                     ["1", "2020-01-06", "2", "Shop_GCh", "2158.26", "a3", "b3"],
                                     ["2", "2020-01-06", "2", "Shop_GCh", "8339.39", "a1", "b1"],
                                     ["1", "2020-01-06", "3", "Shop_W07", "7438.44", "a2", "b2"],
                                     ["1", "2020-01-07", "1", "Shop_Lxv", "2750.52", "a2", "b2"],
                                     ["1", "2020-01-07", "2", "Shop_GCh", "8261.29", "a3", "b3"],
                                     ["2", "2020-01-07", "3", "Shop_W07", "8917.12", "a1", "b1"],
                                     ["1", "2020-01-08", "1", "Shop_Lxv", "1430.22", "a3", "b3"],
                                     ["2", "2020-01-08", "2", "Shop_GCh", "9018.5", "a1", "b1"],
                                     ["1", "2020-01-08", "3", "Shop_W07", "4157.51", "a2", "b2"],
                                     ["2", "2020-01-08", "3", "Shop_W07", "9102.55", "a1", "b1"],]
                            )
                            break
                    }

                    // to print result
/*                        println("==============")
                        println("table name:${ti.name()}")
                        println(ti.columns())
                        println(ti.dataSourceInfo().orElse(null))
                        def dataNum = ti.recordNum()
                        println("record number: ${dataNum}")
                        println("records:")
                        ti.forEach(
                                { record ->
                                    println(record)
                                }
                        )
                    ti.feedEachExclude(
                            {
                                feeder ->
                                    feeder.addAll((1..dataNum)*.toString())
                            })*/


                })
    }

    def "user defined more data than could generate"(int userDefinedNum, int genNum, int expNum) {
        given:
        def meta = new Meta("en", [:])
        def tables = [
                "dt": new Table(
                        "dt",
                        "",
                        userDefinedNum,
                        new Table.Joins([], []),
                        [new Table.ColFamily(
                                ["dt"],
                                "built-in",
                                "",
                                "date-range",
                                [start: "end-${genNum}".toString(), end: "2020-01-09"],
                                []
                        )],
                        []
                ),
        ]

        when:
        def scheduler = engine.getScheduler(new ERML(meta, tables))

        then:
        scheduler.forEach({
            ti ->
                assert ti.name() == "dt"
                assert ti.recordNum() == expNum
        })

        where:
        userDefinedNum | genNum | expNum
        10             | 3      | 3
        2              | 5      | 2
    }

}
