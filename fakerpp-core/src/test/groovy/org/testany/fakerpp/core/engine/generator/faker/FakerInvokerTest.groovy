package org.testany.fakerpp.core.engine.generator.faker

import com.github.javafaker.Animal
import com.github.javafaker.Faker
import com.github.javafaker.HarryPotter
import spock.lang.Shared
import spock.lang.Specification
import com.github.javafaker.Number

class FakerInvokerTest extends Specification {

    @Shared
    FakerInvoker fakerInvoker

    def setupSpec() {
        fakerInvoker = new FakerInvoker()
    }

    def "get faker field object"() {
        expect:
        def invoker = fakerInvoker.fieldInvoker("animal")
        invoker.mh.invokeWithArguments(new Faker()) instanceof Animal
        def invoker2 = fakerInvoker.fieldInvoker("harry-potter")
        invoker2.mh.invokeWithArguments(new Faker()) instanceof HarryPotter
    }

    def "test generatorMethod"() {
        given:
        def clazz = Number.class
        def gName = "random-double"

        when:
        def methodInfo = fakerInvoker.generatorMethod(clazz, gName)
        def obj = new Faker().number()

        then:
        methodInfo.params.keySet() == ["maxNumberOfDecimals", "min", "max"].toSet()
        methodInfo.mh.invokeWithArguments(obj, 8, 1, 9) >= 1
        methodInfo.mh.invokeWithArguments(obj, 8, 1, 9) < 9
    }
}
