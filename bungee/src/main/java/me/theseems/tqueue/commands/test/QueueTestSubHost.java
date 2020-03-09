package me.theseems.tqueue.commands.test;

import me.theseems.tqueue.commands.QueueCommand;
import me.theseems.tqueue.commands.SubCommand;
import me.theseems.tqueue.commands.SubHost;
import net.md_5.bungee.api.CommandSender;

public class QueueTestSubHost extends SubHost implements SubCommand {
  public QueueTestSubHost() {
    super("queue:test");
    attach("try", new QueueTestReachSub());
    attach("grab", new QueueTestGrabSub());
    attach("seek", new QueueTestSeekSub());
    attach("save", new QueueTestSaveConfigSub());
    attach("hfactories", new QueueTestHandlerFactoriesSub());
  }

  @Override
  public void pass(CommandSender sender, String[] args) {
    execute(sender, args);
  }

  @Override
  public String getPermission() {
    return "tqueue.test";
  }

  @Override
  public void onNotFound(CommandSender sender) {
    QueueCommand.sendBanner(sender);
  }

  @Override
  public void onPermissionLack(CommandSender sender, String node) {
    QueueCommand.sendBanner(sender);
  }

  @Override
  public void execute(CommandSender commandSender, String[] strings) {
    super.propagate(commandSender, strings);
  }
}
