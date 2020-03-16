package me.theseems.tqueue;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class TQueueManager implements QueueManager {
  private Map<String, Queue> queueMap;

  public TQueueManager() {
    queueMap = new ConcurrentHashMap<>();
  }

  @Override
  public Optional<Queue> getQueue(@NotNull String name) {
    return Optional.ofNullable(queueMap.get(name));
  }

  @Override
  public void register(Queue queue) {
    if (queueMap.containsKey(queue.getName())) {
      throw new IllegalStateException(
        "An attempt to register queue with existing in system name: "
          + "new="
          + queue
          + ", old="
          + queueMap.get(queue.getName()));
    }

    System.out.println("Registered queue with name " + queue.getName());
    queueMap.put(queue.getName(), queue);
  }

  @Override
  public void unregister(Queue queue) {
    queue.close();
    queueMap.remove(queue.getName());
  }

  @Override
  public Collection<Queue> getQueues() {
    return queueMap.values();
  }
}
