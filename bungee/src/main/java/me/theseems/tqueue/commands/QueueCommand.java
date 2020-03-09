package me.theseems.tqueue.commands;

import me.theseems.tqueue.TQueueBungeePlugin;
import me.theseems.tqueue.commands.test.QueueTestSubHost;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class QueueCommand extends SubHost {

  public QueueCommand() {
    super("queue");

    attach("add", new QueueAddSub());
    attach("create", new QueueCreateSub());
    attach("info", new QueueInfoSub());
    attach("leave", new QueueLeaveSub());
    attach("list", new QueueListSub());
    attach("remove", new QueueRemoveSub());
    attach("kick", new QueueKickSub());
    attach("clear", new QueueClearSub());
    attach("join", new QueueJoinSub());
    attach("test", new QueueTestSubHost());
    attach("addh", new QueueAddHandlerSub());
    attach("removeh", new QueueRemoveHandlerSub());
  }

  public static void sendSubs(CommandSender sender, SubHost host) {
    AtomicInteger count = new AtomicInteger();
    StringBuilder builder = new StringBuilder();
    host.subs.forEach(
            (s, subCommand) -> {
              if ((!(sender instanceof ProxiedPlayer)) && !subCommand.allowConsole()) return;
              if (!sender.hasPermission(subCommand.getPermission())) return;
              builder.append(s).append(',').append(' ');
              count.getAndIncrement();
            });

    if (builder.length() != 0) {
      builder.delete(builder.length() - 2, builder.length() - 1);
      sender.sendMessage(
              new TextComponent("§6You can perform those sub commands (" + count + "):"));
      sender.sendMessage(new TextComponent("§7" + builder.toString()));
    } else {
      sender.sendMessage(new TextComponent("§7There are no sub commands for you to use"));
    }
  }

  public static void sendBanner(CommandSender sender) {
    sender.sendMessage(
            new TextComponent(
                    "§3§lTQueue §fby TheSeems<me@theseems.ru> "
                            + "§7v"
                            + TQueueBungeePlugin.getPlugin().getDescription().getVersion()));

  }

  @Override
  public void onNotFound(CommandSender sender) {
    sendBanner(sender);
  }

  @Override
  public void onPermissionLack(CommandSender sender, String node) {
    sendBanner(sender);
  }

  @Override
  public void execute(CommandSender commandSender, String[] strings) {
    if (Objects.equals(strings[0], "subs")) {
      sendSubs(commandSender, this);
      return;
    }

    propagate(commandSender, strings);
  }
}
