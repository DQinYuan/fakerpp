package org.testany.fakerpp.core.util;

import org.apache.commons.text.WordUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MyStringUtil {

    /**
     *
     * @param origin
     * @return
     */
    public static String delimit2Camel(String origin, boolean capitalUpper) {
        String upper = WordUtils.capitalizeFully(origin, '-')
                .replaceAll("-", "");
        if (!capitalUpper) {
            char[] chars = upper.toCharArray();
            chars[0] +=32;
            return new String(chars);
        }
        return upper;
    }


    private static final String insertTemplate
            = "INSERT INTO %s(%s) values %s";

    /**
     * generate sql to be prepared
     * for example:  prepareInsertSQL("test", ["a", "b", "c"], 3)
     * result: "INSERT INTO test(a,b,c) values (?,?,?),(?,?,?),(?,?,?)"
     * @param tableName
     * @param cols
     * @param recordNum
     * @return sql to be prepared
     */
    public static String prepareInsertSQL(String tableName, List<String> cols, int recordNum) {
        String colsStr = cols.stream().collect(Collectors.joining(","));
        String markValue = Collections.nCopies(cols.size(), "?")
                .stream().collect(Collectors.joining(",","(",")"));
        return String.format(insertTemplate, tableName, colsStr,
                Collections.nCopies(recordNum, markValue)
                        .stream().collect(Collectors.joining(","))
        );
    }

}
