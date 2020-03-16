package me.theseems.tqueue;

import java.util.Optional;
import java.util.UUID;

public interface SpigotPingProcessor {
  /**
   * Hookup a player and produce a verdict if it can be done
   *
   * @param player to hookup
   * @return verdict if can be given
   */
  Optional<Verdict> hookup(UUID player);
}
