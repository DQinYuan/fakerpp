package org.testd.ui.util.binders;

import javafx.beans.WeakListener;
import javafx.collections.MapChangeListener;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class MapContentBinder<EK, EV, FK, FV> implements MapChangeListener<EK, EV>, WeakListener {

    private final WeakReference<Map<FK, FV>> mapRef;
    private final Function<EK, FK> keyMapper;
    private final Function<EV, FV> valueMapper;

    public MapContentBinder(Map<FK, FV> map,
                            Function<EK, FK> keyMapper,
                            Function<EV, FV> valueMapper) {
        this.mapRef = new WeakReference<>(map);
        this.keyMapper = keyMapper;
        this.valueMapper = valueMapper;
    }

    @Override
    public void onChanged(Change<? extends EK, ? extends EV> change) {
        final Map<FK, FV> map = mapRef.get();
        if (map == null) {
            change.getMap().removeListener(this);
        } else if (change.wasAdded()) {
            map.put(keyMapper.apply(change.getKey()),
                    valueMapper.apply(change.getValueAdded()));
        } else if (change.wasRemoved()) {
            map.remove(keyMapper.apply(change.getKey()));
        }
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

        if (obj instanceof MapContentBinder) {
            final MapContentBinder<?, ?, ?, ?> other = (MapContentBinder<?, ?, ?, ?>) obj;
            final Map<?, ?> map2 = other.mapRef.get();
            return map1 == map2;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(mapRef.get());
    }


    @Override
    public boolean wasGarbageCollected() {
        return mapRef.get() == null;
    }


}
