package me.theseems.tqueue.commands.test;

import me.theseems.tqueue.TQueueBungeePlugin;
import me.theseems.tqueue.Verdict;
import me.theseems.tqueue.commands.QueueCommandUtils;
import me.theseems.tqueue.commands.SubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class QueueTestReachSub implements SubCommand {
    @Override
    public void pass(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(
                    new TextComponent(
                            "§cSpecify the (optionally player) and name of server to test"));
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

    Verdict result;
    try {
        result = future.get(5, TimeUnit.SECONDS);
        sender.sendMessage(
                new TextComponent(
                        "§6Response received: §7" + result.ok + " " + result.desc + " §7(" + result + ": " + result.desc + ")"));
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      sender.sendMessage(new TextComponent("§cRequest failed: §7" + e.getMessage()));
    }
  }

  @Override
  public String getPermission() {
      return "queue.test.try";
  }
}
