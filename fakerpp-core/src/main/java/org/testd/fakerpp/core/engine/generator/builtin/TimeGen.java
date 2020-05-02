package org.testd.fakerpp.core.engine.generator.builtin;

import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.engine.generator.Generator;
import org.testd.fakerpp.core.engine.generator.builtin.base.DefaultString;
import org.testd.fakerpp.core.engine.generator.builtin.base.TimeBase;

import java.util.Collections;
import java.util.List;

public class TimeGen implements Generator {

    @DefaultString("HH:mm:ss")
    public String format = "HH:mm:ss";
    @DefaultString("00:00:00")
    public String start = "00:00:00";
    @DefaultString("23:59:59")
    public String end = "23:59:59";

    private TimeBase timeBase;

    @Override
    public void init(int colNum) throws ERMLException {
        timeBase = new TimeBase(format, start, end);
    }

    @Override
    public List<String> nextData() throws ERMLException {
        return Collections.singletonList(timeBase.random());
    }

    @Override
    public long dataNum() {
        return 0;
    }
}
