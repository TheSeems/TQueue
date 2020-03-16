package me.theseems.tqueue.commands;

import me.theseems.tqueue.Queue;
import me.theseems.tqueue.QueueAPI;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Collection;

public class QueueListSub implements SubCommand {
  @Override
  public void pass(CommandSender sender, String[] args) {
    Collection<Queue> queues = QueueAPI.getQueueManager().getQueues();
    sender.sendMessage(new TextComponent("§7There are " + queues.size() + " queue(s)"));
    for (Queue queue : queues) {
      sender.sendMessage(
        new TextComponent(
          "§7Queue §6'"
            + queue.getName()
            + "'§7 has "
            + queue.getDestinations().keys().size()
            + " destination(s), "
            + queue.getHandlers().keys().size()
            + " handler(s) and §6"
            + queue.getPlayers().size()
            + "§7 players"));
    }
  }

  @Override
  public String getPermission() {
    return "tqueue.list";
  }
}
