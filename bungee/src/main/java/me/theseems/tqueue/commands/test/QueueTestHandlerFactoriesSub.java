package me.theseems.tqueue.commands.test;

import me.theseems.tqueue.QueueAPI;
import me.theseems.tqueue.commands.QueueInfoSub;
import me.theseems.tqueue.commands.SubCommand;
import net.md_5.bungee.api.CommandSender;

public class QueueTestHandlerFactoriesSub implements SubCommand {
  @Override
  public void pass(CommandSender sender, String[] args) {
    sender.sendMessage(
            QueueInfoSub.namedCollection(
                    "§7Handler factories ({0}): ", QueueAPI.getHandlerManager().getFactories()));
  }

  @Override
  public String getPermission() {
    return "queue.test.factories.handlers";
  }
}
