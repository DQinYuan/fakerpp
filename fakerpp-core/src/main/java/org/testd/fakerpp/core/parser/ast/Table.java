package org.testd.fakerpp.core.parser.ast;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

        public static Joins emptyJoins = new Joins(ImmutableList.of(), ImmutableList.of());
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

        /**
         * if lang field is "default",
         * means it will follow the global lang in {@link org.testd.fakerpp.core.parser.ast.Meta}
         */
        public static final String FOLLOW_DEFAULT_LANG = "default";

        public static final String BUILT_IN_FIELD = "built-in";

        private final String field;
        private final String lang;
        private final String generator;
        private final Map<String, String> attributes;
        private final List<List<String>> options;
        private final int weight;

        private static ConcurrentMap<String, GeneratorInfo> emptyBuiltInInfoCache =
                new ConcurrentHashMap<>();

        public static GeneratorInfo emptyBuiltInInfo(String generator) {
            return emptyBuiltInInfoCache.computeIfAbsent(generator, gen ->  new GeneratorInfo(
                    BUILT_IN_FIELD,
                    FOLLOW_DEFAULT_LANG,
                    gen,
                    ImmutableMap.of(),
                    ImmutableList.of(),
                    1
            ));
        }

        public static GeneratorInfo builtInInfo(String generator,
                                                Map<String, String> attributes) {
            return emptyBuiltInInfoCache.computeIfAbsent(generator, gen ->  new GeneratorInfo(
                    FOLLOW_DEFAULT_LANG,
                    BUILT_IN_FIELD,
                    gen,
                    attributes,
                    ImmutableList.of(),
                    1
            ));
        }

    }

    private final String name;
    private final String ds;
    private final int num;

    private final Joins joins;
    private final List<ColFamily> colFamilies;
    private final List<String> excludes;

}
