package org.testany.fakerpp.core.engine.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.util.MhAndClass;
import org.testany.fakerpp.core.util.MyStringUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class Generators {

    private static final String GEN_CLASS_TEMPLATE =
            "org.testany.fakerpp.core.engine.generator.builtin.%sGen";

    @Cacheable
    public MhAndClass getConsByBuiltInTag(String name) throws ERMLException {
        String camelCaseName = MyStringUtil.delimit2Camel(name, true);
        String qualifiedName = String.format(GEN_CLASS_TEMPLATE, camelCaseName);
        try {
            Class genClass = Class.forName(qualifiedName);
            MethodHandle constructor = MethodHandles.lookup().findConstructor(genClass,
                    MethodType.methodType(void.class));
            return new MhAndClass(
                    // https://stackoverflow.com/questions/27278314/why-cant-i-invokeexact-here-even-though-the-methodtype-is-ok
                    constructor.asType(constructor.type().changeReturnType(Generator.class)),
                    genClass);
        } catch (ClassNotFoundException e) {
            throw new ERMLException(String.format("built-in generator '%s' can not be found",
                    name));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Cacheable
    public MhAndClass getFieldSetter(Class clazz, String field) throws ERMLException {
        try {
            Class fType = clazz.getDeclaredField(field).getType();
            MethodHandle setter = MethodHandles.lookup().findSetter(clazz, field, fType);
            return new MhAndClass(
                    setter.asType(setter.type().changeParameterType(0, Generator.class)),
                    fType);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ERMLException(String.format("attr %s not exist", field));
        }
    }


    public Generator builtInGenerator(String name,
                                      Map<String, String> attrs,
                                      List<List<String>> options) throws ERMLException {
        // construct
        MhAndClass generatorCM =
                getConsByBuiltInTag(name);
        Generator generator = null;
        try {
            generator = (Generator) generatorCM.getMh().invokeExact();
        } catch (Throwable ignore) {
            ignore.printStackTrace();
        }

        // attrs
        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            MhAndClass gField = getFieldSetter(generatorCM.getClazz(), entry.getKey());
            String attrValue = entry.getValue();
            String fieldType = gField.getClazz().getName();
            if ("int".equals(fieldType)) {
                try {
                    gField.getMh().invokeExact(generator, Integer.parseInt(attrValue));
                } catch (Throwable ignore) {
                    ignore.printStackTrace();
                }
            } else if ("long".equals(fieldType)) {
                try {
                    gField.getMh().invokeExact(generator, Long.parseLong(attrValue));
                } catch (Throwable ignore) {
                    ignore.printStackTrace();
                }
            } else if ("java.lang.String".equals(fieldType)) {
                try {
                    gField.getMh().invokeExact(generator, attrValue);
                } catch (Throwable ignore) {
                    ignore.printStackTrace();
                }
            } else {
                log.warn("<{}> tag's attribute {}'s type error, it is {}", name,
                        entry.getKey(), fieldType);
            }
        }


        try {
            getFieldSetter(generatorCM.getClazz(),"options")
                    .getMh().invokeExact(generator, options);
        } catch (Throwable ignore) {
        }


        return generator;
    }

}
