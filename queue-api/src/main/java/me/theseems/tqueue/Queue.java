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

  /**
   * Get priorities container
   *
   * @return priorities
   */
  QueueMappedContainer<UUID, Integer> getPriorities();

  /**
   * Set priority container
   *
   * @param priorities to set
   */
  void setPriorities(QueueMappedContainer<UUID, Integer> priorities);

  /**
   * Get handlers container
   *
   * @return handlers container
   */
  QueueMappedContainer<String, QueueHandler> getHandlers();

  /**
   * Set handlers container
   *
   * @param handlers to set
   */
  void setHandlers(QueueMappedContainer<String, QueueHandler> handlers);

  /**
   * Get destinations container
   *
   * @return destinations container
   */
  QueueMappedContainer<String, Destination> getDestinations();

  /**
   * Set destinations container
   *
   * @param destinations to set
   */
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

  /**
   * Close queue
   */
  void close();
}
