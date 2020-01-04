package org.testany.fakerpp.core.engine.generator.faker;

import com.github.javafaker.Faker;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Component
public class FakerFactory {

    private ThreadLocal<Map<String, Faker>> threadLocalFakers =
            ThreadLocal.withInitial(() -> new HashMap<>());

    public Faker getLangFaker(String lang) {
        Map<String, Faker> threadLocalCache = threadLocalFakers.get();
        if (!threadLocalCache.containsKey(lang)) {
            threadLocalCache.put(lang, new Faker(new Locale(lang)));
        }

        return threadLocalCache.get(lang);
    }

}
