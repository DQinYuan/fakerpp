package org.testd.fakerpp.core.engine.generator.builtin;

import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.engine.generator.Generator;
import org.testd.fakerpp.core.engine.generator.builtin.base.DateBase;
import org.testd.fakerpp.core.engine.generator.builtin.base.DefaultString;
import org.testd.fakerpp.core.engine.generator.Generator;
import org.testd.fakerpp.core.engine.generator.builtin.base.DefaultString;

import java.util.Arrays;
import java.util.List;

public class DateGen implements Generator {

    @DefaultString("yyyy-MM-dd")
    public String format = "yyyy-MM-dd";
    @DefaultString("end-60")
    public String start = "end-60";
    @DefaultString("now")
    public String end = "now";

    private DateBase dateBase;

    @Override
    public void init(int colNum) throws ERMLException {
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
