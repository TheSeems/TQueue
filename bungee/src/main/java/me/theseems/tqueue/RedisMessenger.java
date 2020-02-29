package me.theseems.tqueue;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class RedisMessenger implements Messenger {
  private JedisPool pool;
  private Logger logger;
  private Map<String, Pair<Boolean, String>> answerMap;

  private JedisPoolConfig buildPoolConfig() {
    return getJedisPoolConfig();
  }

  @NotNull
  static JedisPoolConfig getJedisPoolConfig() {
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

  private Jedis get() {
    try {
      return pool.getResource();
    } catch (Exception e) {
      throw new IllegalStateException("Can't get Jedis instance from the pool: " + e.getMessage());
    }
  }

  void fillOutputFor(UUID player, ByteArrayDataOutput out) {
    ProxiedPlayer pp = TQueueBungeePlugin.getProxyServer().getPlayer(player);
    assert pp != null;
    out.writeUTF(player.toString());
  }

  public void listen() {
    Jedis jedis = get();
    jedis.subscribe(
        new JedisPubSub() {
          @Override
          public void onMessage(String channel, String message) {
            try {
              logger.info("[RedisMessenger] Message: " + message);
              ByteArrayDataInput in = ByteStreams.newDataInput(message.getBytes());
              String name = in.readUTF();
              boolean result = in.readBoolean();
              String additional = in.readUTF();

              answerMap.put(name, new Pair<>(result, additional));
            } catch (Exception e) {
              logger.warning("[RedisMessenger] Error listening: " + e.getMessage());
              e.printStackTrace();
            }
          }
        },
        "tqueue:info:proxy");
  }

  public RedisMessenger() {
    logger = TQueueBungeePlugin.getPlugin().getLogger();
    logger.info(
        "Trying to connect to Redis at "
            + TQueueBungeePlugin.getConfig().getSetting("redis_host", "localhost"));
    pool =
        new JedisPool(
            buildPoolConfig(),
            TQueueBungeePlugin.getConfig().getSetting("redis_host", "localhost"));
    answerMap = new HashMap<>();

    logger.info("Listening...");
    QueueAPI.getService().submit(this::listen);
  }

  @Override
  public CompletableFuture<Verdict> requestUser(String name, UUID player) {
    ServerInfo info = TQueueBungeePlugin.getProxyServer().getServerInfo(name);
    if (info == null) {
      return CompletableFuture.completedFuture(Verdict.UNKNOWN);
    }

    ProxiedPlayer proxiedPlayer = TQueueBungeePlugin.getProxyServer().getPlayer(player);
    if (proxiedPlayer == null) {
      return CompletableFuture.completedFuture(Verdict.FORBIDDEN);
    }

    ByteArrayDataOutput out = ByteStreams.newDataOutput();

    fillOutputFor(player, out);
    Jedis jedis = get();
    jedis.publish("tqueue:info:" + name, new String(out.toByteArray()));
    jedis.close();

    return getVerdictCompletableFuture(name, answerMap);
  }

  static CompletableFuture<Verdict> getVerdictCompletableFuture(
      String name, Map<String, Pair<Boolean, String>> answerMap) {
    return CompletableFuture.supplyAsync(
        () -> {
          while (true) {
            if (answerMap.containsKey(name)) {
              Pair<Boolean, String> result = answerMap.get(name);
              answerMap.remove(name);
              return result.first ? Verdict.OK : Verdict.FORBIDDEN;
            }
          }
        });
  }
}
