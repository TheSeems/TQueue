package me.theseems.tqueue;

import com.google.gson.GsonBuilder;
import me.theseems.tqueue.commands.QueueCommand;
import me.theseems.tqueue.config.BungeeQueueConfig;
import me.theseems.tqueue.config.QueuePluginConfig;
import me.theseems.tqueue.config.RedisConfig;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.theseems.tqueue.RedisMessenger.getJedisPoolConfig;

public class TQueueBungeePlugin extends Plugin {
  private static ProxyServer proxyServer;
  private static Plugin plugin;
  private static QueuePluginConfig config;
  private static Messenger messenger;

  private static JedisPoolConfig buildPoolConfig() {
    return getJedisPoolConfig();
  }

  private static JedisPool generate() {
    return new JedisPool(
      buildPoolConfig(),
      config.getRedisConfig().getHost(),
      config.getRedisConfig().getPort(),
      Protocol.DEFAULT_TIMEOUT,
      getConfig().getRedisConfig().getPassword());
  }

  private static File initConfig() {
    if (!plugin.getDataFolder().exists()) {
      plugin.getDataFolder().mkdir();
    }

    return new File(plugin.getDataFolder(), "config.json");
  }

  public static void saveConfig() {
    QueueAPI.logs().get().info("Saving all queues to config");

    List<BungeeQueueConfig> configList = new ArrayList<>();
    for (Queue queue : QueueAPI.getQueueManager().getQueues()) {
      if (!(queue instanceof TPriorityQueue)) {
        QueueAPI.logs().get().warning("Found custom queue '" + queue.getName() + "', skipping it");
        continue;
      }

      Map<String, Integer> servers = new HashMap<>();

      for (Destination destination : queue.getDestinations().values()) {
        if (destination instanceof ServerDestination)
          servers.put(destination.getName(), destination.getPriority());
        else {
          QueueAPI.logs().get()
            .warning("Found non-bungee destination '" + destination.getName() + "', skipping it");
        }
      }

      BungeeQueueConfig config =
        new BungeeQueueConfig(
          queue.getName(),
          queue.getDelay(),
          servers,
          new ArrayList<>(queue.getHandlers().keys()));
      configList.add(config);
    }

    config.setQueues(configList);
    FileWriter writer;
    try {
      writer = new FileWriter(initConfig());
      new GsonBuilder().setPrettyPrinting().create().toJson(config, writer);
      writer.flush();
    } catch (IOException e) {
      QueueAPI.logs().get().severe("Error saving config to file");
      e.printStackTrace();
    }
  }

  public void loadConfig() {
    File file = initConfig();
    try {
      config = new GsonBuilder().create().fromJson(new FileReader(file), QueuePluginConfig.class);
      getLogger().info("Loaded config from file");
    } catch (IOException e) {
      config =
        new QueuePluginConfig(
          new HashMap<>() {
            {
              put("status", "§7Queued: {0}/{1}");
              put("passed", "§aQueue passed");
              put("verdict", "§7Queue: {0} | {1}");
            }
          },
          new HashMap<>(),
          new ArrayList<>());

      config.setRedisConfig(new RedisConfig("localhost", 6379, null));

      try {
        FileWriter writer = new FileWriter(file);
        new GsonBuilder().setPrettyPrinting().create().toJson(config, writer);
        writer.flush();
        getLogger().info("Created example config");
      } catch (IOException ex) {
        getLogger()
          .warning(
            "Cannot write example config. Using NULL (!) one. You will experience issues!");
        ex.printStackTrace();
      }
      getLogger().warning("Cannot setup config from file");
      e.printStackTrace();
    }
  }

  public void loadQueuesFromConfig() {
    getLogger().info("Loading queues from config...");

    for (BungeeQueueConfig queueConfig : config.getQueues()) {
      if (QueueAPI.getQueueManager().getQueue(queueConfig.getName()).isPresent()) {
        getLogger()
          .warning("Queue '" + queueConfig.getName() + "' from config already exists in manager");
        continue;
      }

      getLogger().info("Registering queue '" + queueConfig.getName() + "' from config");
      Queue queue = queueConfig.construct();
      QueueAPI.getQueueManager().register(queue);
    }
  }

  @Override
  public void onDisable() {
    saveConfig();
    messenger.shutdown();
  }

  @Override
  public void onEnable() {
    proxyServer = getProxy();
    getProxyServer().getPluginManager().registerCommand(this, new QueueCommand());
    plugin = this;

    loadConfig();

    getLogger().info("Setting up queue API");
    new QueueAPI(5);
    QueueAPI.setLogManager(new SimpleLogManager());

    QueueAPI.setHandlerManager(new SimpleQueueHandlerManager());
    QueueAPI.getHandlerManager().register(new QueueHandlerFactory() {
      @Override
      public QueueHandler produce(Queue queue) {
        return Utils.getDefaultHandlerFor(queue);
      }

      @Override
      public String getName() {
        return "bungee-default";
      }
    });

    QueueAPI.setQueueManager(
      new TQueueManager() {
        @Override
        public RedisPriorityQueue make(@NotNull String name, int delay) {
          RedisPriorityQueue queue =
            new RedisPriorityQueue(generate(), delay) {
              @Override
              public String getName() {
                return name;
              }
            };

          queue.setDestinations(new LeastOnlineDestinationContainer());
          queue.setHandlers(
            new SimpleMappedContainer<>() {
              @Override
              public void add(QueueHandler value) {
                this.add(value.getName(), value);
              }
            });
          queue.setPriorities(
            new SimpleMappedContainer<>() {
              @Override
              public void add(Integer value) {
                throw new IllegalStateException("Adding priority nowhere is not supported");
              }
            });
          return queue;
        }
      });
    getLogger().info("Queue API set up");

    getLogger().info("Registering messenger");
    messenger = new RedisMessenger();
    getLogger().info("Messaging set up");

    loadQueuesFromConfig();
  }

  public static ProxyServer getProxyServer() {
    return proxyServer;
  }

  public static QueuePluginConfig getConfig() {
    return config;
  }

  public static Plugin getPlugin() {
    return plugin;
  }

  public static Messenger getMessenger() {
    return messenger;
  }
}
