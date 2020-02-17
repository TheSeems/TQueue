package me.theseems.tqueue;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

public abstract class TPriorityQueue implements PriorityQueue {

  class TQueueItemComparator implements Comparator<UUID> {
    @Override
    public int compare(UUID uuid, UUID t1) {
      if (uuid == t1) return uuid.compareTo(t1);
      return Integer.compare(getPriority(t1), getPriority(uuid));
    }
  }

  static class TQueueDestinationComparator implements Comparator<Destination> {
    @Override
    public int compare(Destination destination, Destination t1) {
      if (!destination.getName().equals(t1.getName()))
        return Integer.compare(t1.getPriority(), destination.getPriority());
      return destination.getName().compareTo(t1.getName());
    }
  }

  private PriorityBlockingQueue<UUID> queue;
  private PriorityBlockingQueue<Destination> destinationList;
  private Map<String, QueueHandler> handlerList;
  private boolean isClosed;
  private int delay;

  @Override
  public void removeHandler(String name) {
    handlerList.remove(name);
  }

  public Collection<String> getHandlers() {
    return handlerList.keySet();
  }

  public TPriorityQueue(int delay) {
    this.queue = new PriorityBlockingQueue<>(1, new TQueueItemComparator());
    this.destinationList = new PriorityBlockingQueue<>(1, new TQueueDestinationComparator());
    this.handlerList = new ConcurrentHashMap<>();
    this.isClosed = false;
    this.delay = delay;

    Runnable runnable =
        () -> {
          Date latest = new Date();
          while (true) {
            if (ChronoUnit.MILLIS.between(latest.toInstant(), Calendar.getInstance().toInstant())
                < delay) continue;

            if (isClosed) {
              destinationList.clear();
              handlerList.clear();
              queue.clear();
              System.out.println("Queue '" + getName() + "' shutdown");
              break;
            } else if (!queue.isEmpty()) {
              try {
                System.out.println(">> giant step <<");
                UUID uuid = queue.take();
                go(uuid);
                System.out.println("After: (" + queue.size() + ")");
              } catch (Exception e) {
                System.out.println(
                    "Execution exception in queue '" + getName() + "': " + e.getMessage());
              }
            }

            latest = new Date();
          }
        };

    QueueAPI.getService().submit(runnable);
  }

  @Override
  public int getPosition(UUID player) {
    int pos = 0;
    for (UUID uuid : queue) {
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
  }

  public void add(UUID player) {
    if (!queue.contains(player)) queue.add(player);
  }

  public void go(UUID uuid) {
    boolean next = false;

    for (Destination destination : destinationList) {
      Future<Verdict> waiting = destination.query(uuid);
      Verdict verdict;
      try {
        verdict = waiting.get(1500, TimeUnit.MILLISECONDS);
      } catch (TimeoutException e) {
        System.out.println(
            "Timed out: " + e.getMessage() + " @" + destination.getName() + " @" + getName());
        verdict = Verdict.TIMED_OUT;
      } catch (Exception e) {
        System.out.println(
            "Error getting verdict in queue '"
                + getName()
                + "' for "
                + uuid
                + " @"
                + destination.toString()
                + " ("
                + destination.getName()
                + ")");
        e.printStackTrace();
        continue;
      }

      System.out.println("Verdict: " + verdict);

      for (QueueHandler queueHandler : handlerList.values()) {
        try {
          next = queueHandler.apply(uuid, destination, verdict);
        } catch (Exception e) {
          System.out.println(
              "Error applying verdict for "
                  + uuid
                  + " and "
                  + destination
                  + " with "
                  + verdict
                  + ": "
                  + e.getMessage());
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
}
