package org.testd.fakerpp.core.engine.generator;

import lombok.Getter;

public abstract class LogicType {

    @Getter
    private final String xsdType;

    public LogicType(String xsdType) {
        this.xsdType = xsdType;
    }

    public abstract Object cast(String value);

    public static class IntType extends LogicType {

        private static IntType instance = new IntType();

        private IntType() {
            super("xs:int");
        }

        public static IntType getInstance() {
            return instance;
        }

        @Override
        public Object cast(String value) {
            return Integer.parseInt(value);
        }
    }

    public static class LongType extends LogicType {

        private static LongType instance = new LongType();

        private LongType() {
            super("xs:long");
        }

        public static LongType getInstance() {
            return instance;
        }

        @Override
        public Object cast(String value) {
            return Long.parseLong(value);
        }
    }

    public static class StringType extends LogicType {

        private static StringType instance = new StringType();

        private StringType() {
            super("xs:token");
        }

        public static StringType getInstance() {
            return instance;
        }

        @Override
        public Object cast(String value) {
            return value;
        }
    }

    public static class BooleanType extends LogicType {

        private static BooleanType instance = new BooleanType();

        private BooleanType() {
            super("xs:boolean");
        }

        public static BooleanType getInstance() {
            return instance;
        }

        @Override
        public Object cast(String value) {
            return Boolean.parseBoolean(value);
        }
    }

}
