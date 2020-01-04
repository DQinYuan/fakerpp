package org.testany.fakerpp.core.util

import com.github.javafaker.Animal
import spock.lang.Specification
import com.github.javafaker.Number

class MyReflectUtilTest extends Specification {

    def "get method map"() {
        expect:
        def map = MyReflectUtil.getMethodMap(Animal.class)
        map.keySet().toList() == ["name"]
    }

    def "get method param info"() {
        given:
        def method = Number.class.getMethod("randomDouble", int.class,
                long.class, long.class)

        when:
        def methodParams = MyReflectUtil.getMethodParam(method)

        then:
        methodParams.keySet() == ["maxNumberOfDecimals", "min", "max"].toSet()
        methodParams.each {
            name, info ->
                switch (name) {
                    case "maxNumberOfDecimals":
                        assert info.order == 0
                        assert info.paramClass.getName() == "int"
                        break
                    case "min":
                        assert info.order == 1
                        assert info.paramClass.getName() == "long"
                        break
                    case "max":
                        assert info.order == 2
                        assert info.paramClass.getName() == "long"
                        break
                }
        }
    }

}
