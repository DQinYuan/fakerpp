package org.testd.fakerpp.core.engine.generator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.testd.fakerpp.core.ERMLException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * use getInitedGenerator to get inited generator
 * use other method to get info
 */
public interface GeneratorSupplier {

    @FunctionalInterface
    interface ParamSetter<T> {
        void setValue(Generator generator, T value);
    }

    @Getter
    @EqualsAndHashCode
    @ToString
    abstract class ParamInfo implements ParamSetter<String> {
        protected final String name;
        protected final Class<?> relType;
        protected final LogicType logicType;
        /**
         * three perhaps values:
         *  1. String: str type default value
         *  2. Long: number type default value
         *  3. String[]: enum value, `defaultValue[0]` is default selected
         */
        protected final Object defaultValue;
        protected final boolean multiLine;

        protected ParamInfo(String name, Class<?> relType,
                            Object defaultValue, boolean multiLine) {
            this.name = name;
            this.relType = relType;
            this.logicType = LogicTypes.get(relType);
            this.defaultValue = defaultValue;
            this.multiLine = multiLine;
        }


        public abstract void setValue(Generator generator, String value);
    }

    Generator generator(String lang);

    Map<String, ParamInfo> paramInfos();

    Optional<ParamSetter<List<List<String>>>> optionSetter();

    default Generator getInitedGenerator(String lang, Map<String, String> attrs,
                                         List<List<String>> options) throws ERMLException {
        Generator generator = generator(lang);
        Map<String, GeneratorSupplier.ParamInfo> paramInfos = paramInfos();
        for (Map.Entry<String, String> attrEntry : attrs.entrySet()) {
            if (paramInfos.containsKey(attrEntry.getKey())) {
                paramInfos.get(attrEntry.getKey()).setValue(generator,
                        attrEntry.getValue());
            } else {
                throw new ERMLException(String.format("Param '%s' not exist in %s"
                        , attrEntry.getKey(), generator.getClass().getSimpleName()));
            }
        }

        optionSetter().ifPresent(opSetter -> {
            assert options != null;
            opSetter.setValue(generator, options);
        });

        return generator;
    }
}
