package org.testany.fakerpp.core.engine.generator.faker;

import lombok.RequiredArgsConstructor;
import org.testany.fakerpp.core.ERMLException;
import org.testany.fakerpp.core.engine.generator.Generator;

import java.lang.invoke.MethodHandle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RequiredArgsConstructor
public class FakerGen implements Generator {

    private final MethodHandle fakerFunction;
    private final Object[] params;

    @Override
    public void init() {

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
