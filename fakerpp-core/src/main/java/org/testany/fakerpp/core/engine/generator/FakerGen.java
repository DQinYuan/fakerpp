package org.testany.fakerpp.core.engine.generator;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FakerGen implements Generator {

    private final String field;
    private final String generator;

    @Override
    public void init() {

    }

    @Override
    public String nextData() {
        return null;
    }

    @Override
    public long dataNum() {
        return 0;
    }

}
