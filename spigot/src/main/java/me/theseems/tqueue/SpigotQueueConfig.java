package me.theseems.tqueue;

import me.theseems.tqueue.config.RedisConfig;

public class SpigotQueueConfig {
  private RedisConfig redisConfig;
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public SpigotQueueConfig(RedisConfig redisConfig) {
    this.redisConfig = redisConfig;
  }

  public RedisConfig getRedisConfig() {
    return redisConfig;
  }

  public void setRedisConfig(RedisConfig redisConfig) {
    this.redisConfig = redisConfig;
  }
}
