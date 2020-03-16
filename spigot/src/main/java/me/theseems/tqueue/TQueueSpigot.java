package me.theseems.tqueue;

import com.google.gson.GsonBuilder;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class TQueueSpigot extends JavaPlugin {
  private static Plugin plugin;
  private static SpigotPingReplier replier;
  private static QueueCommunicator communicator;
  private static SpigotQueueConfig config;

  private File prepareFile() throws IOException {
    plugin.getDataFolder().mkdir();
    File target = new File(plugin.getDataFolder(), "config.json");
    if (!target.exists()) {
      target.createNewFile();
    }
    return target;
  }

  private JedisPool buildPool() {
    final JedisPoolConfig poolConfig = new JedisPoolConfig();
    poolConfig.setMaxTotal(128);
    poolConfig.setMaxIdle(128);
    poolConfig.setMinIdle(2);
    return new JedisPool(
      poolConfig,
      config.getRedisConfig().getHost(),
      config.getRedisConfig().getPort(),
      Protocol.DEFAULT_TIMEOUT,
      config.getRedisConfig().getPassword());
  }

  private void loadConfig() {
    try {
      File config = prepareFile();
      TQueueSpigot.config =
        new GsonBuilder().create().fromJson(new FileReader(config), SpigotQueueConfig.class);
    } catch (IOException e) {
      getLogger().severe("Cannot load config");
      e.printStackTrace();
    }
  }

  @Override
  public void onDisable() {
    getLogger().info("Shutting down the communicator...");
    communicator.shutdown();
    getLogger().info("Shut down communicator");
  }

  @Override
  public void onEnable() {
    plugin = this;
    getLogger().info("Running TQueue instance for Spigot/Bukkit");

    getLogger().info("Loading config...");
    loadConfig();
    getLogger().info("OK. Config loaded");

    getLogger().info("Setting up messaging");
    replier = new SimplePingReplier();
    communicator = new RedisMessenger(buildPool());

    // Whitelist
    replier.addProcessor(
      player -> {
        if (!getServer().hasWhitelist()) return Optional.empty();
        for (OfflinePlayer whitelistedPlayer : getServer().getWhitelistedPlayers()) {
          if (whitelistedPlayer.getUniqueId() == player) return Optional.empty();
        }

        return Optional.of(Verdict.WHITELISTED);
      });
    // Ban
    replier.addProcessor(
      player -> {
        OfflinePlayer offlinePlayer = getServer().getOfflinePlayer(player);
        if (offlinePlayer.getName() == null) {
          return Optional.empty();
        }

        if (offlinePlayer.isBanned()) {
          BanEntry entry =
            Bukkit.getBanList(BanList.Type.NAME).getBanEntry(offlinePlayer.getName());
          Verdict verdict = Verdict.FORBIDDEN;
          if (entry != null) {
            verdict.setDesc(entry.getReason());
          } else {
            verdict.setDesc("<FT> " + Bukkit.getBanList(BanList.Type.NAME).getBanEntries());
          }
          return Optional.of(verdict);
        }
        return Optional.empty();
      });

    Objects.requireNonNull(getCommand("qjoin")).setExecutor(new QueueJoinLocalCommand());
    Objects.requireNonNull(getCommand("qleave")).setExecutor(new QueueLeaveLocalCommand());
    Objects.requireNonNull(getCommand("qinfo")).setExecutor(new QueueInfoLocalCommand());
    getLogger().info("Ready");
  }

  @NotNull
  public static SpigotQueueConfig getSettings() {
    return config;
  }

  public static Plugin getPlugin() {
    return plugin;
  }

  public static SpigotPingReplier getReplier() {
    return replier;
  }

  public static QueueCommunicator getCommunicator() {
    return communicator;
  }
}
