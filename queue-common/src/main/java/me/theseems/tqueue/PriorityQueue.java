package me.theseems.tqueue;

import java.util.UUID;

public interface PriorityQueue extends Queue {
  /**
   * Get priority of a player
   *
   * @param player to get priority of
   * @return priority
   */
  Integer getPriority(UUID player);
}
