package org.testd.ui.util.binders;

import javafx.beans.WeakListener;
import javafx.collections.ListChangeListener;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toMap;

public class MapListContentBinder<E, FK, FV> implements ListChangeListener<E>, WeakListener {

    private final WeakReference<Map<FK, FV>> mapRef;
    private final Function<? super E, ? extends FK> keyMapper;
    private final Function<? super E, ? extends FV> valueMapper;
    private final Predicate<E> filter;

    public MapListContentBinder(Map<FK, FV> map,
                                Function<? super E, ? extends FK> keyMapper,
                                Function<? super E, ? extends FV> valueMapper,
                                Predicate<E> filter) {
        this.mapRef = new WeakReference<>(map);
        this.keyMapper = keyMapper;
        this.valueMapper = valueMapper;
        this.filter = filter;
    }

    @Override
    public void onChanged(Change<? extends E> change) {
        final Map<FK, FV> map = mapRef.get();
        if (map == null) {
            change.getList().removeListener(this);
        } else {
            while (change.next()) {
                if (!change.wasPermutated()) {
                    if (change.wasRemoved()) {
                        change.getRemoved().stream()
                                .filter(filter)
                                .map(keyMapper)
                                .forEach(map::remove);
                    }
                    if (change.wasAdded()) {
                        map.putAll(
                                change.getAddedSubList()
                                        .stream()
                                        .filter(filter)
                                        .collect(toMap(keyMapper, valueMapper))
                        );
                    }
                }
            }
        }
    }

    @Override
    public boolean wasGarbageCollected() {
        return mapRef.get() == null;
    }

    @Override
    public int hashCode() {
        final Map<FK, FV> map = mapRef.get();
        return (map == null) ? 0 : map.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        final Map<FK, FV> map1 = mapRef.get();
        if (map1 == null) {
            return false;
        }

        if (obj instanceof MapListContentBinder) {
            final MapListContentBinder<?, ?, ?> other = (MapListContentBinder<?, ?, ?>) obj;
            final Map<?, ?> map2 = other.mapRef.get();
            return map1 == map2;
        }
        return false;
    }

}
