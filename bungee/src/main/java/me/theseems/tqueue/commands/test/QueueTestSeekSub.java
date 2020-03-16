package me.theseems.tqueue.commands.test;

import me.theseems.tqueue.QueueMappedContainer;
import me.theseems.tqueue.commands.SubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class QueueTestSeekSub implements SubCommand {
  @Override
  public void pass(CommandSender sender, String[] args) {
    if (args.length < 2) {
      sender.sendMessage(
        new TextComponent(
          "§cSpecify the queue and the container: priorities, destinations or handlers"));
      return;
    }

    QueueMappedContainer container = QueueTestGrabSub.getContainer(args);
    sender.sendMessage(new TextComponent("§7Found (" + container.values().size() + ")"));
    container
      .keys()
      .forEach(
        o -> sender.sendMessage(new TextComponent("§7" + o.toString() + "§7 -> §6" + container.get(o))));
  }

  @Override
  public String getPermission() {
    return "queue.seek";
  }
}
