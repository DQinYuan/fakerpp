package org.testd.ui.util.binders;

import javafx.beans.WeakListener;
import javafx.collections.ListChangeListener;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class SetListContentBinder<E, F> implements ListChangeListener<E>, WeakListener {
    private final WeakReference<Set<F>> setRef;
    private final Function<? super E, ? extends F> mapper;
    private final Predicate<E> filter;

    public SetListContentBinder(Set<F> set,
                                Function<? super E, ? extends F> mapper,
                                Predicate<E> filter) {
        this.setRef = new WeakReference<>(set);
        this.mapper = mapper;
        this.filter = filter;
    }

    @Override
    public void onChanged(Change<? extends E> change) {
        final Set<F> set = setRef.get();
        if (set == null) {
            change.getList().removeListener(this);
        } else {
            while (change.next()) {
                if (!change.wasPermutated()) {
                    if (change.wasRemoved()) {
                        set.removeAll(change.getRemoved().stream()
                                .filter(filter)
                                .map(mapper).collect(toList())
                        );
                    }
                    if (change.wasAdded()) {
                        set.addAll(change.getAddedSubList()
                                .stream()
                                .filter(filter)
                                .map(mapper).collect(toList()));
                    }
                }
            }
        }
    }

    @Override
    public boolean wasGarbageCollected() {
        return setRef.get() == null;
    }

    @Override
    public int hashCode() {
        final Set<F> list = setRef.get();
        return (list == null) ? 0 : list.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        final Set<F> list1 = setRef.get();
        if (list1 == null) {
            return false;
        }

        if (obj instanceof SetListContentBinder) {
            final SetListContentBinder<?, ?> other = (SetListContentBinder<?, ?>) obj;
            final Set<?> list2 = other.setRef.get();
            return list1 == list2;
        }
        return false;
    }
}
