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
      public boolean apply(UUID player, Destination destination, Verdict verdict) {
        ProxiedPlayer p = TQueueBungeePlugin.getProxyServer().getPlayer(player);
        if (p == null) {
          return true;
        }

        System.out.println(">> Handler " + queue.toString() + " " + verdict);
        if (p.getServer() != null && destination.getName().equals(p.getServer().getInfo().getName()))
          return false;

        if (verdict.ok) {
          p.sendMessage(
              ChatMessageType.ACTION_BAR,
              new TextComponent(TQueueBungeePlugin.getConfig().get("passed")));
          p.connect(
              TQueueBungeePlugin.getProxyServer().getServerInfo(destination.getName()),
              ServerConnectEvent.Reason.PLUGIN);
          return true;
        } else {
          queue.add(player);
          p.sendMessage(
              ChatMessageType.ACTION_BAR,
              new TextComponent(
                  MessageFormat.format(
                      TQueueBungeePlugin.getConfig().get("status"),
                      queue.getPosition(player),
                      queue.getPlayers().size())));
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
