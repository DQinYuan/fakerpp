package org.testany.fakerpp.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;


@Component
@ConfigurationProperties("fakerpp")
@Data
// open the validation
@Validated
public class FakerppProperties {
    private Store store;

    @Data
    public static class Store {
        @Min(1)
        private int batchSize = 100;
    }
}
