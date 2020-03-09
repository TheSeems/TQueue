package me.theseems.tqueue;

import com.spikhalskiy.futurity.Futurity;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public abstract class TPriorityQueue implements Queue {
    ConcurrentSkipListSet<UUID> queue;
    boolean isClosed;
    Logger logger;
    int delay;

    private QueueMappedContainer<UUID, Integer> priorities;
    private QueueMappedContainer<String, QueueHandler> handlers;
    private QueueMappedContainer<String, Destination> destinations;

    public TPriorityQueue(int delay, Logger logger) {
        this.queue = new ConcurrentSkipListSet<>(new TQueuePlayerComparator());
        this.isClosed = false;
        this.delay = delay;
        this.logger = logger;

        Runnable runnable =
                () -> {
                    Date latest = new Date();
          while (true) {
            if (ChronoUnit.MILLIS.between(latest.toInstant(), new Date().toInstant()) < delay) {
              continue;
            }

            if (isClosed) {
                logger.info("Queue '" + getName() + "' shutting down...");
                getDestinations().clear();
                getPriorities().clear();
                getHandlers().clear();
                queue.clear();
                logger.info("Queue '" + getName() + "' shut down...");
                break;
            } else if (!queue.isEmpty()) {
              try {
                ConcurrentSkipListSet<UUID> clone = queue.clone();
                for (UUID uuid : clone) {
                  go(uuid);
                }
              } catch (Exception e) {
                logger.info("Execution exception in queue '" + getName() + "': " + e.getMessage());
              }
            }

              latest = new Date();
          }
                };

        QueueAPI.getService().submit(runnable);
    }

    @Override
    public QueueMappedContainer<UUID, Integer> getPriorities() {
        return priorities;
    }

    @Override
    public void setPriorities(QueueMappedContainer<UUID, Integer> priorities) {
        this.priorities = priorities;
    }

    @Override
    public QueueMappedContainer<String, QueueHandler> getHandlers() {
        return handlers;
    }

    @Override
    public void setHandlers(QueueMappedContainer<String, QueueHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public QueueMappedContainer<String, Destination> getDestinations() {
        return destinations;
    }

    @Override
    public void setDestinations(QueueMappedContainer<String, Destination> destinations) {
        this.destinations = destinations;
    }

    public void remove(UUID player) {
        if (queue.contains(player)) getHandlers().all().forEach(handler -> handler.onLeave(player));
        queue.remove(player);
    }

    public TPriorityQueue(int delay) {
        this(delay, Logger.getLogger("TPriorityQueue"));
    }

    public TPriorityQueue() {
    }

    @Override
    public int getPosition(UUID player) {
        int pos = 0;
    for (UUID uuid : getPlayers()) {
      if (uuid == player) {
        return pos + 1;
      }
      pos++;
    }
    return 0;
  }

  public Collection<UUID> getPlayers() {
    return new ArrayList<>(queue);
  }

  public void add(UUID player) {
      if (!queue.contains(player)) getHandlers().all().forEach(handler -> handler.onJoin(player));
    queue.add(player);
  }

  public void go(UUID uuid) {
      for (Destination destination : getDestinations().all()) {
          if (!getPlayers().contains(uuid)) break;

          try {
              Futurity.shift(destination.query(uuid))
                      .orTimeout(1000, TimeUnit.MILLISECONDS)
                      .whenComplete(
                              (verdict, throwable) -> {
                                  if (!getPlayers().contains(uuid)) return;

                                  if (throwable != null) {
                                      if (throwable instanceof TimeoutException) {
                                          verdict = Verdict.TIMED_OUT;
                                          verdict.setDesc("A request has timed out");
                                      } else {
                                          verdict = Verdict.UNKNOWN;
                                          verdict.setDesc("Internal error: " + throwable.getMessage());
                                      }
                                  }

                                  for (QueueHandler queueHandler : getHandlers().all()) {
                                      try {
                                          queueHandler.onApply(uuid, destination, verdict);
                                      } catch (Exception e) {
                                          logger.severe(
                                                  "Error applying verdict for "
                                                          + uuid
                                                          + " and "
                                                          + destination
                                                          + " with "
                                                          + verdict
                                                          + ": "
                                                          + e.getMessage());
                                          e.printStackTrace();
                                      }
                                  }
                              });
          } catch (Exception e) {
              e.printStackTrace();
          }
      }
  }

    class TQueuePlayerComparator implements Comparator<UUID> {
        @Override
        public int compare(UUID uuid, UUID t1) {
            int priorityFirst = priorities.get(uuid).orElse(0);
            int prioritySecond = priorities.get(t1).orElse(0);

            if (priorityFirst == prioritySecond) return uuid.compareTo(t1);

            // Higher priority goes higher
            return Integer.compare(prioritySecond, priorityFirst);
        }
    }

    @Override
    public void close() {
        isClosed = true;
    }

    @Override
    public int getDelay() {
        return delay;
  }

  @Override
  public void setDelay(int delay) {
    this.delay = delay;
  }
}
