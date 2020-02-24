package org.testd.ui

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testd.fakerpp.core.ERMLExecutor
import spock.lang.Specification

@SpringBootTest
class FakerppUIApplicationTest extends Specification {

    @Autowired
    ERMLExecutor executor

    @Autowired
    DefaultsConfig defaultsConfig


    def "test di"() {
        expect:
        println(defaultsConfig.getLocalesInfo().getDefaultLocale())
        println(defaultsConfig.getLocalesInfo().getSupportedLocales())
        println(defaultsConfig.getBatchSize())
    }

}
