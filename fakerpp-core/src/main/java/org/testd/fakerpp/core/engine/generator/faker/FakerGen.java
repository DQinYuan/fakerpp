package org.testd.fakerpp.core.engine.generator.faker;

import lombok.RequiredArgsConstructor;
import org.testd.fakerpp.core.ERMLException;
import org.testd.fakerpp.core.engine.generator.Generator;
import org.testd.fakerpp.core.engine.generator.Generator;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class FakerGen implements Generator {

    private final MethodHandle fakerFunction;
    private final Object[] params;

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

}