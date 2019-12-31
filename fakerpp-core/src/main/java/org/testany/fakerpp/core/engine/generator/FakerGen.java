package org.testany.fakerpp.core.engine.generator;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class FakerGen implements Generator {

    private final String field;
    private final String generator;

    @Override
    public void init() {

    }

    @Override
    public List<String> nextData() {
        return null;
    }

    @Override
    public long dataNum() {
        return 0;
    }

}
