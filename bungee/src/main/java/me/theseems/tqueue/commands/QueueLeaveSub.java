package me.theseems.tqueue.commands;

import me.theseems.tqueue.Queue;
import me.theseems.tqueue.QueueAPI;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class QueueLeaveSub implements SubCommand {
  @Override
  public void pass(CommandSender sender, String[] args) {
    for (Queue queue : QueueAPI.getQueueManager().getQueues()) {
      queue.remove(((ProxiedPlayer) sender).getUniqueId());
    }
  }

  @Override
  public String getPermission() {
    return "queue.leave";
  }

  @Override
  public boolean allowConsole() {
    return false;
  }
}
