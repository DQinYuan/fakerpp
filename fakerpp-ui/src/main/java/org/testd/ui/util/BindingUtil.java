package org.testd.ui.util;

import com.sun.javafx.binding.ContentBinding;
import javafx.beans.WeakListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

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

    public static <E, F> void mapContent(ObservableList<F> mapped, ObservableSet<E> source,
                                         Function<E, F> mapper,
                                         Equaler<F> mappedEqualer) {
        final ListSetContentMapping<E, F> contentMapping =
                new ListSetContentMapping<E, F>(mapped, mapper, mappedEqualer);
        mapped.setAll(source.stream().map(mapper::apply).collect(toList()));
        source.removeListener(contentMapping);
        source.addListener(contentMapping);
    }

    public static <E, F> void mapContent(ObservableList<F> mapped, ObservableSet<E> source,
                                         Function<E, F> mapper) {
        mapContent(mapped, source, mapper, Equaler.natural());
    }

    public static <E, F> void mapContent(ObservableList<F> mapped, ObservableList< E> source,
                                         Function<? super E, ? extends F> mapper) {
        final ListContentMapping<E, F> contentMapping = new ListContentMapping<E, F>(mapped, mapper);
        mapped.setAll(source.stream().map(mapper::apply).collect(toList()));
        source.removeListener(contentMapping);
        source.addListener(contentMapping);
    }

    private static class ListContentBinder implements ListChangeListener, WeakListener {
        private final WeakReference<List> listRef;

        public ListContentBinder(List list) {
            this.listRef = new WeakReference<>(list);
        }

        @Override
        public void onChanged(Change change) {
            final List list = listRef.get();
            if (list == null) {
                change.getList().removeListener(this);
            } else {
                while (change.next()) {
                    if (change.wasPermutated()) {
                        list.subList(change.getFrom(), change.getTo()).clear();
                        list.addAll(change.getFrom(), change.getList().subList(change.getFrom(), change.getTo()));
                    } else {
                        if (change.wasRemoved()) {
                            list.subList(change.getFrom(), change.getFrom() + change.getRemovedSize()).clear();
                        }
                        if (change.wasAdded()) {
                            list.addAll(change.getFrom(), change.getAddedSubList());
                        }
                    }
                }
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return listRef.get() == null;
        }

        @Override
        public int hashCode() {
            final List list = listRef.get();
            return (list == null)? 0 : list.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            final List list1 = listRef.get();
            if (list1 == null) {
                return false;
            }

            if (obj instanceof ListContentBinder) {
                final ListContentBinder other = (ListContentBinder) obj;
                final List list2 = other.listRef.get();
                return list1 == list2;
            }
            return false;
        }
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

    private static class ListSetContentMapping<E, F> implements SetChangeListener<E>, WeakListener {

        private final WeakReference<List<F>> mappedRef;
        private final Function<? super E, ? extends F> mapper;
        private final Equaler<F> equaler;

        private ListSetContentMapping(List<F> mappedRef,
                                      Function<? super E, ? extends F> mapper,
                                      Equaler<F> equaler) {
            this.mappedRef = new WeakReference<>(mappedRef);
            this.mapper = mapper;
            this.equaler = equaler;
        }

        @Override
        public boolean wasGarbageCollected() {
            return mappedRef.get() == null;
        }

        @Override
        public void onChanged(Change<? extends E> change) {
            final List<F> mapped = mappedRef.get();
            if (mapped == null) {
                change.getSet().removeListener(this);
            } else if (change.wasAdded()) {
                mapped.add(mapper
                        .apply(change.getElementAdded()));
            } else if (change.wasRemoved()) {
                Optional<F> needRemoved = mapped.stream()
                        .filter(e -> equaler.equal(e,
                                mapper.apply(change.getElementRemoved())))
                        .findFirst();
                needRemoved.map(mapped::remove);
            }
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
