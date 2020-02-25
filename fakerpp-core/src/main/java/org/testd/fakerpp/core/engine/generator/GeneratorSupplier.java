package org.testd.fakerpp.core.engine.generator;

import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface GeneratorSupplier {

    Generator getGenerator(String lang, Map<String, String> attributes, List<List<String>> options);

}
