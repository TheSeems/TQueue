package me.theseems.tqueue;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public abstract class RedisPriorityQueue extends TPriorityQueue {
  private JedisPool pool;
  private int delay;

  public RedisPriorityQueue(JedisPool pool, int delay) {
    super();
    this.pool = pool;
    this.delay = delay;
    this.logger = QueueAPI.logs().prefix("RedisQueue::" + getName());
    run();
  }

  public RedisPriorityQueue(JedisPool pool, int delay, Logger logger) {
    super(delay, logger);
    this.pool = pool;
  }

  @Override
  public void close() {
    super.close();
    if (pool != null) {
      pool.destroy();
      System.out.println("Redis pool destroyed @" + getName());
    }
  }

  @Override
  public void add(UUID player) {
    Jedis jedis = pool.getResource();
    jedis.zadd(getName(), getPriorities().get(player).orElse(0), player.toString());
    jedis.close();

    getHandlers().values().forEach(handler -> handler.onJoin(player));
  }

  @Override
  public int getPosition(UUID player) {
    Jedis jedis = pool.getResource();
    Set<String> response = jedis.zrange(getName(), 0, -1);
    jedis.close();

    int pos = 0;
    for (String s : response) {
      pos++;
      if (player.equals(UUID.fromString(s))) {
        return pos;
      }
    }

    return 0;
  }

  @Override
  public void remove(UUID player) {
    Jedis jedis = pool.getResource();
    jedis.zrem(getName(), player.toString());
    jedis.close();

    getHandlers().values().forEach(handler -> handler.onLeave(player));
  }

  public void run() {
    Runnable runnable =
      () -> {
        Date latest = new Date();
        while (true) {
          if (ChronoUnit.MILLIS.between(latest.toInstant(), Calendar.getInstance().toInstant())
            < delay) continue;

          if (isClosed) {
            getDestinations().clear();
            getHandlers().clear();
            logger.info("Queue '" + getName() + "' shutdown");
            break;
          } else {
            try {
              Jedis jedis = pool.getResource();
              Set<String> response = jedis.zrange(getName(), 0, -1);
              jedis.close();
              for (String s : response) {
                UUID uuid = UUID.fromString(s);
                go(uuid);
              }
            } catch (Exception e) {
              logger.warning(
                "Execution exception in queue '" + getName() + "': " + e.getMessage());
              e.printStackTrace();
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
