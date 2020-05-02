package org.testd.ui.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ListUtil {

    public static <O, T> List<T> map(List<O> origins, Function<O, T> mapFunc) {
        return origins.stream()
                .map(mapFunc).collect(Collectors.toList());
    }

    public static <T> List<T> repeat(Supplier<T> itemSupplier, int num) {
        List<T> list = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            list.add(itemSupplier.get());
        }
        return list;
    }

}
