package org.testd.fakerpp.core.engine.generator.builtin.base;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValues {
    /**
     * first value in String[] will be default value
     * @return
     */
    String[] value();
}
