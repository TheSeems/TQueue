package me.theseems.tqueue;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SimpleMappedContainer<N, T> implements QueueMappedContainer<N, T> {
    Map<N, T> map;

    public SimpleMappedContainer() {
        map = new ConcurrentHashMap<>();
    }

    @Override
    public Collection<T> all() {
        return map.values();
    }

    @Override
    public Collection<N> keys() {
        return map.keySet();
    }

    @Override
    public void add(N name, T value) {
        map.put(name, value);
    }

    @Override
    public void add(T value) {
        throw new UnsupportedOperationException("Adding just by a value is not supported");
    }

    @Override
    public void remove(N name) {
        map.remove(name);
    }

    @Override
    public Optional<T> get(N name) {
        return Optional.ofNullable(map.get(name));
    }

    @Override
    public void clear() {
        map.clear();
    }
}
