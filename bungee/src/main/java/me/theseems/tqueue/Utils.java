package me.theseems.tqueue;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;

import java.text.MessageFormat;
import java.util.UUID;

public class Utils {
  public static QueueHandler getDefaultHandlerFor(Queue queue) {
    return new QueueHandler() {
      @Override
      public boolean onApply(UUID player, Destination destination, Verdict verdict) {
          ProxiedPlayer p = TQueueBungeePlugin.getProxyServer().getPlayer(player);
          if (p == null) {
              return true;
          }

          if (p.getServer() != null
                  && destination.getName().equals(p.getServer().getInfo().getName())) return false;

          if (verdict.ok) {
              queue.remove(player);
          p.sendMessage(
              ChatMessageType.ACTION_BAR,
              new TextComponent(TQueueBungeePlugin.getConfig().get("passed")));
          p.connect(
              TQueueBungeePlugin.getProxyServer().getServerInfo(destination.getName()),
              ServerConnectEvent.Reason.PLUGIN);
          return true;
        } else {
          p.sendMessage(
              ChatMessageType.ACTION_BAR,
              new TextComponent(
                  MessageFormat.format(
                      TQueueBungeePlugin.getConfig().get("status"),
                      queue.getPosition(player),
                      queue.getPlayers().size())));
          p.sendMessage(
              ChatMessageType.CHAT,
              new TextComponent(
                  MessageFormat.format(
                      TQueueBungeePlugin.getConfig().get("verdict"),
                      verdict.name(),
                      verdict.desc)));
          return false;
        }
      }

      @Override
      public String getName() {
        return "bungee-default";
      }
    };
  }
}
