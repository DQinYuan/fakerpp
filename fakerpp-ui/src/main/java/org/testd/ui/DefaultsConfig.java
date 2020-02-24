package org.testd.ui;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "fakerdefaults")
@Component
@Data
public class DefaultsConfig {

    private int batchSize;

    private SupportedLocales localesInfo = new SupportedLocales();

    private String storeType;

    @Data
    public class SupportedLocales {

        private String defaultLocale;

        private int separateBelow;

        private List<String> supportedLocales;

    }
}
