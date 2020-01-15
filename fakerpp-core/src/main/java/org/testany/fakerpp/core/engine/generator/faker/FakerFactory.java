package org.testany.fakerpp.core.engine.generator.faker;

import com.github.javafaker.Faker;
import org.springframework.stereotype.Component;
import org.testany.fakerpp.core.util.SeedableThreadLocalRandom;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class FakerFactory {

    private ThreadLocal<Map<String, Faker>> threadLocalFakers =
            ThreadLocal.withInitial(() -> new HashMap<>());

    public Faker getLangFaker(String lang) {
        Map<String, Faker> threadLocalCache = threadLocalFakers.get();
        return threadLocalCache.computeIfAbsent(lang,
                l -> SeedableThreadLocalRandom.newFaker(l));
    }

}
