package me.theseems.tqueue.commands;

import me.theseems.tqueue.Queue;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.UUID;

public class QueueClearSub implements SubCommand {
  @Override
  public void pass(CommandSender sender, String[] args) {
    if (args.length == 0) {
      sender.sendMessage(new TextComponent("§cYou have to specify the queue name"));
      return;
    }

    Queue queue = QueueCommandUtils.requireQueue(args[0]);
    int kickedCounter = 0;
    for (UUID player : queue.getPlayers()) {
      queue.remove(player);
      kickedCounter++;
    }

    sender.sendMessage(
      new TextComponent(
        "§aKicked §7" + kickedCounter + "§a player(s) from queue §7'" + queue.getName() + "'"));
  }

  @Override
  public String getPermission() {
    return "tqueue.clear";
  }
}
