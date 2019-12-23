package org.testany.fakerpp.core.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class BiExpression {

    public static enum Operator {
        SUM, MINUS
    }

    private final String left;
    private final String right;
    private final Operator op;
}
