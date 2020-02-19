package me.theseems.tqueue;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Collectors;

public abstract class RedisPriorityQueue extends TPriorityQueue {
  private JedisPool pool;
  private int delay;

  public RedisPriorityQueue(JedisPool pool, int delay) {
    super();
    this.pool = pool;
    this.delay = delay;

    destinationList = new PriorityBlockingQueue<>(1, new TQueueDestinationComparator());
    handlerList = new HashMap<>();
    run();
  }

  @Override
  public void add(UUID player) {
    Jedis jedis = pool.getResource();
    jedis.zadd(getName(), getPriority(player), player.toString());
    jedis.close();

    handlerList.values().forEach(handler -> handler.join(player));
  }

  @Override
  public void remove(UUID player) {
    Jedis jedis = pool.getResource();
    jedis.zrem(getName(), player.toString());
    jedis.close();

    handlerList.values().forEach(handler -> handler.leave(player));
  }

  public void run() {
    Runnable runnable =
        () -> {
          Date latest = new Date();
          while (true) {
            if (ChronoUnit.MILLIS.between(latest.toInstant(), Calendar.getInstance().toInstant())
                < delay) continue;

            if (isClosed) {
              destinationList.clear();
              handlerList.clear();
              System.out.println("Queue '" + getName() + "' shutdown");
              break;
            } else {
              try {
                Jedis jedis = pool.getResource();
                Set<String> response = jedis.zrange(getName(), 0, -1);
                jedis.close();
                for (String s : response) {
                  System.out.println("SS " + s);
                  UUID uuid = UUID.fromString(s);
                  go(uuid);
                }
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
  public Collection<UUID> getPlayers() {
    Jedis jedis = pool.getResource();
    Set<String> response = jedis.zrange(getName(), 0, -1);
    jedis.close();

    return response.stream().map(UUID::fromString).collect(Collectors.toSet());
  }
}
