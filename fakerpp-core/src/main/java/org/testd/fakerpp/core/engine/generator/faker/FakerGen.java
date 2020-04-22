package org.testd.fakerpp.core.engine.generator.faker;

import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.engine.generator.Generator;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FakerGen implements Generator {

    private final MethodHandle fakerFunction;
    private final Object[] params;
    private final Map<String, Integer> paramOrder;

    public FakerGen(MethodHandle fakerFunction, Map<String, Integer> paramOrder) {
        this.params = new Object[paramOrder.size()];
        this.fakerFunction = fakerFunction;
        this.paramOrder = paramOrder;
    }

    @Override
    public void init(int colNum) {

    }

    @Override
    public List<String> nextData() throws ERMLException {
        try {
            // invokeWithArguments can uncrate Object[] to variable length param
            return Arrays.asList(fakerFunction.invokeWithArguments(params).toString());
        } catch (Throwable e) {
            throw new ERMLException("Faker method invoke failure", e);
        }
    }

    @Override
    public long dataNum() {
        return 0;
    }

    public void setParam(String paramName, Object value) {
        params[paramOrder.get(paramName)] = value;
    }

}
