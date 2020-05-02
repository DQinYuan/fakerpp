package org.testd.ui.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import org.apache.commons.lang3.StringUtils;
import org.testd.ui.util.binders.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class BindingUtil {

    /**
     * @param op
     * @param predicate
     * @return a BooleanBinding bind with predicate about op
     */
    public static BooleanBinding predicate(final ObservableStringValue op,
                                           final Predicate<String> predicate) {
        if (op == null) {
            throw new NullPointerException("Operand cannot be null");
        }

        return new BooleanBinding() {
            {
                super.bind(op);
            }

            @Override
            public void dispose() {
                super.unbind(op);
            }

            @Override
            protected boolean computeValue() {
                return predicate.test(op.get());
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(op);
            }
        };
    }

    /**
     * @param op
     * @return a BooleanBinding with op is numberic
     */
    public static BooleanBinding isNumberic(final ObservableStringValue op) {
        if (op == null) {
            throw new NullPointerException("Operand cannot be null");
        }

        return new BooleanBinding() {
            {
                super.bind(op);
            }

            @Override
            public void dispose() {
                super.unbind(op);
            }

            @Override
            protected boolean computeValue() {
                return StringUtils.isNumeric(op.get());
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(op);
            }
        };
    }

    public static <T> void bindWithSourceInit(Property<T> bind, Property<T> source) {
        source.setValue(bind.getValue());
        bind.bind(source);
    }

    public static <T> void bindWithSourceInit(List<T> bind, ObservableList<T> source) {
        source.setAll(bind);
        Bindings.bindContent(bind, source);
    }

    public static <K, V> void bindKeyValue(Set<K> bindKey,
                                           Set<V> bindVaue
            , ObservableMap<K, V> source) {
        final KeyValueSetBinder<K, V> keyValueSetBinder =
                new KeyValueSetBinder<>(bindKey, bindVaue);

        bindKey.clear();
        bindKey.addAll(source.keySet());

        bindVaue.clear();
        bindVaue.addAll(source.values());

        source.removeListener(keyValueSetBinder);
        source.addListener(keyValueSetBinder);
    }

    public static <K, V> void bindValue(List<V> valueList,
                                        ObservableMap<K, V> source) {
        final ValueListBinder<K, V> valueListBinder =
                new ValueListBinder<>(valueList);

        if (valueList instanceof ObservableList) {
            ((ObservableList<V>)valueList).setAll(source.values());
        } else {
            valueList.clear();
            valueList.addAll(source.values());
        }

        source.removeListener(valueListBinder);
        source.addListener(valueListBinder);
    }


    @SuppressWarnings("unchecked")
    public static void bindContentTypeUnsafe(List bind, ObservableList source) {
        final ListContentUnsafeBinder contentBinding =
                new ListContentUnsafeBinder(bind);
        if (bind instanceof ObservableList) {
            ((ObservableList) bind).setAll(source);
        } else {
            bind.clear();
            bind.addAll(source);
        }
        source.removeListener(contentBinding);
        source.addListener(contentBinding);
    }

    public static <EK, EV, FK, FV> void mapContentWithoutInit(Map<FK, FV> mapped,
                                                   ObservableMap<EK, EV> source,
                                                   Function<EK, FK> keyMapper,
                                                   Function<EV, FV> valueMapper) {
        MapContentBinder<EK, EV, FK, FV> contentBinder =
                new MapContentBinder<>(mapped, keyMapper, valueMapper);
        source.removeListener(contentBinder);
        source.addListener(contentBinder);
    }

    public static <EK, EV, FK, FV> void mapContent(Map<FK, FV> mapped,
                                                   ObservableMap<EK, EV> source,
                                                   Function<EK, FK> keyMapper,
                                                   Function<EV, FV> valueMapper) {
        MapContentBinder<EK, EV, FK, FV> contentBinder =
                new MapContentBinder<>(mapped, keyMapper, valueMapper);
        mapped.clear();
        mapped.putAll(
                source.entrySet().stream()
                .collect(toMap(en -> keyMapper.apply(en.getKey()), en -> valueMapper.apply(en.getValue())))
        );
        source.removeListener(contentBinder);
        source.addListener(contentBinder);
    }

    /**
     * bind a map with list according to keyMapper and valueMapper
     * @param mapped
     * @param source
     * @param keyMapper
     * @param valueMapper
     * @param <E>
     * @param <FK>
     * @param <FV>
     */
    public static <E, FK, FV> void mapContent(Map<FK, FV> mapped, ObservableList<E> source,
                                              Function<E, Property<FK>> keyMapper,
                                              Function<E, FV> valueMapper) {
        mapContentWithFilter(mapped, source, keyMapper, valueMapper, s -> true);
    }

    public static <E, FK, FV> void mapContentWithFilter(Map<FK, FV> mapped, ObservableList<E> source,
                                                        Function<E, Property<FK>> keyMapper,
                                                        Function<E, FV> valueMapper,
                                                        Predicate<E> filter) {
        MapListContentBinder<E, FK, FV> contentBinder =
                new MapListContentBinder<>(mapped, keyMapper, valueMapper, filter);
        mapped.clear();
        mapped.putAll(
                source.stream()
                        .filter(filter)
                        .collect(toMap(item -> keyMapper.apply(item).getValue(),
                        valueMapper))
        );
        source.removeListener(contentBinder);
        source.addListener(contentBinder);
    }

    public static <E, F> void mapContentWithFilter(Set<F> mapped, ObservableList<E> source,
                                                   Function<E, F> mapper, Predicate<E> filter) {
        final SetListContentBinder<E, F> contentBinder =
                new SetListContentBinder<>(mapped, mapper, filter);
        mapped.clear();
        mapped.addAll(source.stream().filter(filter).map(mapper).collect(toList()));
        source.removeListener(contentBinder);
        source.addListener(contentBinder);
    }

    public static <E, F> void mapContent(Set<F> mapped, ObservableList<E> source,
                                         Function<E, F> mapper) {
        mapContentWithFilter(mapped, source, mapper, e -> true);
    }

    public static <E, F> void mapContent(ObservableList<F> mapped, ObservableSet<E> source,
                                         Function<E, F> mapper,
                                         Equaler<F> mappedEqualer) {
        final ListSetContentBinder<E, F> contentMapping =
                new ListSetContentBinder<>(mapped, mapper, mappedEqualer);
        mapped.setAll(source.stream().map(mapper).collect(toList()));
        source.removeListener(contentMapping);
        source.addListener(contentMapping);
    }

    public static <E, F> void mapContent(ObservableList<F> mapped, ObservableSet<E> source,
                                         Function<E, F> mapper) {
        mapContent(mapped, source, mapper, Equaler.natural());
    }

    public static <E, F> void mapContent(ObservableList<F> mapped, ObservableList<E> source,
                                         Function<? super E, ? extends F> mapper) {
        final ListContentBinder<E, F> contentMapping = new ListContentBinder<>(mapped, mapper);
        mapped.setAll(source.stream().map(mapper).collect(toList()));
        source.removeListener(contentMapping);
        source.addListener(contentMapping);
    }


}
