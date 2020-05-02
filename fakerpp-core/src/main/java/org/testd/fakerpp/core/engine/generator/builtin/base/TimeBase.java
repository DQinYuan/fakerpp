package org.testd.fakerpp.core.engine.generator.builtin.base;

import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.util.SeedableThreadLocalRandom;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;

public class TimeBase {

    private final String format;
    private final String start;
    private final String end;

    private final DateTimeFormatter dateTimeFormatter;
    private final LocalTime startTime;
    private final int intervalSecond;

    public TimeBase(String format, String start, String end) throws ERMLException {
        this.format = format;
        this.start = start;
        this.end = end;

        dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        startTime = LocalTime.parse(start, dateTimeFormatter);
        intervalSecond = LocalTime.parse(end, dateTimeFormatter).toSecondOfDay() -
                startTime.toSecondOfDay();
        if (intervalSecond <= 0) {
            throw new ERMLException(String.format(
                    "start time '%s' can not later than end time '%s'", start, end
            ));
        }
    }

    public LocalTime randomTime() {
        int addSec = SeedableThreadLocalRandom.nextInt(intervalSecond);
        return startTime.plusSeconds(addSec);
    }

    public String random() {
        return randomTime().format(dateTimeFormatter);
    }
}
