package org.testd.ui.util.binders;

import javafx.beans.WeakListener;
import javafx.collections.MapChangeListener;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Set;

public class KeyValueSetBinder<K, V> implements MapChangeListener<K, V>, WeakListener {

    private final WeakReference<Set<K>> keyRef;
    private final WeakReference<Set<V>> valueRef;

    public KeyValueSetBinder(Set<K> key, Set<V> value) {
        this.keyRef = new WeakReference<>(key);
        this.valueRef = new WeakReference<>(value);
    }

    @Override
    public void onChanged(Change<? extends K, ? extends V> change) {
        final Set<K> keyContainer = keyRef.get();
        final Set<V> valueContainer = valueRef.get();
        if (keyContainer == null || valueContainer == null) {
            change.getMap().removeListener(this);
        } else if (change.wasAdded()) {
            keyContainer.add(change.getKey());
            if (change.getValueRemoved() != null) {
                valueContainer.remove(change.getValueRemoved());
            }
            valueContainer.add(change.getValueAdded());
        } else if (change.wasRemoved()) {
            keyContainer.remove(change.getKey());
            valueContainer.remove(change.getValueRemoved());
        }
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        final Set<K> key1 = keyRef.get();
        final Set<V> value1 = valueRef.get();
        if (key1 == null || value1 == null) {
            return false;
        }

        if (obj instanceof KeyValueSetBinder) {
            final KeyValueSetBinder<?, ?> other = (KeyValueSetBinder<?, ?>) obj;
            final Set<?> key2 = other.keyRef.get();
            final Set<?> value2 = other.valueRef.get();
            return key1 == key2 && value1 == value2;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyRef.get(), valueRef.get());
    }

    @Override
    public boolean wasGarbageCollected() {
        return keyRef.get() == null || valueRef.get() == null;
    }
}
