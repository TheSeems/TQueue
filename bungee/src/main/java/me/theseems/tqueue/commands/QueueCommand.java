package me.theseems.tqueue.commands;

import me.theseems.tqueue.TQueueBungeePlugin;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class QueueCommand extends Command {
  private static Map<String, SubCommand> subs;

  public QueueCommand() {
    super("queue");
    subs = new HashMap<>();

    attach("add", new QueueAddSub());
    attach("create", new QueueCreateSub());
    attach("info", new QueueInfoSub());
    attach("leave", new QueueLeaveSub());
    attach("list", new QueueListSub());
    attach("try", new QueueTrySub());
    attach("remove", new QueueRemoveSub());
    attach("kick", new QueueKickSub());
    attach("clear", new QueueClearSub());
    attach("join", new QueueJoinSub());
  }

  public void attach(String name, SubCommand subCommand) {
    subs.put(name, subCommand);
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    if (args.length == 0
        || !subs.containsKey(args[0])
        || sender instanceof ProxiedPlayer
            && !sender.hasPermission(subs.get(args[0]).getPermission())) {
      sender.sendMessage(
          new TextComponent(
              "§3§lTQueue §fby TheSeems<me@theseems.ru> "
                  + "§7v"
                  + TQueueBungeePlugin.getPlugin().getDescription().getVersion()));
      return;
    }

    String next = args[0];
    args = Arrays.copyOfRange(args, 1, args.length);
    try {
      SubCommand command = subs.get(next);
      if (!command.allowConsole() && !(sender instanceof ProxiedPlayer)) {
        sender.sendMessage(new TextComponent("§7Sorry.. but§c command is unavailable for you"));
        return;
      }

      command.pass(sender, args);
    } catch (QueueCommandUtils.PlayerNotFoundException e) {
      sender.sendMessage(new TextComponent("§cPlayer §7'" + e.getName() + "'§c not found"));
    } catch (QueueCommandUtils.QueueNotFoundException e) {
      sender.sendMessage(new TextComponent("§cQueue §7'" + e.getName() + "'§c not found"));
    } catch (Exception e) {
      sender.sendMessage(
          new TextComponent(
              "§7Sorry.. we have a problem executing this command: §c" + e.getMessage()));
      sender.sendMessage(new TextComponent("§7Try again later..."));
      e.printStackTrace();
    }
  }
}
