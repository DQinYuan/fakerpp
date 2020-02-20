package org.testd.fakerpp.core.engine.generator.faker;

import com.github.javafaker.Faker;
import com.google.common.collect.ImmutableMap;
import javassist.NotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.util.MhAndClass;
import org.testd.fakerpp.core.util.MyReflectUtil;
import org.testd.fakerpp.core.util.MyStringUtil;
import org.testd.fakerpp.core.util.MyReflectUtil;
import org.testd.fakerpp.core.util.MyStringUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class FakerInvoker {

    private static class Holder {
        private static Map<String, MhAndClass> methodMap = fakerFieldMap();

        private static Map<String, MhAndClass> fakerFieldMap() {
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
    }

    public MhAndClass fieldInvoker(String field) throws ERMLException {
        field = MyStringUtil.delimit2Camel(field, false);
        if (!Holder.methodMap.containsKey(field)) {
            throw new ERMLException(String.format("field %s not exist", field));
        }

        return Holder.methodMap.get(field);
    }

    private Method selectMethod(Class fieldFakerClazz, String generatorName) throws ERMLException {
        generatorName = MyStringUtil.delimit2Camel(generatorName, false);
        Map<String, List<Method>> methodMap = MyReflectUtil.getMethodMap(fieldFakerClazz);
        if (!methodMap.containsKey(generatorName)) {
            throw new ERMLException(
                    String.format("generator '%s' can not be found in field", generatorName)
            );
        }
        List<Method> methods = methodMap.get(generatorName);
        if (methods.size() <= 0) {
            throw new ERMLException(
                    String.format("generator '%s' can not be found in field", generatorName)
            );
        }

        return methods.get(0);
    }

    @RequiredArgsConstructor
    @Getter
    public static class MethodInfo {
        private final MethodHandle mh;
        private final Map<String, MyReflectUtil.ParamInfo> params;
    }

    /**
     * get method and param order by generator name
     *
     * @param fieldClass
     * @param generatorName
     * @return
     * @throws ERMLException
     */
    @Cacheable("generatorMethod")
    public MethodInfo generatorMethod(Class fieldClass, String generatorName) throws ERMLException {
        Method method = selectMethod(fieldClass, generatorName);
        Map<String, MyReflectUtil.ParamInfo> methodParam = null;
        methodParam = MyReflectUtil.getMethodParam(method);

        MethodHandle mh = null;
        try {
            mh = MethodHandles.publicLookup().unreflect(method);
        } catch (IllegalAccessException e) {
            throw new ERMLException(
                    String.format("can not found generator '%s' in field", generatorName),
                    e);
        }

        return new MethodInfo(mh, methodParam);
    }


}
