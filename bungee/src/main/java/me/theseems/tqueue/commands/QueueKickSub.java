package me.theseems.tqueue.commands;

import me.theseems.tqueue.Queue;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class QueueKickSub implements SubCommand {
  @Override
  public void pass(CommandSender sender, String[] args) {
    if (args.length < 2) {
      sender.sendMessage(new TextComponent("§cSpecify name of queue and player to kick"));
    }

    Queue queue = QueueCommandUtils.requireQueue(args[0]);
    ProxiedPlayer player = QueueCommandUtils.requirePlayer(args[0]);

    queue.remove(player.getUniqueId());
    sender.sendMessage(
        new TextComponent(
            "§aKicked player §7'"
                + player.getName()
                + "'§a from queue §7'"
                + queue.getName()
                + "'"));
  }

  @Override
  public String getPermission() {
    return "tqueue.kick";
  }
}
