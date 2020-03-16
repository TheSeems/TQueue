package me.theseems.tqueue;

import java.util.UUID;

public interface QueueHandler {
  /**
   * Apply verdict
   *
   * @param player      who received
   * @param destination that has given verdict
   * @param verdict     target
   * @return True if there is no need to proceed with a player to other handlers otherwise false
   */
  default boolean onApply(UUID player, Destination destination, Verdict verdict) {
    return false;
  }

  /**
   * On join handler
   *
   * @param player joined
   */
  default void onJoin(UUID player) {
  }

  /**
   * On leave handler
   *
   * @param player left
   */
  default void onLeave(UUID player) {
  }

  /**
   * Get name of handler
   *
   * @return name
   */
  String getName();

}
