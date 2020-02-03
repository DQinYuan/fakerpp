package org.testany.fakerpp.core.parser.ast;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class Table {

    @Getter
    @RequiredArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Joins {
        private final List<Join> leftJoins;
        private final List<Join> rightJoins;
    }

    @Getter
    @RequiredArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class Join {
        // from -> content
        private final Map<String, String> map;
        private final String depend;
        // only valid in right join
        private final boolean random;
    }

    @Getter
    @RequiredArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class ColFamily {
        private final List<String> cols;
        // perhaps compose generators
        private final List<GeneratorInfo> generatorInfos;
    }

    @Getter
    @RequiredArgsConstructor
    @ToString
    @EqualsAndHashCode
    public static class GeneratorInfo {
        private final String field;
        private final String lang;
        private final String generator;
        private final Map<String, String> attributes;
        private final List<List<String>> options;
        private final int weight;
    }

    private final String name;
    private final String ds;
    private final int num;

    private final Joins joins;
    private final List<ColFamily> colFamilies;
    private final List<String> excludes;

}
