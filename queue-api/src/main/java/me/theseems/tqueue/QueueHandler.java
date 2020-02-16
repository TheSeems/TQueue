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
  boolean apply(UUID player, Destination destination, Verdict verdict);

  /**
   * Get name of handler
   * @return name
   */
  String getName();
}
