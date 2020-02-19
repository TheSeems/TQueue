package me.theseems.tqueue;

import java.util.UUID;

public interface QueueHandler {
  /**
   * Apply verdict
   *
   * @param player who received
   * @param destination that has given verdict
   * @param verdict target
   */
  default boolean apply(UUID player, Destination destination, Verdict verdict) {
    return false;
  }

  /**
   * On join handler
   * @param player joined
   */
  default void join(UUID player) {}

  /**
   * On leave handler
   * @param player left
   */
  default void leave(UUID player) {}

  /**
   * Get name of handler
   *
   * @return name
   */
  String getName();
}
