package org.testany.fakerpp.core.util;

import org.apache.commons.lang3.StringUtils;

public class BiExpressionParser {

    public static BiExpression parse(String biexpr) {

        BiExpression.Operator op = null;

        int i;
        for (i = 0; i < biexpr.length(); i++) {
            if (biexpr.charAt(i) == '+') {
                op = BiExpression.Operator.SUM;
                break;
            }
            if (biexpr.charAt(i) == '-') {
                op = BiExpression.Operator.MINUS;
                break;
            }
        }

        if (i == biexpr.length()) {
            return null;
        }

        String left = biexpr.substring(0, i).trim();
        String right = biexpr.substring(i + 1);
        if (StringUtils.isEmpty(left) || StringUtils.isEmpty(right)) {
            return null;
        }

        return new BiExpression(left, right, op);
    }

}
