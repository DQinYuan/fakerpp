package org.testd.ui.view;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "localesinfo")
@Component
@Data
public class SupportedLocales {

    private String defaultLocale;

    private int separateBelow;

    private List<String> supportedLocales;

}
