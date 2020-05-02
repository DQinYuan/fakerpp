package org.testd.ui.util.binders;

import javafx.beans.WeakListener;
import javafx.collections.MapChangeListener;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

public class ValueListBinder<K, V> implements MapChangeListener<K, V>, WeakListener {

    private final WeakReference<List<V>> valueListRef;

    public ValueListBinder(List<V> valueList) {
        this.valueListRef = new WeakReference<>(valueList);
    }

    @Override
    public void onChanged(Change<? extends K, ? extends V> change) {
        List<V> valueList = valueListRef.get();
        if (valueList == null) {
            change.getMap().removeListener(this);
        } else {
            if (change.getValueAdded() != null) {
                valueList.add(change.getValueAdded());
            }
            if (change.getValueRemoved() != null) {
                valueList.remove(change.getValueRemoved());
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        List<V> valueList1 = valueListRef.get();
        if (valueList1 == null) {
            return false;
        }

        if (obj instanceof ValueListBinder) {
            final ValueListBinder<?, ?> other = (ValueListBinder<?, ?>) obj;
            final List<?> valueList2 = other.valueListRef.get();
            return valueList1 == valueList2;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueListRef.get());
    }

    @Override
    public boolean wasGarbageCollected() {
        return valueListRef.get() == null;
    }


}
