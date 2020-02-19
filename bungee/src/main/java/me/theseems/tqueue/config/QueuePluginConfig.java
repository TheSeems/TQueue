package me.theseems.tqueue.config;

import java.util.List;
import java.util.Map;

public class QueuePluginConfig {
  private Map<String, String> messages;
  private Map<String, String> settings;
  private List<BungeeQueueConfig> queues;
  private RedisConfig redisConfig;

  public QueuePluginConfig(Map<String, String> messages, Map<String, String> settings, List<BungeeQueueConfig> queues) {
    this.messages = messages;
    this.settings = settings;
    this.queues = queues;
  }

  public void setRedisConfig(RedisConfig redisConfig) {
    this.redisConfig = redisConfig;
  }

  public List<BungeeQueueConfig> getQueues() {
    return queues;
  }

  public String get(String message) {
    return messages.getOrDefault(message, "message." + message);
  }

  public String getSetting(String name) {
    return settings.getOrDefault(name, "setting." + name);
  }

  public String getSetting(String name, String def) {
    return settings.getOrDefault(name, def);
  }

  public Map<String, String> getMessages() {
    return messages;
  }

  public void setMessages(Map<String, String> messages) {
    this.messages = messages;
  }

  public Map<String, String> getSettings() {
    return settings;
  }

  public void setSettings(Map<String, String> settings) {
    this.settings = settings;
  }

  public void setQueues(List<BungeeQueueConfig> queues) {
    this.queues = queues;
  }

  public RedisConfig getRedisConfig() {
    return redisConfig;
  }
}
