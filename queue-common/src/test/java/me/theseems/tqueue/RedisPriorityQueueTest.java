package me.theseems.tqueue;

import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.UUID;

public class RedisPriorityQueueTest {
    private static JedisPool buildPool() {
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
        return new JedisPool(poolConfig, "localhost");
  }

  public static void main(String[] args) {
      QueueTestCore.setup();
      QueueAPI.setQueueManager(
              new TQueueManager() {
                  @Override
                  public Queue make(@NotNull String name, int delay) {
                      Queue queue =
                              new RedisPriorityQueue(buildPool(), delay) {
                                  @Override
                                  public String getName() {
                                      return name;
                                  }
                              };
                      QueueTestCore.setFor(queue);
                      return queue;
                  }
              });
      Queue redisPriorityQueue = QueueAPI.getQueueManager().make("test", 200);

      redisPriorityQueue.getDestinations().add("test", new DummyDestination(Verdict.OK, "test", 0));
      redisPriorityQueue
              .getHandlers()
              .add(
                      "test",
                      new QueueHandler() {
                          @Override
                          public boolean onApply(UUID player, Destination destination, Verdict verdict) {
                              System.out.println(player + " " + destination + " " + verdict);
                              redisPriorityQueue.remove(player);
                              return true;
                          }

                          @Override
                          public String getName() {
                              return "test";
                          }
                      });
      redisPriorityQueue.add(UUID.randomUUID());
  }
}
