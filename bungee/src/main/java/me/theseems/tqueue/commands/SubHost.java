package me.theseems.tqueue.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public abstract class SubHost extends Command {
  Map<String, SubCommand> subs;

  public SubHost(String name) {
    super(name);
    subs = new HashMap<>();
  }

  public void attach(String name, SubCommand subCommand) {
    subs.put(name, subCommand);
  }

  public abstract void onNotFound(CommandSender sender);

  public abstract void onPermissionLack(CommandSender sender, String node);

  public void onError(CommandSender sender, Exception e) {
    sender.sendMessage(
      new TextComponent(
        "§7Sorry.. we have a problem executing this command: §c" + e.getMessage()));
    sender.sendMessage(new TextComponent("§7Try again later..."));
    e.printStackTrace();
  }

  public void propagate(CommandSender sender, String[] args) {
    if (args.length == 0 || !subs.containsKey(args[0])) {
      onNotFound(sender);
      return;
    }

    String requiredPermission = subs.get(args[0]).getPermission();
    if (sender instanceof ProxiedPlayer && !sender.hasPermission(requiredPermission)) {
      onPermissionLack(sender, requiredPermission);
    }

    String next = args[0];
    args = Arrays.copyOfRange(args, 1, args.length);
    try {
      SubCommand command = subs.get(next);
      if (!command.allowConsole() && !(sender instanceof ProxiedPlayer)) {
        sender.sendMessage(new TextComponent("§7Sorry.. but§c command is unavailable for you. (Not Player)"));
        return;
      }

      command.pass(sender, args);
    } catch (QueueCommandUtils.PlayerNotFoundException e) {
      sender.sendMessage(new TextComponent("§cPlayer §7'" + e.getName() + "'§c not found"));
    } catch (QueueCommandUtils.QueueNotFoundException e) {
      sender.sendMessage(new TextComponent("§cQueue §7'" + e.getName() + "'§c not found"));
    } catch (QueueCommandUtils.InsufficientPermissionsException e) {
      onPermissionLack(sender, e.getPermission());
    } catch (QueueCommandUtils.HandlerNotFoundException e) {
      sender.sendMessage(new TextComponent("§cHandler §7'" + e.getName() + "'§c not found"));
    } catch (Exception e) {
      onError(sender, e);
    }
  }
}
