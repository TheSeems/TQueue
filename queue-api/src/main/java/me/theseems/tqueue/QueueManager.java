package me.theseems.tqueue;

import java.util.Collection;
import java.util.Optional;

public interface QueueManager {
  /**
   * Get queue by name
   * @param name of queue
   * @return optional of queue
   */
  Optional<Queue> getQueue(String name);

  /**
   * Register queue to manager
   * @param queue to register
   */
  void register(Queue queue);

  /**
   * Remove queue from manager
   * @param queue to unregister
   */
  void unregister(Queue queue);

  /**
   * Get all queues there are
   * @return queue list
   */
  Collection<Queue> getQueues();
}
