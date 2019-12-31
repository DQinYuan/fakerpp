package org.testany.fakerpp.core.engine.generator.builtin;

import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.generator.Generator;
import org.testany.fakerpp.core.engine.generator.builtin.base.DateBase;

import java.util.Arrays;
import java.util.List;

public class DateGen implements Generator {

    public String format = "yyyy-MM-dd";
    public String start = "end-60";
    public String end = "now";

    private DateBase dateBase;

    @Override
    public void init() throws ERMLException {
        dateBase = new DateBase(format, start, end);
    }

    @Override
    public List<String> nextData() {
        return Arrays.asList(dateBase.random());
    }

    @Override
    public long dataNum() {
        return 0;
    }


}
