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

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class RedisMessenger implements Messenger {
  private JedisPool pool;
  private Logger logger;
  private Map<Pair<String, UUID>, Pair<Boolean, String>> answerMap;

  public RedisMessenger() {
    logger = TQueueBungeePlugin.getPlugin().getLogger();
    logger.info(
      "Trying to connect to Redis at "
        + TQueueBungeePlugin.getConfig().getSetting("redis_host", "localhost"));
    pool =
      new JedisPool(
        buildPoolConfig(),
        TQueueBungeePlugin.getConfig().getSetting("redis_host", "localhost"));
    answerMap = new ConcurrentHashMap<>();

    logger.info("Listening...");
    QueueAPI.getService().submit(this::listen);
  }

  @NotNull
  static JedisPoolConfig getJedisPoolConfig() {
    final JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(128);
    poolConfig.setMaxIdle(128);
    poolConfig.setMinIdle(2);
    return poolConfig;
  }

  static CompletableFuture<Verdict> getVerdictCompletableFuture(
    String name, UUID player, Map<Pair<String, UUID>, Pair<Boolean, String>> answerMap) {
    final boolean[] valid = {true};
    return CompletableFuture.supplyAsync(
      () -> {
        while (valid[0]) {
          if (answerMap.containsKey(Pair.of(name, player))) {
            Pair<Boolean, String> result = answerMap.get(Pair.of(name, player));
            answerMap.remove(Pair.of(name, player));
            Verdict verdict = result.first ? Verdict.OK : Verdict.FORBIDDEN;
            verdict.setDesc(result.second);
            return verdict;
          }
        }
        return Verdict.UNKNOWN;
      })
      .orTimeout(1, TimeUnit.SECONDS)
      .exceptionally(
        throwable -> {
          System.out.println("Thrown " + throwable);
          valid[0] = false;
          Verdict verdict = Verdict.TIMED_OUT;
          if (!(throwable instanceof TimeoutException)) {
            verdict = Verdict.UNKNOWN;
            verdict.setDesc("Internal error (@B) : " + throwable.getMessage());
          }
          return verdict;
        });
  }

  private JedisPoolConfig buildPoolConfig() {
    return getJedisPoolConfig();
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
            UUID player = UUID.fromString(in.readUTF());
            boolean result = in.readBoolean();
            String additional = in.readUTF();

            answerMap.put(new Pair<>(name, player), new Pair<>(result, additional));
          } catch (Exception e) {
            logger.warning("[RedisMessenger] Error listening: " + e.getMessage());
            e.printStackTrace();
          }
        }
      },
      "tqueue:info:proxy");
  }

  @Override
  public CompletableFuture<Verdict> requestUser(String name, UUID player) {

    System.out.println("Requesting user " + player + " on a  " + name);
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
    out.writeUTF(new Date().toString());
    Jedis jedis = get();
    jedis.publish("tqueue:info:" + name, new String(out.toByteArray()));
    jedis.close();

    return getVerdictCompletableFuture(name, player, answerMap);
  }

  @Override
  public void shutdown() {
    pool.close();
  }
}
