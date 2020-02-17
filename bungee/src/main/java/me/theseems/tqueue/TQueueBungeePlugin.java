package me.theseems.tqueue;

import com.google.gson.GsonBuilder;
import me.theseems.tqueue.commands.QueueCommand;
import me.theseems.tqueue.config.BungeeQueueConfig;
import me.theseems.tqueue.config.QueuePluginConfig;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TQueueBungeePlugin extends Plugin {
  private static ProxyServer proxyServer;
  private static Plugin plugin;
  private static QueuePluginConfig config;
  private static Messenger messenger;

  private File initPluginFile(String name) {
    if (!getDataFolder().exists()) {
      getDataFolder().mkdir();
    }

    return new File(getDataFolder(), name);
  }

  public void loadConfig() {
    File file = initPluginFile("config.json");
    try {
      config = new GsonBuilder().create().fromJson(new FileReader(file), QueuePluginConfig.class);
      getLogger().info("Loaded config from file");
    } catch (IOException e) {
      config =
          new QueuePluginConfig(
              new HashMap<String, String>() {
                {
                  put("status", "§7In queue... {0}/{1}. To leave type /queue leave");
                  put("passed", "§aQueue passed");
                }
              },
              new HashMap<>(),
              new ArrayList<>());
      try {
        FileWriter writer = new FileWriter(file);
        new GsonBuilder().setPrettyPrinting().create().toJson(config, writer);
        writer.flush();
        getLogger().info("Created example config");
      } catch (IOException ex) {
        getLogger().warning("Cannot write example config. Using BLANK one");
        ex.printStackTrace();
      }
      getLogger().warning("Cannot setup config from file");
      e.printStackTrace();
    }
  }

  public void saveConfig() {
    getLogger().info("Saving all queues to config");

    List<BungeeQueueConfig> configList = new ArrayList<>();
    for (Queue queue : QueueAPI.getQueueManager().getQueues()) {
      if (!(queue instanceof TPriorityQueue)) {
        getLogger().warning("Found custom queue '" + queue.getName() + "', skipping it");
        continue;
      }

      Map<String, Integer> servers = new HashMap<>();

      for (Destination destination : queue.getDestinations()) {
        if (destination instanceof ServerDestination)
          servers.put(destination.getName(), destination.getPriority());
        else {
          getLogger()
              .warning("Found non bungee destination '" + destination.getName() + "', skipping it");
        }
      }

      BungeeQueueConfig config =
          new BungeeQueueConfig(
              queue.getName(),
              ((TPriorityQueue) queue).getDelay(),
              servers,
              new ArrayList<>(queue.getHandlers()));
      configList.add(config);
    }

    config.setQueues(configList);
    FileWriter writer;
    try {
      writer = new FileWriter(initPluginFile("config.json"));
      new GsonBuilder().setPrettyPrinting().create().toJson(config, writer);
      writer.flush();
    } catch (IOException e) {
      getLogger().severe("Error saving config to file");
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
  }

  @Override
  public void onEnable() {
    proxyServer = getProxy();
    getProxyServer().getPluginManager().registerCommand(this, new QueueCommand());
    plugin = this;

    loadConfig();

    getLogger().info("Setting up queue API");
    new QueueAPI(5);
    QueueAPI.setQueueManager(new TQueueManager());
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
