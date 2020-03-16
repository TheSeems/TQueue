package me.theseems.tqueue;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;

import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class ServerDestination implements Destination, Listener {
  private String name;
  private int priority;

  public ServerDestination(String name, int priority) {
    this.name = name;
    this.priority = priority;
  }

  @Override
  public String getName() {
    return name;
  }

  public Future<Verdict> query(UUID user) {
    return QueueAPI.getService()
      .submit(
        () -> {
          ProxyServer server = TQueueBungeePlugin.getProxyServer();
          ServerInfo info = server.getServerInfo(getName());
          if (info == null) {
            return Verdict.FORBIDDEN;
          }

          ProxiedPlayer player = TQueueBungeePlugin.getProxyServer().getPlayer(user);
          if (player == null) {
            return Verdict.FORBIDDEN;
          }

          return TQueueBungeePlugin.getMessenger()
            .requestUser(getName(), user)
            .get(3, TimeUnit.SECONDS);
        });
  }

  @Override
  public int getPriority() {
    return priority;
  }
}
