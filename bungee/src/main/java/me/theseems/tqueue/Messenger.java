package me.theseems.tqueue;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Messenger {
  /**
   * Request a status of player in the server
   * @param name of server
   * @param player to check for
   * @return verdict
   */
  CompletableFuture<Verdict> requestUser(String name, UUID player);
}
