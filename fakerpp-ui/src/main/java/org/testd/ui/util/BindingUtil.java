package org.testd.ui.util;

import javafx.beans.WeakListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import org.testd.ui.util.binders.ListContentBinder;
import org.testd.ui.util.binders.ListSetContentBinder;
import org.testd.ui.util.binders.MapListContentBinder;
import org.testd.ui.util.binders.SetListContentBinder;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class BindingUtil {

    @SuppressWarnings("unchecked")
    public static void bindContentTypeUnsafe(List bind, ObservableList source) {
        final ListContentBinder contentBinding =
                new ListContentBinder(bind);
        if (bind instanceof ObservableList) {
            ((ObservableList) bind).setAll(source);
        } else {
            bind.clear();
            bind.addAll(source);
        }
        source.removeListener(contentBinding);
        source.addListener(contentBinding);
    }

    public static <E, FK, FV> void mapContentWithFilter(Map<FK, FV> mapped, ObservableList<E> source,
                                                   Function<? super E, ? extends FK> keyMapper,
                                                   Function<? super E, ? extends FV> valueMapper,
                                                   Predicate<E> filter) {
        MapListContentBinder<E, FK, FV> contentBinder =
                new MapListContentBinder<>(mapped, keyMapper, valueMapper, filter);
        mapped.clear();
        mapped.putAll(
                source.stream().collect(toMap(keyMapper, valueMapper))
        );
        source.removeListener(contentBinder);
        source.addListener(contentBinder);
    }

    public static <E, F> void mapContentWithFilter(Set<F> mapped, ObservableList<E> source,
                                                   Function<E, F> mapper, Predicate<E> filter) {
        final SetListContentBinder<E, F> contentBinder =
                new SetListContentBinder<>(mapped, mapper, filter);
        mapped.clear();
        mapped.addAll(source.stream().map(mapper).collect(toList()));
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
        final ListContentMapping<E, F> contentMapping = new ListContentMapping<E, F>(mapped, mapper);
        mapped.setAll(source.stream().map(mapper::apply).collect(toList()));
        source.removeListener(contentMapping);
        source.addListener(contentMapping);
    }



    private static class ListContentMapping<E, F> implements ListChangeListener<E>, WeakListener {
        private final WeakReference<List<F>> mappedRef;
        private final Function<? super E, ? extends F> mapper;

        public ListContentMapping(List<F> mapped, Function<? super E, ? extends F> mapper) {
            this.mappedRef = new WeakReference<List<F>>(mapped);
            this.mapper = mapper;
        }

        @Override
        public void onChanged(Change<? extends E> change) {
            final List<F> mapped = mappedRef.get();
            if (mapped == null) {
                change.getList().removeListener(this);
            } else {
                while (change.next()) {
                    if (change.wasPermutated()) {
                        mapped.subList(change.getFrom(), change.getTo()).clear();
                        mapped.addAll(change.getFrom(), change.getList().subList(change.getFrom(), change.getTo())
                                .stream().map(o -> mapper.apply(o)).collect(toList()));
                    } else {
                        if (change.wasRemoved()) {
                            mapped.subList(change.getFrom(), change.getFrom() + change.getRemovedSize()).clear();
                        }
                        if (change.wasAdded()) {
                            mapped.addAll(change.getFrom(), change.getAddedSubList()
                                    .stream().map(o -> mapper.apply(o)).collect(toList()));
                        }
                    }
                }
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return mappedRef.get() == null;
        }

        @Override
        public int hashCode() {
            final List<F> list = mappedRef.get();
            return (list == null) ? 0 : list.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            final List<F> mapped1 = mappedRef.get();
            if (mapped1 == null) {
                return false;
            }

            if (obj instanceof ListContentMapping) {
                final ListContentMapping<?, ?> other = (ListContentMapping<?, ?>) obj;
                final List<?> mapped2 = other.mappedRef.get();
                return mapped1 == mapped2;
            }
            return false;
        }
    }
}
