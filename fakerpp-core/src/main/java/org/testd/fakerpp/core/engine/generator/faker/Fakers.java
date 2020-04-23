package org.testd.fakerpp.core.engine.generator.faker;

import com.github.javafaker.Faker;
import com.google.common.collect.ImmutableMap;
import javassist.ClassPool;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.testd.fakerpp.core.engine.generator.*;
import org.testd.fakerpp.core.util.MhAndClass;
import org.testd.fakerpp.core.util.MyMapUtil;
import org.testd.fakerpp.core.util.MyReflectUtil;
import org.testd.fakerpp.core.util.MyStringUtil;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
@Component
public class Fakers {

    private final FakerFactory fakerFactory;
    private final FakerInvoker fakerInvoker;

    @RequiredArgsConstructor
    @Getter
    private static class ParamVal {
        private final Class clazz;
        private final Object val;
    }

    private Object[] convertParams(ParamVal[] params) {
        List<Object> realParams = new ArrayList<>(params.length);

        for (ParamVal param : params) {
            final Object paramVal = param.getVal();
            Function<String, Object> parseFunc = null;
            switch (param.getClazz().getSimpleName()) {
                case "int":
                case "Integer":
                    parseFunc = Integer::parseInt;
                    break;
                case "long":
                case "Long":
                    parseFunc = Long::parseLong;
                    break;
                case "float":
                case "Float":
                    parseFunc = Float::parseFloat;
                    break;
                case "double":
                case "Double":
                    parseFunc = Double::parseDouble;
                    break;
            }
            if (parseFunc != null) {
                realParams.add(parseFunc.apply(paramVal.toString()));
            } else {
                realParams.add(paramVal);
            }
        }
        return realParams.toArray();
    }

    private Object newFakerField(MethodHandle fieldGetter, String lang) {
        Faker langFaker = fakerFactory.getLangFaker(lang);
        Object fieldFaker = null;
        try {
            fieldFaker = fieldGetter.invokeExact(langFaker);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
        return fieldFaker;
    }

    private Object[] generatorParams(FakerInvoker.MethodInfo generatorMethodInfo,
                                     Map<String, String> attributes) {
        Map<String, MyReflectUtil.ParamInfo> paramInfos = generatorMethodInfo.getParams();
        ParamVal[] params = new ParamVal[generatorMethodInfo.getParams().size()];

        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String paramName = MyStringUtil.delimit2Camel(entry.getKey(), false);
            String value = entry.getValue();
            MyReflectUtil.ParamInfo paramInfo = paramInfos.get(paramName);
            if (paramInfo == null) {
                log.warn("param {} not exist in generator", paramName);
                continue;
            }
            params[paramInfo.getOrder()] = new ParamVal(paramInfo.getParamClass(),
                    value);
        }

        return convertParams(params);
    }

    @Cacheable("fakerGenerators")
    public Map<String, Map<String, GeneratorSupplier>> fakerGenerators(ClassPool cp) {
        ImmutableMap.Builder<String, Map<String, GeneratorSupplier>> fieldBuilder
                = ImmutableMap.builder();
        Map<String, MhAndClass> fakerFields = fakerInvoker.fakerFieldMap();

        for (Map.Entry<String, MhAndClass> fieldsEn : fakerFields.entrySet()) {
            String fieldName = fieldsEn.getKey();
            MhAndClass fieldMh = fieldsEn.getValue();

            ImmutableMap.Builder<String, GeneratorSupplier> generatorBuilder
                    = new ImmutableMap.Builder<>();

            for (Map.Entry<String, FakerInvoker.MethodInfo> generatorsEn :
                    fakerInvoker.fieldMethodMap(fieldMh.getClazz(), cp).entrySet()) {
                String generatorName = generatorsEn.getKey();
                FakerInvoker.MethodInfo generatorMethodInfo = generatorsEn.getValue();
                generatorBuilder.put(MyStringUtil.camelToDelimit(generatorName),
                        new GeneratorSupplier() {
                            @Override
                            public Generator generator(String lang) {

                                return new FakerGen(
                                        generatorMethodInfo.getMh().bindTo(newFakerField(fieldMh.getMh(),
                                                lang)),
                                        MyMapUtil.map(
                                                generatorMethodInfo.getParams(),
                                                MyStringUtil::camelToDelimit,
                                                MyReflectUtil.ParamInfo::getOrder
                                        )
                                );
                            }

                            private final Map<String, ParamInfo> paramInfos = MyMapUtil.crossMap(generatorMethodInfo.getParams(),
                                    (name, paramInfo) -> MyStringUtil.camelToDelimit(name),
                                    (name, paramInfo) -> new ParamInfo(MyStringUtil.camelToDelimit(name), paramInfo.getParamClass(),
                                            null, false) {
                                        @Override
                                        public void setValue(Generator generator,
                                                             String value){
                                            FakerGen fGen = (FakerGen) generator;
                                            fGen.setParam(this.name,
                                                    this.logicType.cast(value));
                                        }
                                    });

                            @Override
                            public Map<String, ParamInfo> paramInfos() {
                                return paramInfos;
                            }

                            @Override
                            public Optional<ParamSetter<List<List<String>>>> optionSetter() {
                                return Optional.empty();
                            }
                        });
            }

            Map<String, GeneratorSupplier> genMap = generatorBuilder.build();
            if (!genMap.isEmpty()) {
                fieldBuilder.put(MyStringUtil.camelToDelimit(fieldName), genMap);
            }


        }

        return fieldBuilder.build();
    }

}
