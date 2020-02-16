package me.theseems.tqueue;

import java.util.Collection;
import java.util.UUID;

public interface Queue {
  /**
   * Get name of the queue
   * @return name
   */
  String getName();

  /**
   * Players there are
   * @return players
   */
  Collection<UUID> getPlayers();

  /**
   * Remove player from the queue
   * @param player to remove
   */
  void remove(UUID player);

  /**
   * Add player to the queue
   * @param player to add
   */
  void add(UUID player);

  /**
   * Get all destinations of queue
   * @return destinations
   */
  Collection<Destination> getDestinations();

  /**
   * Add destination
   * @param destination to add
   */
  void addDestination(Destination destination);

  /**
   * Remove destination
   * @param destination to remove
   */
  void removeDestination(Destination destination);

  /**
   * Handle certain verdict for the player
   * @param handler target
   */
  void addHandler(QueueHandler handler);

  /**
   * Remove handler by name
   * @param name to remove
   */
  void removeHandler(String name);

  /**
   * Get list of names of handlers
   * @return handlers
   */
  Collection<String> getHandlers();

  /**
   * Get position of player in queue
   * @param player to get position for
   * @return position
   */
  int getPosition(UUID player);

  /**
   * Close queue
   */
  void close();
}
