package me.theseems.tqueue.commands;

import net.md_5.bungee.api.CommandSender;

public interface SubCommand {
  /**
   * Pass the sub command
   *
   * @param sender of sub command
   * @param args   of sub command
   */
  void pass(CommandSender sender, String[] args);

  /**
   * Get permission to use the sub
   *
   * @return permission
   */
  default String getPermission() {
    return "tqueue.use";
  }

  /**
   * Do a sub command allows console to use ut
   *
   * @return allow console to use a sub
   */
  default boolean allowConsole() {
    return true;
  }
}
