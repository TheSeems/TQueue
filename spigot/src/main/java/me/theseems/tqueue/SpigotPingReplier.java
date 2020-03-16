package me.theseems.tqueue;

import java.util.UUID;

public interface SpigotPingReplier {
  /**
   * Add ping processor
   *
   * @param processor to add
   */
  void addProcessor(SpigotPingProcessor processor);

  /**
   * Remove ping processor
   *
   * @param processor to remove
   */
  void removeProcessor(SpigotPingProcessor processor);

  /**
   * Get a player through all processors and give a verdict
   *
   * @param player to get verdict for
   * @return verdict
   */
  Verdict process(UUID player);
}
