package me.theseems.tqueue.config;

import me.theseems.tqueue.Queue;
import me.theseems.tqueue.ServerDestination;
import me.theseems.tqueue.TPriorityQueue;

import java.util.Map;
import java.util.UUID;

public class BungeeQueueConfig {
  private String name;
  private Integer delay;
  private Map<String, Integer> servers;

  public BungeeQueueConfig(String name, Integer delay, Map<String, Integer> servers) {
    this.name = name;
    this.delay = delay;
    this.servers = servers;
  }

  public Queue construct() {
    TPriorityQueue queue =
        new TPriorityQueue(delay) {
          @Override
          public Integer getPriority(UUID player) {
            return 0;
          }

          @Override
          public String getName() {
            return name;
          }
        };

    servers.forEach((s, integer) -> queue.addDestination(new ServerDestination(s, integer)));
    return queue;
  }

  public String getName() {
    return name;
  }
}
