package org.testd.fakerpp.core.engine.generator;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.engine.generator.faker.Fakers;
import org.testd.fakerpp.core.util.MhAndClass;
import org.testd.fakerpp.core.util.MyReflectUtil;
import org.testd.fakerpp.core.util.MyStringUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Slf4j
public class Generators {

    private final Fakers fakers;

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

    private void setBuiltInGeneratorAttr(Generator generator, Map<String, String> attrs,
                                         List<List<String>> options) throws ERMLException {
        // attrs
        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            MhAndClass gField = getFieldSetter(generator.getClass(), entry.getKey());
            String attrValue = entry.getValue();
            if (int.class.equals(gField.getClazz())) {
                try {
                    gField.getMh().invokeExact(generator, Integer.parseInt(attrValue));
                } catch (Throwable ignore) {
                    ignore.printStackTrace();
                }
            } else if (long.class.equals(gField.getClazz())) {
                try {
                    gField.getMh().invokeExact(generator, Long.parseLong(attrValue));
                } catch (Throwable ignore) {
                    ignore.printStackTrace();
                }
            } else if (String.class.equals(gField.getClazz())) {
                try {
                    gField.getMh().invokeExact(generator, attrValue);
                } catch (Throwable ignore) {
                    ignore.printStackTrace();
                }
            } else {
                log.warn("attribute {}'s type error, it is {}",
                        entry.getKey(), gField.getClazz().getName());
            }
        }


        try {
            getFieldSetter(generator.getClass(), "options")
                    .getMh().invokeExact(generator, options);
        } catch (Throwable ignore) {
        }
    }

    @Cacheable("builtInGenerators")
    public Map<String, GeneratorSupplier> builtInGenerators() {
        return MyReflectUtil
                .subtypes("org.testd.fakerpp.core.engine.generator.builtin", Generator.class)
                .filter(c -> c.getSimpleName().endsWith("Gen"))
                .collect(ImmutableMap.toImmutableMap((Class<? extends Generator> c) -> {
                            String sName = c.getSimpleName();
                            return MyStringUtil
                                    .camelToDelimit(sName.substring(0, sName.length() - 3));
                        },
                        c -> new GeneratorSupplier() {
                            @Override
                            public Generator getGenerator(String lang, Map<String, String> attributes, List<List<String>> options) {
                                Generator generator = null;
                                try {
                                    generator = (Generator) MyReflectUtil
                                            .getNoArgConstructor(c, Generator.class).invokeExact();
                                    setBuiltInGeneratorAttr(generator, attributes, options);
                                    return generator;
                                } catch (Throwable throwable) {
                                    throw new RuntimeException(throwable);
                                }
                            }

                            @Override
                            public Map<String, GeneratorParamInfo> paramInfos() {
                                return GeneratorParamInfo.fromClass(c);
                            }
                        }));
    }

    @Cacheable("generators")
    public Map<String, Map<String, GeneratorSupplier>> generators() {
        ImmutableMap.Builder<String, Map<String, GeneratorSupplier>>
                builder = ImmutableMap.builder();
        builder.put("built-in", builtInGenerators());
        builder.putAll(fakers.fakerGenerators());
        return builder.build();
    }

}
