package me.theseems.tqueue.commands;

import me.theseems.tqueue.Queue;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class QueueRemoveHandlerSub implements SubCommand {
  @Override
  public void pass(CommandSender sender, String[] args) {
    if (args.length < 2) {
      sender.sendMessage(new TextComponent("§cSpecify the queue and handler to remove"));
      return;
    }

    Queue queue = QueueCommandUtils.requireQueue(args[0]);
    if (!queue.getHandlers().keys().contains(args[1])) {
      sender.sendMessage(new TextComponent("§cThere is no handler §7'" + args[1] + "'§c in the queue §7'" + queue.getName() + "'"));
      return;
    }

    queue.getHandlers().remove(args[1]);
    QueueInfoSub.sendInfo(sender, queue);
  }

  @Override
  public String getPermission() {
    return "queue.remove.handlers";
  }
}
