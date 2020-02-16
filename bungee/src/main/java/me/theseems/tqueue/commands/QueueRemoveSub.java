package me.theseems.tqueue.commands;

import me.theseems.tqueue.QueueAPI;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class QueueRemoveSub implements SubCommand {
  @Override
  public void pass(CommandSender sender, String[] args) {
    if (args.length == 0) {
      sender.sendMessage(new TextComponent("§cYou have to specify the queue name"));
      return;
    }

    QueueAPI.getQueueManager().unregister(QueueCommandUtils.requireQueue(args[0]));
    sender.sendMessage(new TextComponent("§aSend shutdown signal to queue §7'" + args[0] + "'"));
  }

  @Override
  public String getPermission() {
    return "tqueue.remove";
  }
}
