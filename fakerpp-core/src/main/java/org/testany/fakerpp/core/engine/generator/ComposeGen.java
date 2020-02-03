package org.testany.fakerpp.core.engine.generator;

import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.util.WeightedRandom;

import java.util.List;

public class ComposeGen implements Generator {

    private final WeightedRandom<Generator> composedGens;

    public ComposeGen(List<WeightedRandom.WeightedItem<Generator>> weithedGenerators) {
        composedGens = new WeightedRandom<>(weithedGenerators);
    }

    @Override
    public void init(int colNum) throws ERMLException {
    }

    @Override
    public List<String> nextData() throws ERMLException {
        return composedGens.random().nextData();
    }

    @Override
    public long dataNum() {
        return 0;
    }
}
