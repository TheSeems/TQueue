package me.theseems.tqueue;

import java.util.Collection;
import java.util.Optional;

public interface QueueMappedContainer<N, T> {
    Collection<T> all();

    Collection<N> keys();

    void add(T value);

    void add(N name, T value);

    void remove(N name);

    Optional<T> get(N name);

    void clear();
}
