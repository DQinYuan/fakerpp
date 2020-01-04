package org.testany.fakerpp.core.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.domain.ColExec;
import org.testany.fakerpp.core.engine.domain.ColFamilyExec;
import org.testany.fakerpp.core.engine.domain.TableExec;
import org.testany.fakerpp.core.engine.generator.faker.Fakers;
import org.testany.fakerpp.core.engine.generator.Generator;
import org.testany.fakerpp.core.engine.generator.Generators;
import org.testany.fakerpp.core.parser.ast.DataSourceInfo;
import org.testany.fakerpp.core.parser.ast.ERML;
import org.testany.fakerpp.core.parser.ast.Table;
import org.testany.fakerpp.core.store.ERMLStore;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ERMLEngine {

    private final ERMLStore ermlStore;

    private final Generators generators;

    private final Fakers fakers;

    public void exec(ERML erml) throws ERMLException {
        Scheduler sched = getScheduler(erml);
        ermlStore.exec(sched);
    }

    public Scheduler getScheduler(ERML erml) throws ERMLException {
        // convert to table exec
        // 1. convert every single table to table exec
        // 2. connect them graph
        // 3. construct graph

        return new Scheduler(getTableExecMap(erml));
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

            String field = cf.getField();
            Generator generator = null;
            if ("built-in".equals(field)) {
                generator = generators.builtInGenerator(cf.getGenerator(),
                        cf.getAttributes(),
                        cf.getOtherLists());
            } else {
                generator = fakers.fakerGenerator("".equals(cf.getLang()) ? defaultLang: cf.getLang(),
                        field,
                        cf.getGenerator(),
                        cf.getAttributes(),
                        cf.getOtherLists());
            }

            ColFamilyExec colFamilyExec = new ColFamilyExec(colExecs, generator);
            if (generator.dataNum() > 0) {
                criticalCfExecs.add(colFamilyExec);
            } else {
                normalCfExecs.add(colFamilyExec);
            }
        }

        // exclude column
        Map<String, ColExec> excludes = new HashMap<>();
        for (String excludeColName : table.getExcludes()) {
            if (orderMap.containsKey(excludeColName)) {
                throw new ERMLException(
                        String.format("<exclude> '%s' column already define", excludeColName)
                );
            }
            excludes.put(excludeColName, new ColExec(excludeColName));
        }

        return new TableExec(table.getName(), table.getNum(),
                dInfo, criticalCfExecs, normalCfExecs, orderMap, excludes);
    }


}
