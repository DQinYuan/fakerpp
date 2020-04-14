package org.testd.ui.util.binders;


import javafx.beans.WeakListener;
import javafx.collections.ListChangeListener;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public class ListContentBinder<E, F> implements ListChangeListener<E>, WeakListener {
    private final WeakReference<List<F>> mappedRef;
    private final Function<? super E, ? extends F> mapper;

    public ListContentBinder(List<F> mapped, Function<? super E, ? extends F> mapper) {
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

        if (obj instanceof ListContentBinder) {
            final ListContentBinder<?, ?> other = (ListContentBinder<?, ?>) obj;
            final List<?> mapped2 = other.mappedRef.get();
            return mapped1 == mapped2;
        }
        return false;
    }
}
