package org.testd.fakerpp.core.engine.generator.faker;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.util.MhAndClass;
import org.testd.fakerpp.core.util.MyReflectUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

@Slf4j
@Component
public class FakerInvoker {


    @Cacheable("fakerFieldMap")
    public Map<String, MhAndClass> fakerFieldMap() {
        ImmutableMap.Builder<String, MhAndClass> fieldMap = new ImmutableMap.Builder<>();
        for (Method method : MyReflectUtil.getFakerFieldMethod()) {
            try {
                MethodHandle originMh = MethodHandles.publicLookup().unreflect(method);
                fieldMap.put(method.getName(),
                        new MhAndClass(
                                originMh.asType(originMh.type().changeReturnType(Object.class)),
                                method.getReturnType()
                        ));
            } catch (IllegalAccessException ignore) {
                log.warn("can not reflect faker field method  \n", ignore);
            }
        }

        return fieldMap.build();
    }

    private boolean filterCollectionParamMethod(MethodInfo methodInfo) {
        for (MyReflectUtil.ParamInfo paramInfo : methodInfo.getParams().values()) {
            if (Collection.class.isAssignableFrom(paramInfo.getParamClass())) {
                return false;
            }
        }
        return true;
    }

    public Map<String, MethodInfo> fieldMethodMap(Class<?> fieldFakerClazz) {

        ImmutableMap.Builder<String, MethodInfo> fieldMethodMapBuilder = new ImmutableMap.Builder<>();
        MyReflectUtil.getMethodMap(fieldFakerClazz).forEach((name, methods) -> {
            // select one method from overload methods
            if (methods != null && methods.size() > 0) {
                methods.stream()
                        .filter(m -> !Modifier.isStatic(m.getModifiers()) && !Modifier.isPrivate(m.getModifiers()))
                        .map(this::methodInfoFromMethod)
                        .filter(this::filterCollectionParamMethod)
                        .findFirst()
                .map(info -> fieldMethodMapBuilder.put(name, info));
            }
        });

        return fieldMethodMapBuilder.build();
    }

    @RequiredArgsConstructor
    @Getter
    public static class MethodInfo {
        private final MethodHandle mh;
        private final Map<String, MyReflectUtil.ParamInfo> params;
    }

    private MethodInfo methodInfoFromMethod(Method method) {
        Map<String, MyReflectUtil.ParamInfo> methodParam = MyReflectUtil.getMethodParam(method);

        MethodHandle mh = null;
        try {
            mh = MethodHandles.publicLookup().unreflect(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return new MethodInfo(mh, methodParam);
    }


}
