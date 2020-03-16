package me.theseems.tqueue.commands;

import me.theseems.tqueue.Queue;
import me.theseems.tqueue.QueueHandler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class QueueAddHandlerSub implements SubCommand {
  @Override
  public void pass(CommandSender sender, String[] args) {
    if (args.length < 2) {
      sender.sendMessage(new TextComponent("§cSpecify the queue and the handler name"));
      return;
    }

    Queue queue = QueueCommandUtils.requireQueue(args[0]);
    QueueHandler handler = QueueCommandUtils.requireHandlerForQueue(queue, args[1]);
    if (queue.getHandlers().keys().contains(args[1])) {
      sender.sendMessage(new TextComponent("§cQueue §7'" + queue.getName() + "'§c already has handler §7'" + args[1] + "'"));
      return;
    }

    queue.getHandlers().add(handler);
    QueueInfoSub.sendInfo(sender, queue);
  }

  @Override
  public String getPermission() {
    return "queue.add.handler";
  }
}
