package org.testd.fakerpp.core.engine.generator.builtin;

import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.engine.generator.Generator;
import org.testd.fakerpp.core.engine.generator.builtin.base.DateBase;
import org.testd.fakerpp.core.engine.generator.builtin.base.DefaultString;
import org.testd.fakerpp.core.engine.generator.builtin.base.TimeBase;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class DateTimeGen implements Generator {

    @DefaultString("yyyy-MM-dd HH:mm:ss")
    public String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    @DefaultString("yyyy-MM-dd")
    public String dateFormat = "yyyy-MM-dd";
    @DefaultString("end-60")
    public String dateStart = "end-60";
    @DefaultString("now")
    public String dateEnd = "now";

    @DefaultString("HH:mm:ss")
    public String timeFormat = "HH:mm:ss";
    @DefaultString("00:00:00")
    public String timeStart = "00:00:00";
    @DefaultString("23:59:59")
    public String timeEnd = "23:59:59";

    private DateBase dateBase;
    private TimeBase timeBase;
    private DateTimeFormatter formatter;

    @Override
    public void init(int colNum) throws ERMLException {
        dateBase = new DateBase(dateFormat, dateStart, dateEnd);
        timeBase = new TimeBase(timeFormat, timeStart, timeEnd);
        formatter = DateTimeFormatter.ofPattern(dateTimeFormat);
    }

    @Override
    public List<String> nextData() throws ERMLException {
        return Collections.singletonList(
                dateBase.randomDate().atTime(timeBase.randomTime()).format(formatter)
        );
    }

    @Override
    public long dataNum() {
        return 0;
    }
}
