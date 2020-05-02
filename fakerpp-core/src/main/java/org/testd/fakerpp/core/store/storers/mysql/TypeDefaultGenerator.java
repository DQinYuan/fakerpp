package org.testd.fakerpp.core.store.storers.mysql;

import com.google.common.collect.ImmutableMap;
import org.testd.fakerpp.core.parser.ast.Table;

import java.time.Year;
import java.util.Map;

public class TypeDefaultGenerator {

    private static final Map<String, Table.GeneratorInfo> type2Generator =
            ImmutableMap.<String, Table.GeneratorInfo>builder()
                    .put("TINYINT", Table.GeneratorInfo.emptyBuiltInInfo("int"))
                    .put("SMALLINT", Table.GeneratorInfo.emptyBuiltInInfo("int"))
                    .put("MEDIUMINT", Table.GeneratorInfo.emptyBuiltInInfo("int"))
                    .put("INT", Table.GeneratorInfo.emptyBuiltInInfo("int"))
                    .put("INTEGER", Table.GeneratorInfo.emptyBuiltInInfo("int"))
                    .put("BIGINT", Table.GeneratorInfo.emptyBuiltInInfo("int"))
                    .put("DECIMAL", Table.GeneratorInfo.emptyBuiltInInfo("double"))
                    .put("FLOAT", Table.GeneratorInfo.emptyBuiltInInfo("double"))
                    .put("DOUBLE", Table.GeneratorInfo.emptyBuiltInInfo("double"))
                    .put("BIT", Table.GeneratorInfo.emptyBuiltInInfo("boolean"))
                    .put("CHAR", Table.GeneratorInfo.emptyBuiltInInfo("str"))
                    .put("VARCHAR", Table.GeneratorInfo.emptyBuiltInInfo("str"))
                    .put("TINYTEXT", Table.GeneratorInfo.emptyBuiltInInfo("str"))
                    .put("TEXT", Table.GeneratorInfo.emptyBuiltInInfo("str"))
                    .put("MEDIUMTEXT", Table.GeneratorInfo.emptyBuiltInInfo("str"))
                    .put("LONGTEXT", Table.GeneratorInfo.emptyBuiltInInfo("str"))
                    .put("DATE", Table.GeneratorInfo.emptyBuiltInInfo("date"))
                    .put("TIME", Table.GeneratorInfo.emptyBuiltInInfo("time"))
                    .put("DATETIME", Table.GeneratorInfo.emptyBuiltInInfo("date-time"))
                    .put("TIMESTAMP", Table.GeneratorInfo.emptyBuiltInInfo("date-time"))
                    .put("YEAR", Table.GeneratorInfo.builtInInfo("int",
                            ImmutableMap.of("min", Year.now().minusYears(10).toString(),
                                    "max", Year.now().toString())
                    ))
                    .build();

    private static final Table.GeneratorInfo defaultInfo4Unknown =
            Table.GeneratorInfo.emptyBuiltInInfo("str");

    public static Table.GeneratorInfo defaultGenByType(String type) {
        return type2Generator.getOrDefault(type, defaultInfo4Unknown);
    }


}
