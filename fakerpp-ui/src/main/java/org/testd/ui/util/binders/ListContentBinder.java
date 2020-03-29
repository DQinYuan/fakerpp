package org.testd.ui.util.binders;

import javafx.beans.WeakListener;
import javafx.collections.ListChangeListener;

import java.lang.ref.WeakReference;
import java.util.List;

public class ListContentBinder implements ListChangeListener, WeakListener {
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
        return (list == null) ? 0 : list.hashCode();
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
