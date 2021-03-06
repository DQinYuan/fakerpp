package org.testd.fakerpp.core.store;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.engine.Scheduler;
import org.testd.fakerpp.core.engine.TableIter;
import org.testd.fakerpp.core.engine.TopologyScheduler;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;
import org.testd.fakerpp.core.store.storers.Storer;
import org.testd.fakerpp.core.store.storers.Storers;
import org.testd.fakerpp.core.store.storers.TableStorer;
import org.testd.fakerpp.core.parser.ast.DataSourceInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ERMLStore {

    private final Storers storerFactory;

    public void exec(Scheduler sched) throws ERMLException {
        sched.forEach(this::storeTable);
    }

    public void storeTable(TableIter tableIter) throws ERMLException {
        if (!tableIter.dataSourceInfo().isPresent()) {
            // virtual table
            tableIter.forEach(l -> {});
            return;
        }

        DataSourceInfo dataSourceInfo = tableIter.dataSourceInfo().get();
        TableStorer storer = storerFactory.getInitedStorer(dataSourceInfo)
                .getTableStorer(tableIter.name(), tableIter.columns());
        tableIter.forEach(storer::store);
        storer.flush();

        if (tableIter.exludes() == null || tableIter.exludes().size() == 0) {
            // needn't feedBack data
            return;
        }

        Map<String, List<String>> feadBackData = storer.feedBackData(tableIter.exludes());
        tableIter.feedEachExclude(
                dataFeeder ->
                        dataFeeder.addAll(
                                feadBackData.getOrDefault(dataFeeder.getName(), new ArrayList<>())
                        )
        );
    }

}
