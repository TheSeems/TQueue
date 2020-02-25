package me.theseems.tqueue;

import java.util.Collection;
import java.util.UUID;

public interface QueueCommunicator {
  // Perform a request to join
  void join(UUID player, String name);

  // Perform a request to leave
  void leave(UUID player, String name);

  // Get players in queue
  Collection<UUID> getPlayers(String name);

  // Shutdown the communicator
  void shutdown();
}
