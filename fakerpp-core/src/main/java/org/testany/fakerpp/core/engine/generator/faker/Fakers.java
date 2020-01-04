package org.testany.fakerpp.core.engine.generator.faker;

import com.github.javafaker.Faker;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.generator.Generator;
import org.testany.fakerpp.core.util.MhAndClass;
import org.testany.fakerpp.core.util.MyReflectUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

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

    public Generator fakerGenerator(String lang, String field, String generator,
                                    Map<String, String> attrs,
                                    Map<String, List<String>> listAttrs) throws ERMLException {
        Faker langFaker = fakerFactory.getLangFaker(lang);
        MhAndClass mhAndClass = fakerInvoker.fieldInvoker(field);
        Object fieldFaker = null;
        try {
            fieldFaker = mhAndClass.getMh().invokeExact(langFaker);
        } catch (Throwable throwable) {
            throw new ERMLException(
                    String.format("field %s invoke fail", field),
                    throwable);
        }

        // bindTo
        FakerInvoker.MethodInfo methodInfo = fakerInvoker
                .generatorMethod(fieldFaker.getClass(), generator);
        Map<String, MyReflectUtil.ParamInfo> paramInfos = methodInfo.getParams();
        ParamVal[] params = new ParamVal[methodInfo.getParams().size()];

        BiConsumer<String, Object> transFunc = (paramName, value) -> {
            MyReflectUtil.ParamInfo paramInfo = paramInfos.get(paramName);
            params[paramInfo.getOrder()] = new ParamVal(paramInfo.getParamClass(),
                    value);
        };
        attrs.forEach(transFunc);
        listAttrs.forEach(transFunc);

        return new FakerGen(
                methodInfo.getMh().bindTo(fieldFaker),
                convertParams(params));
    }

    private Object[] convertParams(ParamVal[] params) {
        List realParams = new ArrayList<>(params.length);

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

}
