package me.theseems.tqueue;

import java.util.UUID;

public interface SpigotPingReplier {
  void addProcessor(SpigotPingProcessor processor);
  void removeProcessor(SpigotPingProcessor processor);

  Verdict process(UUID player);
}
