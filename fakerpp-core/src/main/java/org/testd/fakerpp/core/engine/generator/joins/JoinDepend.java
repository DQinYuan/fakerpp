package org.testd.fakerpp.core.engine.generator.joins;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.testd.fakerpp.core.engine.domain.ColExec;
import org.testd.fakerpp.core.engine.domain.ColExec;

import java.util.List;

/**
 * `joinColExecs` and `dependColExecs` map in corresponding index
 */
@RequiredArgsConstructor
@Getter
public class JoinDepend {
    private final List<ColExec> joinColExecs;
    private final List<ColExec> dependColExecs;
}
