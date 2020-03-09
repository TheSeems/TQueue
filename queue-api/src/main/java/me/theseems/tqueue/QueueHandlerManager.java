package me.theseems.tqueue;

import java.util.Collection;
import java.util.Optional;

public interface QueueHandlerManager {
  /**
   * Request handler for a queue
   *
   * @param queue to request for
   * @param name  of handler to request
   * @return optional of handler
   */
  Optional<QueueHandler> requestFor(String name, Queue queue);

  /**
   * Register handler by a name
   *
   * @param factory to register
   */
  void register(QueueHandlerFactory factory);

  /**
   * Unregister handler from a manager
   *
   * @param name to unregister
   */
  void unregister(String name);

  /**
   * Get all the handler factories there are
   *
   * @return factories
   */
  Collection<String> getFactories();
}
