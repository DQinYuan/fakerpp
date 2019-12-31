package org.testany.fakerpp.core.engine.generator.builtin.base;

import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.util.BiExpression;
import org.testany.fakerpp.core.util.BiExpressionParser;
import org.testany.fakerpp.core.util.SeedableThreadLocalRandom;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateBase {

    private final String format;
    private final String start;
    private final String end;

    private LocalDate startDate; // inclusive

    private LocalDate endDate;   // exclusive
    private long interval;
    private DateTimeFormatter dateFormatter;

    public DateBase(String format, String start, String end) throws ERMLException {
        this.format = format;
        this.start = start;
        this.end = end;

        // formatter
        dateFormatter = DateTimeFormatter.ofPattern(format);
        // end date
        if ("now".equals(end)) {
            endDate = LocalDate.now();
        } else {
            endDate = parse(end);
        }

        // start date
        if (start.contains("end")) { //expression
            BiExpression biExpr = BiExpressionParser.parse(start);
            if (biExpr == null) {
                throw new ERMLException(String.format(
                        "<date start='...'> expression '%s' is illegal", start));
            }
            if (!"end".equals(biExpr.getLeft())) {
                throw new ERMLException(String.format(
                        "<date start='...'> expression left expected to be 'end', not '%s'",
                        biExpr.getLeft()));
            }
            long rightNumber;
            try {
                rightNumber = Long.parseLong(biExpr.getRight());
            } catch (NumberFormatException e) {
                throw new ERMLException(String.format(
                        "<date start='...'> expression right expected to be a int number, not '%s'",
                        biExpr.getRight()));
            }

            switch (biExpr.getOp()) {
                case SUM:
                    throw new ERMLException(
                            "<date start='...'> expression op expected to be '-', not '+'");
                case MINUS:
                    startDate = LocalDate.ofEpochDay(endDate.toEpochDay() - rightNumber);
                    interval = rightNumber;
                    break;
            }
        } else {  // literal
            startDate = parse(start);
            interval = endDate.toEpochDay() - startDate.toEpochDay();
        }

        if (interval <= 0) {
            throw new ERMLException("<date> end date can not be earlier than or equal start date");
        }
    }

    private LocalDate parse(String dateStr) throws ERMLException {
        try {
            return LocalDate.parse(dateStr, dateFormatter);
        } catch (DateTimeParseException e) {
            throw new ERMLException(
                    String.format(
                            "date '%s' do not fit format '%s'", dateStr, format));
        }
    }

    public String random() {
        long randomInterval = SeedableThreadLocalRandom.nextLong(interval);
        return LocalDate.ofEpochDay(
                startDate.toEpochDay() + randomInterval).format(dateFormatter);
    }

    public long getInterval() {
        return interval;
    }

    private long scanIndex = 0;

    public String scan() {
        if (scanIndex >= interval) {
            return null;
        }

        return LocalDate.ofEpochDay(
                startDate.toEpochDay() + scanIndex++).format(dateFormatter);
    }

}
