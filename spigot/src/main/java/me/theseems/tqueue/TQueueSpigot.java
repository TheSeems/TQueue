package me.theseems.tqueue;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class TQueueSpigot extends JavaPlugin {
  private static Plugin plugin;
  private static SpigotPingReplier replier;

  @Override
  public void onEnable() {
    plugin = this;
    getLogger().info("Running TQueue instance for Spigot/Bukkit");
    getLogger().info("Setting up messaging");
    replier = new SimplePingReplier();
    new RedisMessenger();
    getLogger().info("Ready");

    // Whitelist
    replier.addProcessor(
        player -> {
          if (!getServer().hasWhitelist())
            return Optional.empty();

          for (OfflinePlayer whitelistedPlayer : getServer().getWhitelistedPlayers()) {
            if (whitelistedPlayer.getUniqueId() == player) return Optional.empty();
          }

          return Optional.of(Verdict.WHITELISTED);
        });
    // Ban
    replier.addProcessor(
        player -> {
          if (getServer().getBannedPlayers().contains(getServer().getOfflinePlayer(player))) {
            return Optional.of(Verdict.FORBIDDEN);
          }
          return Optional.empty();
        });
  }

  public static Plugin getPlugin() {
    return plugin;
  }

  public void setPlugin(Plugin plugin) {
    TQueueSpigot.plugin = plugin;
  }

  public static SpigotPingReplier getReplier() {
    return replier;
  }
}
