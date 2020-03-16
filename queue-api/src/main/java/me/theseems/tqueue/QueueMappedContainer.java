package me.theseems.tqueue;

import java.util.Collection;
import java.util.Optional;

public interface QueueMappedContainer<N, T> {
  /**
   * All values there are
   *
   * @return values
   */
  Collection<T> values();

  /**
   * All keys there are
   *
   * @return keys
   */
  Collection<N> keys();

  /**
   * Add a value (assuming that we can get a key from it)
   *
   * @param value to add
   */
  void add(T value);

  /**
   * Add entry
   *
   * @param name  key
   * @param value value
   */
  void add(N name, T value);

  /**
   * Remove key
   *
   * @param name to remove
   */
  void remove(N name);

  /**
   * Get optional of value by a key
   *
   * @param name to get a value from
   * @return optional of value
   */
  Optional<T> get(N name);

  /**
   * Clear container
   */
  void clear();
}
