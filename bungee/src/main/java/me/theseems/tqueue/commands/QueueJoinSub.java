package me.theseems.tqueue.commands;

import me.theseems.tqueue.Queue;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class QueueJoinSub implements SubCommand {
  @Override
  public void pass(CommandSender sender, String[] args) {
    if (args.length == 0) {
      sender.sendMessage(new TextComponent("§cYou have to specify name of queue"));
      return;
    }

    Queue queue = QueueCommandUtils.requireQueue(args[0]);
    ProxiedPlayer player = QueueCommandUtils.requirePlayer(sender.getName());

    queue.add(player.getUniqueId());
    sender.sendMessage(new TextComponent("§aYou have just joined queue §7'" + queue.getName() + "'"));
  }

  @Override
  public String getPermission() {
    return "tqueue.join";
  }

  @Override
  public boolean allowConsole() {
    return false;
  }
}
