package org.testany.fakerpp.core.engine.generator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.WordUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.testany.fakerpp.core.ERMLException;

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

    @RequiredArgsConstructor
    private static class GenClass {
        private final Class clazz;
        private final MethodHandle mh;
    }

    @Cacheable
    public GenClass getConsByBuiltInTag(String name) throws ERMLException {
        String camelCaseName = WordUtils.capitalizeFully(name, '-')
                .replaceAll("-", "");
        String qualifiedName = String.format(GEN_CLASS_TEMPLATE, camelCaseName);
        try {
            Class genClass = Class.forName(qualifiedName);
            MethodHandle constructor = MethodHandles.lookup().findConstructor(genClass,
                    MethodType.methodType(void.class));
            return new GenClass(genClass,
                    // https://stackoverflow.com/questions/27278314/why-cant-i-invokeexact-here-even-though-the-methodtype-is-ok
                    constructor.asType(constructor.type().changeReturnType(Generator.class)));
        } catch (ClassNotFoundException e) {
            throw new ERMLException(String.format("built-in generator '%s' can not be found",
                    name));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @RequiredArgsConstructor
    private static class GenField {
        private final Class fieldClass;
        private final MethodHandle setter;
    }

    @Cacheable
    public GenField getFieldSetter(Class clazz, String field) throws ERMLException {
        try {
            Class fType = clazz.getDeclaredField(field).getType();
            MethodHandle setter = MethodHandles.lookup().findSetter(clazz, field, fType);
            return new GenField(fType,
                    setter.asType(setter.type().changeParameterType(0, Generator.class)));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ERMLException(String.format("attr %s not exist", field));
        }
    }


    public Generator builtInGenerator(String name,
                                      Map<String, String> attrs,
                                      Map<String, List<String>> listAttrs) throws ERMLException {
        // construct
        GenClass generatorCM =
                getConsByBuiltInTag(name);
        Generator generator = null;
        try {
            generator = (Generator) generatorCM.mh.invokeExact();
        } catch (Throwable ignore) {
            ignore.printStackTrace();
        }

        // attrs
        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            GenField gField = getFieldSetter(generatorCM.clazz, entry.getKey());
            String attrValue = entry.getValue();
            String fieldType = gField.fieldClass.getName();
            if ("int".equals(fieldType)) {
                try {
                    gField.setter.invokeExact(generator, Integer.parseInt(attrValue));
                } catch (Throwable ignore) {
                    ignore.printStackTrace();
                }
            } else if ("long".equals(fieldType)) {
                try {
                    gField.setter.invokeExact(generator, Long.parseLong(attrValue));
                } catch (Throwable ignore) {
                    ignore.printStackTrace();
                }
            } else if ("java.lang.String".equals(fieldType)) {
                try {
                    gField.setter.invokeExact(generator, attrValue);
                } catch (Throwable ignore) {
                    ignore.printStackTrace();
                }
            } else {
                log.warn("<{}> tag's attribute {}'s type error, it is {}", name,
                        entry.getKey(), fieldType);
            }
        }

        for (Map.Entry<String, List<String>> entry : listAttrs.entrySet()) {
            try {
                getFieldSetter(generatorCM.clazz, entry.getKey())
                        .setter.invokeExact(generator, entry.getValue());
            } catch (Throwable ignore) {
                ignore.printStackTrace();
            }
        }

        return generator;
    }

}
