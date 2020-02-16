package me.theseems.tqueue;

import java.util.Optional;
import java.util.UUID;

public interface SpigotPingProcessor {
  Optional<Verdict> hookup(UUID player);
}
