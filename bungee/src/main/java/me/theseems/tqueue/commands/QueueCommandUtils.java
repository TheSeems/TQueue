package me.theseems.tqueue.commands;

import me.theseems.tqueue.Queue;
import me.theseems.tqueue.QueueAPI;
import me.theseems.tqueue.TQueueBungeePlugin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Optional;
import java.util.UUID;

public class QueueCommandUtils {
  public static class QueueNotFoundException extends IllegalStateException {
    private String name;
    public QueueNotFoundException(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  public static class PlayerNotFoundException extends IllegalStateException {
    private UUID uuid;
    private String name;

    public PlayerNotFoundException(UUID uuid, String name) {
      this.uuid = uuid;
      this.name = name;
    }

    public UUID getUuid() {
      return uuid;
    }

    public String getName() {
      return name;
    }
  }

  public static Queue requireQueue(String name) {
    Optional<Queue> optionalQueue = QueueAPI.getQueueManager().getQueue(name);
    if (!optionalQueue.isPresent()) {
      throw new QueueNotFoundException(name);
    }
    return optionalQueue.get();
  }

  public ProxiedPlayer requirePlayer(UUID uuid) {
    ProxiedPlayer player = TQueueBungeePlugin.getProxyServer().getPlayer(uuid);
    if (player == null) {
      throw new PlayerNotFoundException(uuid, "");
    }
    return player;
  }

  public static ProxiedPlayer requirePlayer(String name) {
    ProxiedPlayer player = TQueueBungeePlugin.getProxyServer().getPlayer(name);
    if (player == null) {
      throw new PlayerNotFoundException(null, name);
    }
    return player;
  }
}
