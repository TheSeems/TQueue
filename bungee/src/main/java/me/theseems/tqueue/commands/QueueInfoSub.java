package me.theseems.tqueue.commands;

import me.theseems.tqueue.Destination;
import me.theseems.tqueue.Queue;
import me.theseems.tqueue.TPriorityQueue;
import me.theseems.tqueue.TQueueBungeePlugin;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.UUID;

public class QueueInfoSub implements SubCommand {

  public static void sendInfo(CommandSender sender, Queue queue) {
    sender.sendMessage(new TextComponent("§7        Queue §e§l" + queue.getName() + "§r        "));

    StringBuilder destBuilder = new StringBuilder();
    for (Destination destination : queue.getDestinations()) {
      destBuilder
          .append(destination.getName())
          .append(",")
          .append(destination.getPriority())
          .append(' ');
    }
    sender.sendMessage(
        new TextComponent(
            "§eDestinations §7("
                + queue.getDestinations().size()
                + "): "
                + destBuilder.toString()));

    StringBuilder builder = new StringBuilder();
    for (UUID player : queue.getPlayers()) {
      builder
          .append(TQueueBungeePlugin.getProxyServer().getPlayer(player).getDisplayName())
          .append(',');
    }
    if (builder.length() > 0) builder.deleteCharAt(builder.length() - 1);

    sender.sendMessage(
        new TextComponent(
            "§ePlayers §7(" + queue.getPlayers().size() + "): " + builder.toString()));

    StringBuilder handlerBuilder = new StringBuilder();
    for (String handler : queue.getHandlers()) {
      handlerBuilder.append(handler).append(',').append(' ');
    }
    if (handlerBuilder.length() > 1) {
      handlerBuilder.delete(handlerBuilder.length() - 2, handlerBuilder.length() - 1);
    }

    sender.sendMessage(
        new TextComponent(
            "§eHandlers §7(" + queue.getHandlers().size() + "): " + handlerBuilder.toString()));

    if (queue instanceof TPriorityQueue) {
      sender.sendMessage(new TextComponent("§eDelay: §7" + queue.getDelay()));
    }
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
