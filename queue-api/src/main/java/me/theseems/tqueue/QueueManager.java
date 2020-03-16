package me.theseems.tqueue;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public interface QueueManager {
  /**
   * Get queue by name
   *
   * @param name of queue
   * @return optional of queue
   */
  Optional<Queue> getQueue(@NotNull String name);

  /**
   * Register queue to manager
   *
   * @param queue to register
   */
  void register(Queue queue);

  /**
   * Remove queue from manager
   *
   * @param queue to unregister
   */
  void unregister(Queue queue);

  /**
   * Get all queues there are
   *
   * @return queue list
   */
  Collection<Queue> getQueues();

  /**
   * Make queue (using default in system)
   *
   * @param name of queue
   * @return queue
   */
  Queue make(@NotNull String name, int delay);
}
