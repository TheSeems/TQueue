package me.theseems.tqueue.config;

import me.theseems.tqueue.Queue;
import me.theseems.tqueue.QueueAPI;
import me.theseems.tqueue.QueueHandler;
import me.theseems.tqueue.ServerDestination;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BungeeQueueConfig {
  private String name;
  private Integer delay;
  private Map<String, Integer> servers;
  private List<String> handlers;

  public BungeeQueueConfig(
      String name, Integer delay, Map<String, Integer> servers, List<String> handlers) {
    this.name = name;
    this.delay = delay;
    this.servers = servers;
    this.handlers = handlers;
  }

  public Queue construct() {
    Queue queue = QueueAPI.getQueueManager().make(name, delay);
    queue.setDelay(delay);

    servers.forEach(
            (server, priority) -> queue.getDestinations().add(new ServerDestination(server, priority)));
    for (String handler : handlers) {
      Optional<QueueHandler> handlerOptional = QueueAPI.getHandlerManager().requestFor(handler, queue);
      if (!handlerOptional.isPresent()) {
        QueueAPI.logs().prefix("Config").warning("Unknown handler present for queue '" + name + "': " + handler + ". Skipping...");
        continue;
      }

      queue.getHandlers().add(handlerOptional.get());
    }
    return queue;
  }

  public String getName() {
    return name;
  }
}
