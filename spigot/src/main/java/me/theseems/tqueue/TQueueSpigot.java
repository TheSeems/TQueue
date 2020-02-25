package me.theseems.tqueue;

import com.google.gson.GsonBuilder;
import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

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

  private File prepareFile(String name) throws IOException {
    plugin.getDataFolder().mkdir();
    File target = new File(plugin.getDataFolder(), name);
    if (!target.exists()) {
      target.createNewFile();
    }
    return target;
  }

  private void loadConfig() {
    try {
      File config = prepareFile("config.json");
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
    communicator = new RedisMessenger();

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
          if (getServer().getBannedPlayers().contains(getServer().getOfflinePlayer(player))) {
            Verdict verdict = Verdict.FORBIDDEN;
            OfflinePlayer offlinePlayer = getServer().getOfflinePlayer(player);
            if (offlinePlayer.getName() == null) {
              return Optional.empty();
            }

            BanEntry entry = getServer()
              .getBanList(BanList.Type.NAME)
              .getBanEntry(offlinePlayer.getName());

            if (entry == null) {
              return Optional.empty();
            }

            verdict.setDesc(entry.getReason());
            return Optional.of(verdict);
          }
          return Optional.empty();
        });

    Objects.requireNonNull(getCommand("qjoin")).setExecutor(new QueueJoinLocalCommand());
    Objects.requireNonNull(getCommand("qleave")).setExecutor(new QueueLeaveLocalCommand());
    Objects.requireNonNull(getCommand("qinfo")).setExecutor(new QueueInfoLocalCommand());
    getLogger().info("Ready");
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
