package org.testany.fakerpp.core.engine;

import lombok.extern.slf4j.Slf4j;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.domain.TableExec;
import org.testany.fakerpp.core.util.ExceptionConsumer;

import java.util.Map;

@Slf4j
public class TopologyScheduler implements Scheduler {

    private Graph<TableExec, DefaultEdge> dag =
            new DefaultDirectedGraph<>(DefaultEdge.class);
    private CycleDetector<TableExec, DefaultEdge> detector
             = new CycleDetector<>(dag);

    public TopologyScheduler(Map<String, TableExec> tableExecMap) {
        tableExecMap.forEach(
                (name, exec) -> dag.addVertex(exec)
        );

        tableExecMap.forEach(
                (name, curExec) ->
                    curExec.getDepends().forEach(
                            dependExec -> dag.addEdge(dependExec, curExec)
                    )
        );
    }


    public void forEach(ExceptionConsumer<TableIter, ERMLException> consumer) throws ERMLException {
        if (detector.detectCycles()) {
            throw new ERMLException("tables has cycle dependency");
        }

        TopologicalOrderIterator<TableExec, DefaultEdge> iterator = new TopologicalOrderIterator<>(dag);
        while (iterator.hasNext()) {
            TableExec next = iterator.next();
            log.info("start generate table {}", next.getName());
            consumer.accept(next);
        }
    }

}
