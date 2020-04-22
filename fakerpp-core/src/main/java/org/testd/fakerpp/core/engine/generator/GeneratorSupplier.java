package org.testd.fakerpp.core.engine.generator;

import lombok.Getter;
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
    abstract class ParamInfo implements ParamSetter<String> {
        protected final String name;
        protected final Class<?> relType;
        protected final LogicType logicType;
        protected final Object defaultValue;

        protected ParamInfo(String name, Class<?> relType, Object defaultValue) {
            this.name = name;
            this.relType = relType;
            this.logicType = LogicTypes.get(relType);
            this.defaultValue = defaultValue;
        }


        public abstract void setValue(Generator generator, String value);

        @Override
        public String toString() {
            return "ParamInfo{" +
                    "name='" + name + '\'' +
                    ", relType=" + relType +
                    ", logicType=" + logicType +
                    ", defaultValue=" + defaultValue +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ParamInfo)) return false;
            ParamInfo paramInfo = (ParamInfo) o;
            return Objects.equals(getName(), paramInfo.getName()) &&
                    Objects.equals(getRelType(), paramInfo.getRelType()) &&
                    Objects.equals(getLogicType(), paramInfo.getLogicType()) &&
                    Objects.equals(getDefaultValue(), paramInfo.getDefaultValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getName(), getRelType(), getLogicType(), getDefaultValue());
        }
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
