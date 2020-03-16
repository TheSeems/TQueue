package me.theseems.tqueue;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public abstract class PriorityMappedContainer<N, T> implements QueueMappedContainer<N, T> {
    private Map<NamedEntry, T> map;

    public PriorityMappedContainer(Comparator<NamedEntry> comparator) {
        map = new TreeMap<>(comparator);
    }

    public PriorityMappedContainer() {
        Comparator<NamedEntry> comparator =
          (namedEntry, t1) -> {
              if (namedEntry.name.equals(t1.name)) return 0;

              int priorityFirst = getPriority(namedEntry.name, namedEntry.value);
              int prioritySecond = getPriority(namedEntry.name, namedEntry.value);
              if (priorityFirst != prioritySecond) {
                  return Integer.compare(prioritySecond, priorityFirst);
              }
              return 1;
          };
        map = new TreeMap<>(comparator);
    }

    @Override
    public Collection<T> values() {
        return map.values();
    }

    @Override
    public Collection<N> keys() {
        return map.keySet().stream().map(namedEntry -> namedEntry.name).collect(Collectors.toSet());
    }

    @Override
    public void add(N name, T value) {
        map.put(new NamedEntry(name, value), value);
    }

    @Override
    public void remove(N name) {
        map.remove(new NamedEntry(name, null));
    }

    @Override
    public Optional<T> get(N name) {
        return Optional.ofNullable(map.get(new NamedEntry(name, null)));
    }

    @Override
    public void clear() {
        map.clear();
    }

    public abstract int getPriority(@NotNull N name, @NotNull T value);

    private class NamedEntry {
        private N name;
        private T value;

        public NamedEntry(N name, T value) {
            this.name = name;
            this.value = value;
        }
    }
}
