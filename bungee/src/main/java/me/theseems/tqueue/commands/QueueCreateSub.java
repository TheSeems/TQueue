package me.theseems.tqueue.commands;

import me.theseems.tqueue.Queue;
import me.theseems.tqueue.QueueAPI;
import me.theseems.tqueue.ServerDestination;
import me.theseems.tqueue.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Optional;

import static me.theseems.tqueue.commands.QueueInfoSub.sendInfo;

public class QueueCreateSub implements SubCommand {
  @Override
  public void pass(CommandSender sender, String[] args) {
    if (args.length < 2) {
      sender.sendMessage(
        new TextComponent(
          "§cYou have to specify the queue's name and destination servers"));
      return;
    }

    String name = args[0];
    Optional<Queue> optionalQueue = QueueAPI.getQueueManager().getQueue(name);
    if (optionalQueue.isPresent()) {
      sendInfo(sender, optionalQueue.get());
      sender.sendMessage(
        new TextComponent("§6§l↑ §cQueue with name §7'" + name + "'§c already exist"));
      return;
    }

    Queue queue = QueueAPI.getQueueManager().make(name, 600);

    for (int i = 1; i < args.length; i++) {
      String newDestination = args[i];
      String[] list = newDestination.split(",");
      if (list.length < 2) {
        sender.sendMessage(new TextComponent("§cWrong format!§7 Expected <name>,<priority>"));
        sender.sendMessage(
          new TextComponent("§8Example /queue create first lobby1,10 lobby2,20"));
        return;
      }

      int priority;
      try {
        priority = Integer.parseInt(list[1]);
      } catch (NumberFormatException e) {
        sender.sendMessage(
          new TextComponent("§cCannot read a number§7 from this: '" + list[1] + "'"));
        return;
      }

      queue.addDestination(new ServerDestination(list[0], priority));
    }

    queue.addHandler(Utils.getDefaultHandlerFor(queue));
    QueueAPI.getQueueManager().register(queue);
    sendInfo(sender, queue);
  }

  @Override
  public String getPermission() {
    return "queue.create";
  }
}
