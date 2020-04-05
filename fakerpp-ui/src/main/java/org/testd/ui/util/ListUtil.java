package org.testd.ui.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListUtil {

    public static <O, T> List<T> map(List<O> origins, Function<O, T> mapFunc) {
        return origins.stream()
                .map(mapFunc).collect(Collectors.toList());
    }

}
