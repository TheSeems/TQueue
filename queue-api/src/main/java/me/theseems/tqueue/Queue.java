package me.theseems.tqueue;
import java.util.Collection;
import java.util.UUID;

public interface Queue {
  /**
   * Get name of the queue
   *
   * @return name
   */
  String getName();

  /**
   * Players there are
   *
   * @return players
   */
  Collection<UUID> getPlayers();

  /**
   * Remove player from the queue
   *
   * @param player to remove
   */
  void remove(UUID player);

  /**
   * Add player to the queue
   *
   * @param player to add
   */
  void add(UUID player);

  // Mapped containers for priorities, handlers and destinations
  QueueMappedContainer<UUID, Integer> getPriorities();

  void setPriorities(QueueMappedContainer<UUID, Integer> priorities);

  QueueMappedContainer<String, QueueHandler> getHandlers();

  void setHandlers(QueueMappedContainer<String, QueueHandler> handlers);

  QueueMappedContainer<String, Destination> getDestinations();

  void setDestinations(QueueMappedContainer<String, Destination> destinations);

  /**
   * Get position of player in queue
   *
   * @param player to get position for
   * @return position
   */
  int getPosition(UUID player);

  /**
   * Get delay of queue
   *
   * @return delay of actions
   */
  int getDelay();

  /**
   * Set delay of a queue
   *
   * @param millis milliseconds
   */
  void setDelay(int millis);

  /** Close queue */
  void close();
}
