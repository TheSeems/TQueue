package me.theseems.tqueue.commands;

import me.theseems.tqueue.Queue;
import me.theseems.tqueue.TQueueBungeePlugin;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.function.BiConsumer;

public class QueueInfoSub implements SubCommand {

  public static <T> String wrap(
    Collection<T> collection, BiConsumer<StringBuilder, T> consume, String sep) {
    StringBuilder builder = new StringBuilder();
    for (T t : collection) {
      consume.accept(builder, t);
      builder.append(sep);
    }
    if (builder.length() >= sep.length()) {
      builder.delete(builder.length() - sep.length(), builder.length() - 1);
    }
    return builder.toString();
  }

  public static <T> TextComponent namedCollection(String name, Collection<T> collection) {
    return new TextComponent(
      MessageFormat.format(name, collection.size())
        + wrap(collection, (stringBuilder, t) -> stringBuilder.append(t.toString()), ", "));
  }

  public static <T> TextComponent namedCollection(
    String name, Collection<T> collection, BiConsumer<StringBuilder, T> consume, String sep) {
    return new TextComponent(
      MessageFormat.format(name, collection.size()) + wrap(collection, consume, sep));
  }

  public static void sendInfo(CommandSender sender, Queue queue) {
    sender.sendMessage(new TextComponent("§7        Queue §e§l" + queue.getName() + "§r        "));

    sender.sendMessage(
      new TextComponent(
        namedCollection("§eDestinations §7({0}): ", queue.getDestinations().keys())));

    sender.sendMessage(
      namedCollection(
        "§ePlayers §7({0}): ",
        queue.getPlayers(),
        (stringBuilder, uuid) -> {
          ProxiedPlayer proxiedPlayer = TQueueBungeePlugin.getProxyServer().getPlayer(uuid);
          String playerName;
          if (proxiedPlayer == null) {
            playerName = "uuid:" + uuid;
          } else {
            playerName = proxiedPlayer.getDisplayName();
          }
          stringBuilder.append(playerName);
        },
        ", "));

    sender.sendMessage(
      new TextComponent(namedCollection("§eHandlers §7({0}): ", queue.getHandlers().keys())));

    sender.sendMessage(new TextComponent("§eDelay: §7" + queue.getDelay()));
  }

  @Override
  public void pass(CommandSender sender, String[] args) {
    if (args.length == 0) {
      sender.sendMessage(new TextComponent("§cYou have to specify the queue's name"));
      return;
    }

    sendInfo(sender, QueueCommandUtils.requireQueue(args[0]));
  }

  @Override
  public String getPermission() {
    return "tqueue.info";
  }
}
