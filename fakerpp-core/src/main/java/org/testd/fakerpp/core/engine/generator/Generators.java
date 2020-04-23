package org.testd.fakerpp.core.engine.generator;

import com.google.common.collect.ImmutableMap;
import javassist.ClassPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.engine.generator.builtin.base.DefaultNumber;
import org.testd.fakerpp.core.engine.generator.builtin.base.DefaultString;
import org.testd.fakerpp.core.engine.generator.builtin.base.EnumValues;
import org.testd.fakerpp.core.engine.generator.builtin.base.MultiLine;
import org.testd.fakerpp.core.engine.generator.faker.Fakers;
import org.testd.fakerpp.core.util.MhAndClass;
import org.testd.fakerpp.core.util.MyReflectUtil;
import org.testd.fakerpp.core.util.MyStringUtil;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
@Slf4j
public class Generators {

    private final Fakers fakers;

    public MhAndClass getFieldSetter(Class clazz, String field) {
        try {
            Class fType = clazz.getDeclaredField(field).getType();
            MethodHandle setter = MethodHandles.lookup().findSetter(clazz, field, fType);
            return new MhAndClass(
                    setter.asType(setter.type().changeParameterType(0, Generator.class)),
                    fType);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(String.format("attr %s not exist", field));
        }
    }

    private static final String builtInGeneratorBasePkg =
            "org.testd.fakerpp.core.engine.generator.builtin";

    private static final String optionsField = "options";

    @Cacheable("builtInGenerators")
    public Map<String, GeneratorSupplier> builtInGenerators() {
        return MyReflectUtil
                .subtypes(builtInGeneratorBasePkg, Generator.class)
                .filter(c -> c.getSimpleName().endsWith("Gen"))
                .collect(ImmutableMap.toImmutableMap((Class<? extends Generator> c) -> {
                            String sName = c.getSimpleName();
                            return MyStringUtil
                                    .camelToDelimit(sName.substring(0, sName.length() - 3));
                        },
                        c -> new GeneratorSupplier() {
                            @Override
                            public Generator generator(String lang) {
                                try {
                                    return (Generator) MyReflectUtil
                                            .getNoArgConstructor(c, Generator.class).invokeExact();
                                } catch (Throwable throwable) {
                                    throw new RuntimeException(throwable);
                                }
                            }

                            Map<String, ParamInfo> paramInfos
                                    = paramInfoFromClass(c);

                            @Override
                            public Map<String, ParamInfo> paramInfos() {
                                return paramInfos;
                            }

                            @Override
                            public Optional<ParamSetter<List<List<String>>>> optionSetter() {
                                Field options;
                                try {
                                    options = c.getField(optionsField);
                                } catch (NoSuchFieldException e) {
                                    return Optional.empty();
                                }
                                if (options.getType() != List.class) {
                                    return Optional.empty();
                                }

                                return Optional.of(
                                        (generator, value) -> {
                                            MhAndClass paramSetter = getFieldSetter(c, optionsField);
                                            try {
                                                paramSetter.getMh().invoke(generator, value);
                                            } catch (Throwable e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                );
                            }
                        }));
    }

    private Map<String, GeneratorSupplier.ParamInfo> paramInfoFromClass(Class<?> c) {
        ImmutableMap.Builder<String, GeneratorSupplier.ParamInfo> builder =
                ImmutableMap.builder();
        Field[] sortedFields = MyReflectUtil.getSortedDeclaredFields(c);
        for (Field f : sortedFields) {
            if (!Modifier.isPublic(f.getModifiers()) ||
                    !LogicTypes.has(f.getType())) {
                continue;
            }

            String paramName = MyStringUtil.camelToDelimit(f.getName());

            // get default value
            Object defaultValue = getDefaultValue(f);

            MhAndClass paramSetter = getFieldSetter(c, f.getName());

            builder.put(paramName,
                    new GeneratorSupplier.ParamInfo(paramName, f.getType(),
                            defaultValue, f.isAnnotationPresent(MultiLine.class)) {
                        @Override
                        public void setValue(Generator generator,
                                             String value) {
                            try {
                                paramSetter.getMh().invoke(generator,
                                        this.logicType.cast(value)
                                );
                            } catch (Throwable e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
        }

        return builder.build();
    }

    private Object getDefaultValue(Field f) {
        if (f.isAnnotationPresent(DefaultString.class)) {
            return f.getAnnotation(DefaultString.class).value();
        }
        if (f.isAnnotationPresent(DefaultNumber.class)) {
            return f.getAnnotation(DefaultNumber.class).value();
        }
        if (f.isAnnotationPresent(EnumValues.class)) {
            return f.getAnnotation(EnumValues.class).value();
        }
        return null;
    }

    @Cacheable("generators")
    public Map<String, Map<String, GeneratorSupplier>> generators() {
        return generators(ClassPool.getDefault());
    }


    public Map<String, Map<String, GeneratorSupplier>> generators(ClassPool cp) {
        ImmutableMap.Builder<String, Map<String, GeneratorSupplier>>
                builder = ImmutableMap.builder();
        builder.put("built-in", builtInGenerators());
        builder.putAll(fakers.fakerGenerators(cp));
        return builder.build();
    }

}
