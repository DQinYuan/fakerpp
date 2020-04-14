package org.testd.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Separator;
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

    private double lineEvadeInterval;

    @Data
    public class SupportedLocales {

        private String defaultLocale;

        private int separateBelow;

        private List<String> supportedLocales;

        public ObservableList getLocaleItems() {
            ObservableList items = FXCollections.observableArrayList();
            List<String> locales = supportedLocales;

            for (int i = 0; i < locales.size(); i++) {
                if (i == separateBelow) {
                    items.add(new Separator());
                }
                items.add(locales.get(i));
            }

            return items;
        }

    }
}
