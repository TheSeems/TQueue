package me.theseems.tqueue;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class RedisMessenger implements QueueCommunicator {
  private JedisPool pool;

  public void shutdown() {
    pool.destroy();
  }

  public RedisMessenger(JedisPool pool) {
    this.pool = pool;
    Executors.newFixedThreadPool(1).submit(this::listen);
  }

  void fillOutput(ByteArrayDataInput in, ByteArrayDataOutput out) {
    try {
      UUID uuid = UUID.fromString(in.readUTF());
      Verdict verdict = TQueueSpigot.getReplier().process(uuid);
      out.writeUTF(uuid.toString());
        out.writeBoolean(verdict.ok);
      out.writeUTF(verdict.desc);
    } catch (Exception e) {
      e.printStackTrace();
      out.writeBoolean(false);
      out.writeUTF("Internal error: " + e.getMessage());
    }
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

              out.writeUTF(TQueueSpigot.getSettings().getName());
                fillOutput(in, out);
                System.out.println("Received " + message + " at " + in.readUTF());
              Jedis jedis = get();
              jedis.publish("tqueue:info:proxy", new String(out.toByteArray()));
              jedis.close();
              System.out.println("Published " + new String(out.toByteArray()));

            } catch (Exception e) {
              System.out.println("[RedisMessenger] Error while listening: " + e.getMessage());
              e.printStackTrace();
            }
          }
        },
        "tqueue:info:" + TQueueSpigot.getSettings().getName());
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
