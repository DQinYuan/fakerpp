package org.testd.ui.util.binders;

import javafx.beans.WeakListener;
import javafx.collections.SetChangeListener;
import org.testd.ui.util.Equaler;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * list bind to set
 * @param <E>
 * @param <F>
 */
public class ListSetContentBinder<E, F> implements SetChangeListener<E>, WeakListener {

    private final WeakReference<List<F>> mappedRef;
    private final Function<? super E, ? extends F> mapper;
    private final Equaler<F> equaler;

    public ListSetContentBinder(List<F> mappedRef,
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

        if (obj instanceof ListSetContentBinder) {
            final ListSetContentBinder<?, ?> other = (ListSetContentBinder<?, ?>) obj;
            final List<?> mapped2 = other.mappedRef.get();
            return mapped1 == mapped2;
        }
        return false;
    }
}
