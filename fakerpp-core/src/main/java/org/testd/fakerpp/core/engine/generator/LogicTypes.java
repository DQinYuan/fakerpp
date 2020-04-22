package org.testd.fakerpp.core.engine.generator;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.ClassUtils;

import java.util.Map;

public class LogicTypes {

    // only need to write boxed class
    private static Map<Class<?>, LogicType> logicTypeMap = ImmutableMap.of(
            Integer.class, LogicType.IntType.getInstance(),
            Long.class, LogicType.LongType.getInstance(),
            String.class, LogicType.StringType.getInstance(),
            Boolean.class, LogicType.BooleanType.getInstance()
    );

    public static boolean has(Class<?> clazz) {
        return logicTypeMap.containsKey(ClassUtils.primitiveToWrapper(clazz));
    }

    public static LogicType get(Class<?> clazz) {
        return logicTypeMap.get(ClassUtils.primitiveToWrapper(clazz));
    }

}
