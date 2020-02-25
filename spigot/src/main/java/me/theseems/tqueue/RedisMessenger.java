package me.theseems.tqueue;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class RedisMessenger implements QueueCommunicator {
  private JedisPool pool;
  private String selfHost;

  public void shutdown() {
    pool.destroy();
  }

  public RedisMessenger() {
    fillSelfHost();
    pool = new JedisPool(buildPoolConfig(), "localhost");
    Executors.newFixedThreadPool(1).submit(this::listen);
  }

  void fillOutput(ByteArrayDataInput in, ByteArrayDataOutput out) {
    try {
      UUID uuid = UUID.fromString(in.readUTF());
      Verdict verdict = TQueueSpigot.getReplier().process(uuid);
      out.writeBoolean(verdict.ok);
      out.writeUTF(verdict.desc);
    } catch (Exception e) {
      e.printStackTrace();
      out.writeBoolean(false);
      out.writeUTF("Internal error: " + e.getMessage());
    }
  }

  public void fillSelfHost() {
    int port;
    try {
      BufferedReader is = new BufferedReader(new FileReader("server.properties"));
      Properties props = new Properties();
      props.load(is);
      is.close();
      port = Integer.parseInt(props.getProperty("server-port"));
      String ip = props.getProperty("server-ip");
      selfHost = (ip.isEmpty() ? "localhost" : ip) + ":" + port;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

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
      throw new IllegalStateException("Error getting Jedis out of pool: " + e.getMessage());
    }
  }

  public void listen() {
    Jedis jedis = get();
    jedis.subscribe(
            new JedisPubSub() {
              @Override
              public void onMessage(String channel, String message) {
                try {
                  ByteArrayDataOutput out = ByteStreams.newDataOutput();
                  ByteArrayDataInput in = ByteStreams.newDataInput(message.getBytes());
                  System.out.println("MSG " + message);
                  String host = in.readUTF();
                  System.out.println("Ours " + selfHost + " VV " + host);

                  if (host.equals(selfHost)) {
                    System.out.println("Outs!");
                    out.writeUTF(selfHost);
                    fillOutput(in, out);
                    Jedis jedis = get();
                    jedis.publish("tqueue:info:proxy", new String(out.toByteArray()));
                    jedis.close();
                    System.out.println("Published " + new String(out.toByteArray()));
                  }
                } catch (Exception e) {
                  System.out.println("[RedisMessenger] Error while listening: " + e.getMessage());
                  e.printStackTrace();
                }
              }
            },
            "tqueue:info:inst");
  }

  @Override
  public void join(UUID player, String name) {
    Jedis jedis = get();
    jedis.zadd(name, 0, player.toString());
    jedis.close();
  }

  @Override
  public void leave(UUID player, String name) {
    Jedis jedis = get();
    jedis.zrem(name, player.toString());
    jedis.close();
  }

  @Override
  public Collection<UUID> getPlayers(String name) {
    Jedis jedis = get();
    Collection<String> result = jedis.zrange(name, 0, -1);
    jedis.close();

    return result.stream().map(UUID::fromString).collect(Collectors.toList());
  }
}
