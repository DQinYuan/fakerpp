package org.testd.fakerpp.core.engine.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.util.MhAndClass;
import org.testd.fakerpp.core.util.MyReflectUtil;
import org.testd.fakerpp.core.util.MyStringUtil;
import org.testd.fakerpp.core.util.MyReflectUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class Generators {

    private static final String GEN_CLASS_TEMPLATE =
            "org.testd.fakerpp.core.engine.generator.builtin.%sGen";

    @Cacheable("getConsByBuiltInTag")
    public MhAndClass getConsByBuiltInTag(String name) throws ERMLException {
        String camelCaseName = MyStringUtil.delimit2Camel(name, true);
        String qualifiedName = String.format(GEN_CLASS_TEMPLATE, camelCaseName);
        try {
            return new MhAndClass(
                    MyReflectUtil.getNoArgConstructor(qualifiedName, Generator.class),
                    Class.forName(qualifiedName));
        } catch (ClassNotFoundException e) {
            throw new ERMLException(String.format("built-in generator '%s' can not be found",
                    name));
        }
    }

    @Cacheable("getFieldSetter")
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
