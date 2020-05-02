package org.testd.fakerpp.core.engine.generator.builtin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.engine.generator.Generator;
import org.testd.fakerpp.core.engine.generator.builtin.base.DefaultNumber;
import org.testd.fakerpp.core.parser.ast.Table;
import org.testd.fakerpp.core.util.SeedableThreadLocalRandom;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class DoubleGen implements Generator {

    @DefaultNumber(0)
    public double min = 0;

    @DefaultNumber(0)
    public double max = 100;

    @DefaultNumber(3)
    public int maxNumberOfDigits = 3;


    @Override
    public void init(int colNum) throws ERMLException {
        if (max <= min) {
            throw new ERMLException("<int min='...' max='...'> max must be larger than min");
        }
    }

    @Override
    public List<String> nextData() throws ERMLException {
        final double range = max - min;

        final double chunkCount = Math.sqrt(Math.abs(range));
        final double chunkSize = chunkCount;
        final long randomChunk = SeedableThreadLocalRandom.nextLong((long) chunkCount);

        final double chunkStart = min + randomChunk * chunkSize;
        final double adj = chunkSize * SeedableThreadLocalRandom.nextDouble();

        return Collections.singletonList(new BigDecimal(chunkStart + adj)
                .setScale(maxNumberOfDigits, BigDecimal.ROUND_HALF_DOWN)
                .toPlainString()
        );
    }

    @Override
    public long dataNum() {
        return 0;
    }
}
