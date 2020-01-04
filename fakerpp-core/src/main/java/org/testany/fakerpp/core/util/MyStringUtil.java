package org.testany.fakerpp.core.util;

import org.apache.commons.text.WordUtils;

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

}
