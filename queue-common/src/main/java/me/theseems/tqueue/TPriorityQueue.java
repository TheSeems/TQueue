package me.theseems.tqueue;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

public abstract class TPriorityQueue implements PriorityQueue {
  ConcurrentSkipListSet<UUID> queue;
  PriorityBlockingQueue<Destination> destinationList;
  Map<String, QueueHandler> handlerList;
  boolean isClosed;
  Logger logger;
  int delay;

  class TQueuePlayerComparator implements Comparator<UUID> {
    @Override
    public int compare(UUID uuid, UUID t1) {
      int priority_first = getPriority(uuid);
      int priority_second = getPriority(t1);

      if (priority_first == priority_second) return uuid.compareTo(t1);

      // Bigger priority goes higher
      return Integer.compare(priority_second, priority_first);
    }
  }

  static class TQueueDestinationComparator implements Comparator<Destination> {
    @Override
    public int compare(Destination first, Destination second) {
      String nameFirst = first.getName();
      String nameSecond = second.getName();

      if (!nameFirst.equals(nameSecond)) return nameFirst.compareTo(nameSecond);

      // Bigger priority goes higher
      return Integer.compare(second.getPriority(), first.getPriority());
    }
  }

  @Override
  public void removeHandler(String name) {
    handlerList.remove(name);
  }

  public Collection<String> getHandlers() {
    return handlerList.keySet();
  }

  public TPriorityQueue(int delay, Logger logger) {
    this.queue = new ConcurrentSkipListSet<>(new TQueuePlayerComparator());
    this.destinationList = new PriorityBlockingQueue<>(1, new TQueueDestinationComparator());
    this.handlerList = new ConcurrentHashMap<>();
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
              destinationList.clear();
              handlerList.clear();
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

  public TPriorityQueue(int delay) {
    this(delay, Logger.getLogger("TPriorityQueue"));
  }

  public TPriorityQueue() {}

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

  public void remove(UUID player) {
    queue.remove(player);
    for (QueueHandler value : handlerList.values()) {
      value.leave(player);
    }
  }

  public void add(UUID player) {
    queue.add(player);
    for (QueueHandler value : handlerList.values()) {
      value.join(player);
    }
  }

  public void go(UUID uuid) {
    boolean next = false;

    for (Destination destination : destinationList) {
      Verdict verdict;
      try {
        Future<Verdict> waiting = destination.query(uuid);
        verdict = waiting.get(1000, TimeUnit.MILLISECONDS);
      } catch (TimeoutException e) {
        verdict = Verdict.TIMED_OUT;
      } catch (Exception e) {
        e.printStackTrace();
        continue;
      }

      for (QueueHandler queueHandler : handlerList.values()) {
        try {
          next = queueHandler.apply(uuid, destination, verdict);
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
          continue;
        }

        if (next) {
          break;
        }
      }

      if (next) break;
    }

  }

  public Collection<Destination> getDestinations() {
    return destinationList;
  }

  @Override
  public void addDestination(Destination destination) {
    destinationList.add(destination);
  }

  @Override
  public void removeDestination(Destination destination) {
    destinationList.remove(destination);
  }

  @Override
  public void addHandler(QueueHandler handler) {
    handlerList.put(handler.getName(), handler);
  }

  @Override
  public void close() {
    isClosed = true;
  }

  public int getDelay() {
    return delay;
  }

  @Override
  public String getName() {
    return "<UNKNOWN>";
  }
}
