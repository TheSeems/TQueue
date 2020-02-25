package me.theseems.tqueue.config;

import me.theseems.tqueue.Queue;
import me.theseems.tqueue.QueueAPI;
import me.theseems.tqueue.ServerDestination;
import me.theseems.tqueue.Utils;

import java.util.List;
import java.util.Map;

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

    servers.forEach((s, integer) -> queue.addDestination(new ServerDestination(s, integer)));
    if (handlers.contains("bungee-default"))
      queue.addHandler(Utils.getDefaultHandlerFor(queue));
    return queue;
  }

  public String getName() {
    return name;
  }
}
