package me.theseems.tqueue;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class RedisMessenger implements Messenger {
  private JedisPool pool;
  private Logger logger;
  private Map<String, Pair<Boolean, String>> answerMap;
  private Map<String, String> ipNameMap;

  private JedisPoolConfig buildPoolConfig() {
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

  public void listen() {
    Jedis jedis = get();
    jedis.subscribe(
        new JedisPubSub() {
          @Override
          public void onMessage(String channel, String message) {
            logger.info("Received message on " + channel + ": " + message);
            ByteArrayDataInput in = ByteStreams.newDataInput(message.getBytes());

            String name = in.readUTF();
            boolean result = in.readBoolean();
            String additional = in.readUTF();

            logger.info("Read as " + result + " with info: " + additional);
            answerMap.put(ipNameMap.get(name), new Pair<>(result, additional));
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
    ipNameMap = new HashMap<>();

    logger.info("Listening...");
    QueueAPI.getService().submit(() -> {
      while (true) {
        try {
          listen();
        } catch (Exception e) {
          logger.warning(e.getMessage());
        }
      }
    });
  }

  String getHostFor(ServerInfo info) {
    return info.getAddress().getHostString() + ":" + info.getAddress().getPort();
  }

  @Override
  public CompletableFuture<Verdict> requestUser(String name, UUID player) {
    ServerInfo info = TQueueBungeePlugin.getProxyServer().getServerInfo(name);
    if (info == null) {
      return CompletableFuture.completedFuture(Verdict.FORBIDDEN);
    }

    ProxiedPlayer proxiedPlayer = TQueueBungeePlugin.getProxyServer().getPlayer(player);
    if (proxiedPlayer == null) {
      return CompletableFuture.completedFuture(Verdict.FORBIDDEN);
    }

    String host = getHostFor(info);
    ipNameMap.put(host, name);

    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    logger.info("Pinging " + host);

    out.writeUTF(host);
    BungeeMessenger.forUserOut(player, out);
    get().publish("tqueue:info:inst", new String(out.toByteArray()));

    return getVerdictCompletableFuture(name, answerMap)
        .thenApply(
            verdict -> {
              ipNameMap.remove(
                  info.getAddress().getHostString() + ":" + info.getAddress().getPort());
              return verdict;
            });
  }

  static CompletableFuture<Verdict> getVerdictCompletableFuture(
      String name, Map<String, Pair<Boolean, String>> answerMap) {
    return CompletableFuture.supplyAsync(
        () -> {
          Calendar date = Calendar.getInstance();
          while (ChronoUnit.SECONDS.between(date.toInstant(), Calendar.getInstance().toInstant())
              < 5) {
            if (answerMap.containsKey(name)) {
              Pair<Boolean, String> result = answerMap.get(name);
              answerMap.remove(name);
              return result.first ? Verdict.OK : Verdict.FORBIDDEN;
            }
          }
          throw new IllegalStateException("Request timed out");
        });
  }
}
