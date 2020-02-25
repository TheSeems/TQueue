package me.theseems.tqueue;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class QueueJoinLocalCommand implements CommandExecutor {
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!sender.hasPermission("tqueue.local.join")
        || args.length == 0
        || !sender.hasPermission("tqueue.local.join." + args[0])) return false;

    if (sender instanceof ConsoleCommandSender) {
      sender.sendMessage("Not available for Console.");
      return false;
    }

    TQueueSpigot.getCommunicator().join(((Player) sender).getUniqueId(), args[0]);
    return true;
  }
}