package org.testany.fakerpp.core;

import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

@Configuration
public class CustomKeyGenerator extends CachingConfigurerSupport implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append(target.getClass().getName()).append("_")
                .append(method.getName());
        for (Object param : params) {
            sb.append("_");
            if (param instanceof Class) {
                sb.append(((Class) param).getName());
            } else {
                sb.append(param.toString());
            }
        }

        return sb.toString();
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new CustomKeyGenerator();
    }
}
