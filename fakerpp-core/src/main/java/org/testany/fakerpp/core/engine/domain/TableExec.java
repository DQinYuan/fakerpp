package org.testany.fakerpp.core.engine.domain;

import com.google.common.collect.Streams;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.generator.joins.JoinDepend;
import org.testany.fakerpp.core.engine.generator.joins.LeftJoinGen;
import org.testany.fakerpp.core.engine.generator.joins.RightJoinGen;
import org.testany.fakerpp.core.parser.ast.DataSourceInfo;
import org.testany.fakerpp.core.parser.ast.Table;
import org.testany.fakerpp.core.util.ExceptionConsumer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.testany.fakerpp.core.util.ExceptionConsumer.sneaky;

@Slf4j
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public class TableExec {

    private final String name;
    private final int num;

    // null in virtual table
    private final DataSourceInfo dataSourceInfo;
    // column families can define record number
    private final List<ColFamilyExec> criticalColFamilies;
    // normal column failies
    private final List<ColFamilyExec> colFamilies;
    // columns without excludes
    private final LinkedHashMap<String, ColExec> columns;
    // exclude columns
    private final Map<String, ColExec> excludes;


    public List<String> columns() {
        return null;
    }

    public void forEach(ExceptionConsumer<List<String>, ERMLException> consumer) throws ERMLException {
    }

    public boolean containsCol(String colName) {
        return columns.containsKey(colName) || excludes.containsKey(colName);
    }

    public ColExec column(String colName) {
        return columns.getOrDefault(colName,
                excludes.get(colName));
    }

    @RequiredArgsConstructor
    @Getter
    public static class JoinInfo {
        private final Table.Join join;
        private final TableExec dependTable;

        public static List<JoinInfo> getJoinInfos(Map<String, TableExec> tableExecMap,
                                                  List<Table.Join> joins) {
            Stream<TableExec> tableExecStream = joins.stream()
                    .map(j -> tableExecMap.get(j.getDepend()));
            return Streams.zip(joins.stream(), tableExecStream,
                    JoinInfo::new).collect(Collectors.toList());
        }

    }

    public void leftJoin(List<JoinInfo> joinInfos) throws ERMLException {
        joinInfos.stream().forEach(sneaky(this::leftJoin));
    }

    public void leftJoin(JoinInfo joinInfo) throws ERMLException {
        JoinDepend joinDependExecs = getJoinDependExecs(joinInfo.getDependTable(),
                joinInfo.getJoin().getMap());
        colFamilies.add(new ColFamilyExec(
                joinDependExecs.getJoinColExecs(),
                new LeftJoinGen(joinDependExecs.getDependColExecs())
        ));
    }

    public void rightJoin(List<JoinInfo> joinInfos) throws ERMLException {
        RightJoinGen.Builder builder = RightJoinGen.builder();

        for (JoinInfo joinInfo : joinInfos) {
            JoinDepend joinDepend = getJoinDependExecs(
                    joinInfo.getDependTable(), joinInfo.getJoin().getMap());
            builder.appendDimension(joinDepend, joinInfo.getJoin().isRandom());
        }

        criticalColFamilies.add(new ColFamilyExec(
                builder.colOrder(),
                builder.build()
        ));
    }


    /**
     * @param dependTable
     * @param depend2table
     * @return depend cols
     * @throws ERMLException
     */
    private JoinDepend getJoinDependExecs(TableExec dependTable, Map<String, String> depend2table)
            throws ERMLException {
        if (depend2table == null || depend2table.size() < 1) {
            throw new ERMLException("join field can not be empty");
        }

        List<ColExec> joinColExecs = new ArrayList<>();
        List<ColExec> dependColExecs = new ArrayList<>();
        for (Map.Entry<String, String> entry : depend2table.entrySet()) {
            String dependColName = entry.getKey();
            String originColName = entry.getValue();
            if (!dependTable.containsCol(dependColName)) {
                throw new ERMLException(
                        String.format("error in join, field '%s' not in table '%s'",
                                dependColName, dependTable.getName())
                );
            }
            if (containsCol(originColName)) {
                throw new ERMLException(
                        String.format("duplicate col '%s' in table '%s'",
                                originColName, name)
                );
            }
            dependColExecs.add(dependTable.column(dependColName));
            ColExec joinColExec = new ColExec(originColName);
            columns.put(joinColExec.getName(), joinColExec);
            joinColExecs.add(joinColExec);
        }

        return new JoinDepend(joinColExecs, dependColExecs);
    }

}
