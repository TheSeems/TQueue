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
import java.util.Properties;
import java.util.concurrent.Executors;

public class RedisMessenger {
  private JedisPool pool;
  private String selfHost;

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
    get()
        .subscribe(
            new JedisPubSub() {
              @Override
              public void onMessage(String channel, String message) {
                try {
                  ByteArrayDataOutput out = ByteStreams.newDataOutput();
                  ByteArrayDataInput in = ByteStreams.newDataInput(message.getBytes());
                  String host = in.readUTF();

                  if (host.equals(selfHost)) {
                    out.writeUTF(selfHost);
                    SpigotMessenger.fillOutput(in, out);
                    Jedis jedis = get();
                    jedis.publish("tqueue:info:proxy", new String(out.toByteArray()));
                    jedis.close();
                  }
                } catch (Exception e) {
                  System.out.println("[RedisMessenger] Error while listening: " + e.getMessage());
                  e.printStackTrace();
                }
              }
            },
            "tqueue:info:inst");
  }

  public RedisMessenger() {
    fillSelfHost();
    pool = new JedisPool(buildPoolConfig(), "localhost");
    Executors.newFixedThreadPool(1).submit(this::listen);
  }
}
