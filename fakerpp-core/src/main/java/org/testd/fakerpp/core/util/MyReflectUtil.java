package org.testd.fakerpp.core.util;

import com.github.javafaker.Faker;
import com.google.common.collect.ImmutableMap;
import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class MyReflectUtil {

    private static ConcurrentMap<String, Map<String, List<Method>>> methodMapCache = new ConcurrentHashMap<>();

    public static List<Method> getFakerFieldMethod() {
        return Arrays.stream(getSortedDeclaredMethods(Faker.class))
                // none param, public, not static
                .filter(method -> method.getParameterCount() == 0 && java.lang.reflect.Modifier.isPublic(method.getModifiers())
                        && !java.lang.reflect.Modifier.isStatic(method.getModifiers()))
                .collect(Collectors.toList());
    }

    public static Method[] getSortedDeclaredMethods(Class clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        Arrays.sort(methods, Comparator.comparing(Method::toString));
        return methods;
    }

    public static Map<String, List<Method>> getMethodMap(Class clazz) {
        return methodMapCache.computeIfAbsent(
                clazz.getName(), cacheKey -> {
                    Map<String, List<Method>> methodMap = new HashMap<>();
                    for (Method method : getSortedDeclaredMethods(clazz)) {
                        List<Method> before = methodMap.getOrDefault(method.getName(), new ArrayList<>());
                        before.add(method);
                        methodMap.put(method.getName(), before);
                    }
                    return methodMap;
                }
        );
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

    private static ConcurrentMap<String, MethodHandle> consCache =
            new ConcurrentHashMap<>();

    public static MethodHandle getNoArgConstructor(String qualifiedName,
                                                   Class typeToGet) throws ClassNotFoundException {
        String cacheKey = qualifiedName + "_" +typeToGet.getName();
        if (!consCache.containsKey(cacheKey)) {
            Class genClass = null;
            genClass = Class.forName(qualifiedName);
            MethodHandle constructor = null;
            try {
                constructor = MethodHandles.lookup().findConstructor(genClass,
                        MethodType.methodType(void.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new AssertionError(
                        String.format("class %s do not have no arg constructor", qualifiedName), e);
            }
            // https://stackoverflow.com/questions/27278314/why-cant-i-invokeexact-here-even-though-the-methodtype-is-ok
            consCache.put(cacheKey, constructor.asType(constructor.type().changeReturnType(typeToGet)));
        }

        return consCache.get(cacheKey);
    }
}
