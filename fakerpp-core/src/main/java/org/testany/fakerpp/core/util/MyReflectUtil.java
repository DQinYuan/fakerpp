package org.testany.fakerpp.core.util;

import com.google.common.collect.ImmutableMap;
import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MyReflectUtil {

    private static ConcurrentMap<String, Map<String, List<Method>>> methodMapCache = new ConcurrentHashMap<>();

    public static Map<String, List<Method>> getMethodMap(Class clazz) {
        String cacheKey = clazz.getName();
        if (!methodMapCache.containsKey(cacheKey)) {
            Map<String, List<Method>> methodMap = new HashMap<>();
            for (Method method : clazz.getDeclaredMethods()) {
                List<Method> before = methodMap.getOrDefault(method.getName(), new ArrayList<>());
                before.add(method);
                methodMap.put(method.getName(), before);
            }

            methodMapCache.putIfAbsent(cacheKey, methodMap);
        }

        return methodMapCache.get(cacheKey);
    }

    @RequiredArgsConstructor
    @Getter
    public static class ParamInfo {
        private final Class paramClass;
        private final int order;
    }

    /**
     * do not need cache because of upper layer cache
     *
     * @param m
     * @return
     */
    public static Map<String, ParamInfo> getMethodParam(Method m) {
        ClassPool pool = ClassPool.getDefault();
        CtClass ctClass = null;
        try {
            ctClass = pool.get(m.getDeclaringClass().getName());
            int count = m.getParameterCount();
            Class<?>[] paramTypes = m.getParameterTypes();
            CtClass[] ctParams = new CtClass[count];
            for (int i = 0; i < count; i++) {
                ctParams[i] = pool.getCtClass(paramTypes[i].getName());
            }

            CtMethod ctMethod = ctClass.getDeclaredMethod(m.getName(), ctParams);
            MethodInfo methodInfo = ctMethod.getMethodInfo();
            CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
            LocalVariableAttribute attr =
                    (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
            // start pos if not static, start from 1, skip `this` param
            int pos = Modifier.isStatic(m.getModifiers()) ? 0 : 1;

            ImmutableMap.Builder<String, ParamInfo> res = new ImmutableMap.Builder<>();
            for (int i = 0; i < count; i++) {
                String attrName = attr.variableName(i + pos);
                res.put(attrName, new ParamInfo(paramTypes[i], i));
            }

            return res.build();
        } catch (NotFoundException ignore) {
        }

        return null;
    }
}
