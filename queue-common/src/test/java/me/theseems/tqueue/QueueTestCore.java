package me.theseems.tqueue;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class QueueTestCore {
  public static Map<UUID, Integer> priority;

  public static void setup() {
    priority = new TreeMap<>();
    new QueueAPI(5);
    QueueAPI.setQueueManager(
      new TQueueManager() {
        @Override
        public Queue make(String name, int delay) {
          return new TPriorityQueue(delay) {
            @Override
            public Integer getPriority(UUID player) {
              return priority.getOrDefault(player, -1);
            }

            @Override
            public void add(UUID player) {
              super.add(player);
            }

            @Override
            public String getName() {
              return "test-" + name;
            }
          };
        }
      });
  }
}
