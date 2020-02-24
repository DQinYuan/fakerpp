package org.testd.fakerpp.core.autoconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.testd.fakerpp.core.ERMLExecutor;

@Configuration
@ConditionalOnClass(ERMLExecutor.class)
@ComponentScan(value = "org.testd.fakerpp.core"
        ,excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = ApplicationRunner.class)
)
public class FakerCoreAutoConfigure {

    @Autowired
    ERMLExecutor ermlExecutor;

    @Bean
    @ConditionalOnMissingBean
    ERMLExecutor ermlExecutor() {
        return ermlExecutor;
    }

}
