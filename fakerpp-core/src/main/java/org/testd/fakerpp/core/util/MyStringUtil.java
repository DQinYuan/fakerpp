package org.testd.fakerpp.core.util;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.WordUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MyStringUtil {


    private static boolean isUpCase(char c) {
        return c >= 'A' && c <= 'Z';
    }

    /**
     *
     * @param origin
     * @param capitalUpper is keep capital case up
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

    public static String camelToDelimit(String str) {
        if (isUpCase(str.charAt(0))) {
            char[] chars = str.toCharArray();
            chars[0] += 32;
            str = new String(chars);
        }

        return str.replaceAll("[A-Z]", "-$0").toLowerCase();
    }

    /**
     *
     * @param source string to be replaced with placeholder
     * @param param map placeholder to value
     * @return
     */
    public static String replace(String source, Map<String, String> param){
        StringSubstitutor strSubstitutor = new StringSubstitutor(param);
        return strSubstitutor.replace(source);
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
