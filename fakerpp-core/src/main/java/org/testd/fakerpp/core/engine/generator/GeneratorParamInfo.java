package org.testd.fakerpp.core.engine.generator;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.testd.fakerpp.core.engine.generator.builtin.base.DefaultNumber;
import org.testd.fakerpp.core.engine.generator.builtin.base.DefaultString;
import org.testd.fakerpp.core.util.MyStringUtil;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;

@RequiredArgsConstructor
@Getter
public class GeneratorParamInfo {

    private final String name;
    private final Class<?> type;
    private final Object defaultValue;

    private static Set<Class<?>> supportedType = new HashSet<Class<?>>() {
        {
            add(int.class);
            add(Integer.class);
            add(boolean.class);
            add(Boolean.class);
            add(long.class);
            add(Long.class);
            add(String.class);
            add(List.class);
        }
    };

    public static Map<String, GeneratorParamInfo> fromClass(Class<?> c) {
        return Arrays.stream(c.getDeclaredFields())
                .filter(f -> Modifier.isPublic(f.getModifiers()))
                .map(f -> {
                    String pInfoName = MyStringUtil.camelToDelimit(f.getName());
                    DefaultString ds = null;
                    DefaultNumber dn = null;
                    if (f.isAnnotationPresent(DefaultString.class)) {
                        ds = f.getAnnotation(DefaultString.class);
                    }
                    if (f.isAnnotationPresent(DefaultNumber.class)) {
                        dn = f.getAnnotation(DefaultNumber.class);
                    }
                    if ((ds == null && dn == null) || (ds != null && dn != null)) {
                        return new GeneratorParamInfo(pInfoName,
                                f.getType(), null);
                    }
                    return new GeneratorParamInfo(pInfoName,
                            f.getType(), ds != null ? ds.value() : dn.value());
                })
                .filter(pInfo -> supportedType.contains(pInfo.getType()))
                .collect(ImmutableMap.toImmutableMap(
                        GeneratorParamInfo::getName,
                        Function.identity()
                ));
    }



}
