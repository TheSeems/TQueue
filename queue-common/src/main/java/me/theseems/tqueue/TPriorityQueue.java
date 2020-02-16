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
              System.out.println("Queue '" + getName() + "' shutdown");
              destinationList.clear();
              handlerList.clear();
              queue.clear();
              break;
            } else if (!queue.isEmpty()) {
              try {
                System.out.println(">> giant step <<");
                for (UUID uuid : queue) {
                  System.out.println("> baby step <");
                  go(uuid);
                }
              } catch (Exception e) {
                System.out.println("Execution exception in queue '" + getName() + "'");
                e.printStackTrace();
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
    return -1;
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
    boolean shouldAdd = false;
    int testi = 0;
    for (Destination destination : destinationList) {
      System.out.println(
          "Testing "
              + uuid
              + " on "
              + destination.toString()
              + " ("
              + destination.getName()
              + ") @"
              + testi++);
      Future<Verdict> waiting = destination.query(uuid);
      Verdict verdict;
      try {
        verdict = waiting.get(3, TimeUnit.SECONDS);
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

      for (QueueHandler queueHandler : handlerList.values()) {
        boolean applyVerdict;
        try {
          applyVerdict = queueHandler.apply(uuid, destination, verdict);
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
          shouldAdd = true;
          continue;
        }

        System.out.println("Handler '" + queueHandler.getName() + "' responded " + applyVerdict);
        if (applyVerdict) {
          System.out.println("Handler feels fine for " + uuid);
          shouldAdd = false;
          break;
        } else {
          System.out.println("Handlers allows us to go on");
          shouldAdd = true;
        }
      }

      if (!shouldAdd) break;
    }

    if (!shouldAdd) queue.remove(uuid);
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
