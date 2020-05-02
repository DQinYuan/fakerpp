package org.testd.fakerpp.core

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testd.fakerpp.core.util.SeedableThreadLocalRandom
import org.testd.fakerpp.core.util.SeedableThreadLocalRandom
import spock.lang.Specification

import java.nio.file.Files

import static ClassPathUtil.relative
import static org.testd.fakerpp.core.store.H2DbTool.execSqlInH2
import static org.testd.fakerpp.core.store.H2DbTool.rowSetToString

@SpringBootTest
class ERMLExecutorTest extends Specification {

    @Autowired
    ERMLExecutor executor

    def "test diskExec"(String modelName, String dbName, Map<String, String> expectSqls) {
        given:
        def modelDirPath = relative(getClass(), "models", modelName)
        def prepareSqlPath = modelDirPath.resolve("schema.sql")
        execSqlInH2(dbName,
                new String(Files.readAllBytes(prepareSqlPath)))

        when:
        SeedableThreadLocalRandom.setSeed(1)
        executor.diskExec(modelDirPath)

        then:
        expectSqls.each { sql, expect ->
            assert rowSetToString(execSqlInH2(dbName, sql)) == expect
        }

        where:
        modelName     | dbName                         | expectSqls
        "user_shop"   | "ERMLExecutorTest.diskExec"    | ["select * from shop"       : """
           id         name        owner
            1     Shop_GuC    Owner_zl6
            2     Shop_wrk    Owner_S0t
            3     Shop_XXH    Owner_GpS
""",
                                                          "select * from user"       : """
           id         name          sex          age
            1          钟泽洋       female           25
            2          王旭尧         male           23
            3          郑君浩       female           45
""",
                                                          "select * from user_detail": """
           id         name          sex          age      address  description
            1          钟泽洋       female           25   street_rVF         FVF8
            2          王旭尧         male           23   street_RXB         mMoH
            3          郑君浩       female           45   street_WTR         foPp
""",
                                                          "select * from user_shop"  : """
           id           dt      shop_id      user_id       amount
            1   2020-01-02            1            1      8590.94
            2   2020-01-02            1            2      4811.81
            3   2020-01-02            1            3       3662.8
            4   2020-01-02            2            1      4031.38
            5   2020-01-02            2            2      3685.69
            6   2020-01-02            2            3       8749.2
            7   2020-01-02            3            1       5005.2
            8   2020-01-02            3            2      8087.13
            9   2020-01-02            3            3      1135.68
           10   2020-01-03            1            1      1446.89
           11   2020-01-03            1            2      6013.93
           12   2020-01-03            1            3      2123.66
           13   2020-01-03            2            1      4939.76
           14   2020-01-03            2            2      7676.58
           15   2020-01-03            2            3      7809.92
           16   2020-01-03            3            1      2637.24
           17   2020-01-03            3            2      5037.49
           18   2020-01-03            3            3      2504.55
           19   2020-01-04            1            1      1406.54
           20   2020-01-04            1            2      4450.61
           21   2020-01-04            1            3      3590.45
           22   2020-01-04            2            1       2181.4
           23   2020-01-04            2            2      2499.88
           24   2020-01-04            2            3       8721.6
           25   2020-01-04            3            1       7009.6
           26   2020-01-04            3            2       5082.7
           27   2020-01-04            3            3      4253.53
""",
                                                          "show tables"              : """
   TABLE_NAME TABLE_SCHEMA
         shop       PUBLIC
         user       PUBLIC
  user_detail       PUBLIC
    user_shop       PUBLIC
"""]
        "composetest" | "ERMLExecutorTest.composetest" | ["select * from user": """
         name
Romaine Lindgren V
Mellissa Lang
          钱梓晨
"""]
    }


    def "test memoryExec"() {
        given:
        def dbName = "ERMLExecutorTest.memoryExec"
        def metaStr = """<?xml version="1.0" encoding="UTF-8" ?>
<meta xmlns='https://github.com/dqinyuan/fakerpp/meta'>
    <datasources>
        <datasource name="mysql0" type="mysql" storer="default">
            <url>jdbc:h2:mem:${dbName}</url>
            <user></user>
            <passwd></passwd>
        </datasource>
    </datasources>
</meta>
""".toString()
        def tableStr = """<?xml version="1.0" encoding="UTF-8" ?>
<table xmlns="https://github.com/dqinyuan/fakerpp"
       name="user" num="3" ds="mysql0">
    <col-families>
        <name lang="zh-CN">
            <full-name>
                <cols>
                    <col>name</col>
                </cols>
            </full-name>
        </name>
    </col-families>
</table>
"""
        execSqlInH2(dbName, "create table user (name varchar(255))".toString())
        SeedableThreadLocalRandom.setSeed(0)

        when:
        executor.memoryExec(new ByteArrayInputStream(metaStr.getBytes()),
                [new ByteArrayInputStream(tableStr.getBytes())])

        then:
        rowSetToString(execSqlInH2(dbName, "select * from user")) == """
         name
          沈明哲
          林哲瀚
          金钰轩
"""
    }

    def "test cache"() {
        expect:
        int cacheValue =  executor.testCache(1, "s", int.class)
        5.times {
             assert cacheValue == executor.testCache(1, "s", int.class)
        }
    }
}
