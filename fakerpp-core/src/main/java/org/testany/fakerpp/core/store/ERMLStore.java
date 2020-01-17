package org.testany.fakerpp.core.store;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.Scheduler;
import org.testany.fakerpp.core.engine.TableIter;
import org.testany.fakerpp.core.engine.TopologyScheduler;
import org.testany.fakerpp.core.parser.ast.DataSourceInfo;
import org.testany.fakerpp.core.store.storers.Storer;
import org.testany.fakerpp.core.store.storers.Storers;
import org.testany.fakerpp.core.store.storers.TableStorer;

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

        Map<String, List<String>> feadBackData = storer.feedBackData(tableIter.exludes());
        tableIter.feedEachExclude(
                dataFeeder ->
                        dataFeeder.addAll(
                                feadBackData.get(dataFeeder.getName())
                        )
        );
    }

}
