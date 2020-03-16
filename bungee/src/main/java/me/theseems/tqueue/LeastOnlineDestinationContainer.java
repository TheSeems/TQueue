package me.theseems.tqueue;

import net.md_5.bungee.api.config.ServerInfo;
import org.jetbrains.annotations.NotNull;

public class LeastOnlineDestinationContainer extends PriorityMappedContainer<String, Destination> {
  @Override
  public int getPriority(@NotNull String name, @NotNull Destination value) {
    ServerInfo info = TQueueBungeePlugin.getProxyServer().getServerInfo(name);
    if (info == null) {
      return value.getPriority();
    }
    return -info.getPlayers().size();
  }

  @Override
  public void add(Destination value) {
    this.add(value.getName(), value);
  }
}
