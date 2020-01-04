package org.testany.fakerpp.core.engine.domain;

import com.google.common.collect.Streams;
import lombok.Getter;
import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.generator.Generator;
import org.testany.fakerpp.core.parser.ast.ERML;

import java.util.List;

@Getter
public class ColFamilyExec {

    private final List<ColExec> cols;

    // generator
    private final Generator generator;

    public ColFamilyExec(List<ColExec> cols, Generator generator) throws ERMLException {
        this.cols = cols;
        this.generator = generator;
        generator.init();
    }

    public List<String> nextData() throws ERMLException {
        List<String> data = generator.nextData();
        if (data == null) {
            throw new ERMLException(
                    String.format("col family %s exhaust", cols)
            );
        }
        if (data.size() != cols.size()) {
            throw new ERMLException(
                    String.format("col family %s illegal, generated cols num: %s, " +
                            "col family col num: %s", cols, data.size(), cols.size())
            );
        }

        // add data to colExec
        Streams.forEachPair(cols.stream(), data.stream(),
                (colExec, s) -> colExec.add(s));

        return data;
    }

    public long dataNum() {
        return generator.dataNum();
    }

}
