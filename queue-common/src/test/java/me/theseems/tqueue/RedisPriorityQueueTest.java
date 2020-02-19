package me.theseems.tqueue;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Executors;

public class RedisPriorityQueueTest {
  private static JedisPoolConfig buildPoolConfig() {
    final JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(128);
    poolConfig.setMaxIdle(128);
    poolConfig.setMinIdle(16);
    poolConfig.setTestOnBorrow(true);
    poolConfig.setTestOnReturn(true);
    poolConfig.setTestWhileIdle(true);
    poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
    poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
    poolConfig.setNumTestsPerEvictionRun(3);
    poolConfig.setBlockWhenExhausted(true);
    return poolConfig;
  }

  public static void main(String[] args) {
    QueueAPI.setService(Executors.newFixedThreadPool(5));
    JedisPool pool = new JedisPool(buildPoolConfig(), "localhost");
    RedisPriorityQueue redisPriorityQueue = new RedisPriorityQueue(pool, 200) {
      @Override
      public Integer getPriority(UUID player) {
        return 0;
      }

      @Override
      public String getName() {
        return "redis-test";
      }
    };

    redisPriorityQueue.addDestination(new DummyDestination(Verdict.FORBIDDEN, "test", 0));
    redisPriorityQueue.add(UUID.randomUUID());
    redisPriorityQueue.addHandler(
        new QueueHandler() {
          @Override
          public boolean apply(UUID player, Destination destination, Verdict verdict) {
            System.out.println(player + " @ " + destination.getName() + " -> " + verdict);
            return true;
          }

          @Override
          public String getName() {
            return "test";
          }
        });
  }
}
