package me.theseems.tqueue;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public class QueueInfoLocalCommand implements CommandExecutor {
  private String shrink(String string, int pos) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < Math.min(pos, string.length()); i++) {
      builder.append(string.charAt(i));
    }

    if (pos < string.length())
      builder.append("...");

    return builder.toString();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!sender.hasPermission("tqueue.local.info")
        || args.length == 0
        || !sender.hasPermission("tqueue.local.info." + args[0])) return false;

    StringBuilder builder = new StringBuilder();
    Collection<UUID> uuidCollection = TQueueSpigot.getCommunicator().getPlayers(args[0]);
    uuidCollection.forEach(uuid -> {
      Player player = Bukkit.getPlayer(uuid);
      if (player == null)
        builder.append(shrink(uuid.toString(), 5));
      else
        builder.append(player.getName());
    });

    sender.sendMessage("§6Queue §7'" + args[0] + "'");
    sender.sendMessage("§7(" + uuidCollection.size() + "): " + builder.toString());
    return true;
  }
}
