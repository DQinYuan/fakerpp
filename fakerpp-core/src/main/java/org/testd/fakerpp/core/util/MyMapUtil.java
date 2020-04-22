package org.testd.fakerpp.core.util;

import com.google.common.collect.ImmutableMap;
import org.checkerframework.checker.units.qual.K;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class MyMapUtil {

    public static <K1, K2, V1, V2> Map<K2, V2> map(Map<K1, V1> originMap, Function<K1, K2> keyMap,
                                                           Function<V1, V2> valueMap) {
        return originMap.entrySet().stream()
                .collect(ImmutableMap.toImmutableMap(
                   en -> keyMap.apply(en.getKey()),
                   en -> valueMap.apply(en.getValue())
                ));
    }

    public static <K1, K2, V1, V2> Map<K2, V2> crossMap(Map<K1, V1> originMap, BiFunction<K1, V1, K2> keyMap,
                                                        BiFunction<K1, V1, V2> valueMap) {
        return originMap.entrySet().stream()
                .collect(ImmutableMap.toImmutableMap(
                        en -> keyMap.apply(en.getKey(), en.getValue()),
                        en -> valueMap.apply(en.getKey(), en.getValue())
                ));
    }


    public static <K, V1, V2> Map<K, V2> valueMap(Map<K, V1> originMap, Function<V1, V2> map) {
        return originMap.entrySet().stream()
                .collect(ImmutableMap.toImmutableMap(
                        Map.Entry::getKey,
                        en -> map.apply(en.getValue())
                ));
    }

    public static <K, V1, V2> Map<K, V2> mutableValueMap(Map<K, V1> originMap, Function<V1, V2> map) {
        return originMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        en -> map.apply(en.getValue())
                ));
    }

}
