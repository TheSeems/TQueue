package me.theseems.tqueue.commands;

import me.theseems.tqueue.Queue;
import me.theseems.tqueue.QueueAPI;
import me.theseems.tqueue.TQueueBungeePlugin;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Optional;

public class QueueAddSub implements SubCommand {
  @Override
  public void pass(CommandSender sender, String[] args) {
    if (args.length < 2) {
      sender.sendMessage(new TextComponent("§cYou have to specify the player and queue's name"));
      return;
    }

    String playerName = args[0];
    ProxiedPlayer proxiedPlayer = TQueueBungeePlugin.getProxyServer().getPlayer(playerName);
    if (proxiedPlayer == null) {
      sender.sendMessage(new TextComponent("§cPlayer §7'" + playerName + "'§c is not found"));
      return;
    }

    String qName = args[1];
    Optional<Queue> queueOptional = QueueAPI.getQueueManager().getQueue(qName);
    if (!queueOptional.isPresent()) {
      sender.sendMessage(new TextComponent("§cThere's no queue with name §7'" + qName + "'"));
      return;
    }

    queueOptional.get().add(proxiedPlayer.getUniqueId());
  }

  @Override
  public String getPermission() {
    return "queue.add";
  }
}
