package me.theseems.tqueue.commands;

import me.theseems.tqueue.TQueueBungeePlugin;
import me.theseems.tqueue.Verdict;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.CompletableFuture;

public class QueueTrySub implements SubCommand {
  @Override
  public void pass(CommandSender sender, String[] args) {
    if (args.length == 0) {
      sender.sendMessage(
          new TextComponent(
              "§cSpecify the name of server to test (and optionally player in first argument to test for them)"));
      return;
    }

    String playerName;
    String serverName;
    if (args.length >= 2) {
      playerName = args[0];
      serverName = args[1];
    } else {
      playerName = sender.getName();
      serverName = args[0];
    }

    ProxiedPlayer player = QueueCommandUtils.requirePlayer(playerName);

    ServerInfo info = TQueueBungeePlugin.getProxyServer().getServerInfo(serverName);
    if (info == null) {
      sender.sendMessage(new TextComponent("§cServer §7'" + serverName + "'§c is not found"));
      return;
    }

    sender.sendMessage(new TextComponent("§eSending request..."));
    CompletableFuture<Verdict> future =
        TQueueBungeePlugin.getMessenger().requestUser(serverName, player.getUniqueId());

    future
        .thenAccept(
            result ->
                sender.sendMessage(
                    new TextComponent("§aRequest succeed: " + result.ok + " " + result.desc)))
        .exceptionally(
            throwable -> {
              sender.sendMessage(new TextComponent("§cRequest failed... " + throwable));
              return null;
            });
  }

  @Override
  public String getPermission() {
    return "queue.try";
  }
}
