package org.testany.fakerpp.core.engine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.domain.ColExec;
import org.testany.fakerpp.core.engine.domain.ColFamilyExec;
import org.testany.fakerpp.core.engine.domain.TableExec;
import org.testany.fakerpp.core.engine.generator.ComposeGen;
import org.testany.fakerpp.core.engine.generator.faker.Fakers;
import org.testany.fakerpp.core.engine.generator.Generator;
import org.testany.fakerpp.core.engine.generator.Generators;
import org.testany.fakerpp.core.parser.ast.DataSourceInfo;
import org.testany.fakerpp.core.parser.ast.ERML;
import org.testany.fakerpp.core.parser.ast.Table;
import org.testany.fakerpp.core.store.ERMLStore;
import org.testany.fakerpp.core.util.WeightedRandom;

import java.util.*;
import java.util.stream.Collectors;

import static org.testany.fakerpp.core.util.ExceptionFunction.sneakyFunction;

@Component
@RequiredArgsConstructor
@Slf4j
public class ERMLEngine {

    private final ERMLStore ermlStore;

    private final Generators generators;

    private final Fakers fakers;

    public void exec(ERML erml) throws ERMLException {
        ermlStore.exec(getScheduler(erml));
    }

    public Scheduler getScheduler(ERML erml) throws ERMLException {
        // convert to table exec
        // 1. convert every single table to table exec
        // 2. connect them in graph
        // 3. construct graph

        return new TopologyScheduler(getTableExecMap(erml));
    }

    public Map<String, TableExec> getTableExecMap(ERML erml) throws ERMLException {
        Map<String, TableExec> tableExecMap = new HashMap<>();
        for (Map.Entry<String, Table> entry : erml.getTables().entrySet()) {
            tableExecMap.put(entry.getKey(),
                    getTableExec(entry.getValue(), erml.getMeta().getDataSourceInfos(),
                            erml.getMeta().getLang()));
        }

        // process joins
        for (Map.Entry<String, Table> entry : erml.getTables().entrySet()) {
            TableExec originTable = tableExecMap.get(entry.getKey());
            Table.Joins tableJoins = entry.getValue().getJoins();
            originTable.leftJoin(TableExec.JoinInfo.getJoinInfos(tableExecMap,
                    tableJoins.getLeftJoins()));
            originTable.rightJoin(TableExec.JoinInfo.getJoinInfos(tableExecMap,
                    tableJoins.getRightJoins()));
        }

        return tableExecMap;
    }

    private TableExec getTableExec(Table table,
                                   Map<String, DataSourceInfo> infoMap,
                                   String defaultLang) throws ERMLException {
        // dataSourceInfo
        DataSourceInfo dInfo;
        if (StringUtils.isEmpty(table.getDs())) {
            dInfo = null;
        } else if (!infoMap.containsKey(table.getDs())) {
            dInfo = null;
            log.warn("datasource of name {} can not be found in meta", table.getDs());
        } else {
            dInfo = infoMap.get(table.getDs());
        }

        // col families (get generator)
        List<ColFamilyExec> criticalCfExecs = new ArrayList<>();
        List<ColFamilyExec> normalCfExecs = new ArrayList<>();
        LinkedHashMap<String, ColExec> orderMap = new LinkedHashMap<>();
        for (Table.ColFamily cf : table.getColFamilies()) {
            List<ColExec> colExecs = new ArrayList<>();
            for (String colName : cf.getCols()) {
                if (orderMap.containsKey(colName)) {
                    throw new ERMLException(
                            String.format("duplicate col '%s' in table '%s'"
                                    , colName, table.getName()));
                }
                ColExec colExec = new ColExec(colName);
                orderMap.put(colName, colExec);
                colExecs.add(colExec);
            }

            if (cf.getGeneratorInfos().size() <= 0) {
                throw new ERMLException("generator can not be empty");
            }
            Generator generator = null;
            if (cf.getGeneratorInfos().size() == 1) {
                generator = getGeneratorByInfo(cf.getGeneratorInfos().get(0), defaultLang);
            } else {
                // compose generator
                ImmutableList.Builder<WeightedRandom.WeightedItem<Generator>>
                        weightedItemsBuilder = ImmutableList.builder();
                for (Table.GeneratorInfo info : cf.getGeneratorInfos()) {
                    weightedItemsBuilder.add(new WeightedRandom.WeightedItem(info.getWeight(),
                            getGeneratorByInfo(info, defaultLang)));
                }
                generator = new ComposeGen(weightedItemsBuilder.build());
            }


            ColFamilyExec colFamilyExec = new ColFamilyExec(colExecs, generator);
            if (generator.dataNum() > 0) {
                criticalCfExecs.add(colFamilyExec);
            } else {
                normalCfExecs.add(colFamilyExec);
            }
        }

        // exclude column
        ImmutableMap.Builder<String, ColExec> excludesBuilder = new ImmutableMap.Builder<>();
        for (String excludeColName : table.getExcludes()) {
            if (orderMap.containsKey(excludeColName)) {
                throw new ERMLException(
                        String.format("<exclude> '%s' column already define", excludeColName)
                );
            }
            excludesBuilder.put(excludeColName, new ColExec(excludeColName));
        }

        return new TableExec(table.getName(), table.getNum(),
                dInfo, criticalCfExecs, normalCfExecs, orderMap, excludesBuilder.build());
    }

    private Generator getGeneratorByInfo(Table.GeneratorInfo gi, String defaultLang) throws ERMLException {
        String field = gi.getField();
        Generator generator = null;
        if ("built-in".equals(field)) {
            return generators.builtInGenerator(gi.getGenerator(),
                    gi.getAttributes(),
                    gi.getOptions());
        } else {
            return fakers.fakerGenerator("default".equals(gi.getLang())
                            ? defaultLang : gi.getLang(),
                    field,
                    gi.getGenerator(),
                    gi.getAttributes());
        }
    }


}
