package org.testany.fakerpp.core.engine.generator.faker

import spock.lang.Specification

class FakerFactoryTest extends Specification {

    boolean isChineseByRange(String str) {
        if (str == null) {
            return false
        }
        char[] ch = str.toCharArray()
        for (char c : ch) {
            if (c < 0x4E00 || c > 0x9FBF) {
                return false
            }
        }
        return true
    }

    def "cache faker"() {
        given:
        def factory = new FakerFactory()

        when:
        def f1 = factory.getLangFaker("zh-CN")
        def f2 = factory.getLangFaker("zh-CN")
        def f3 = factory.getLangFaker("en")

        then:
        f1.is(f2)
        !f1.is(f3)
        isChineseByRange(f1.name().fullName())
    }

}
