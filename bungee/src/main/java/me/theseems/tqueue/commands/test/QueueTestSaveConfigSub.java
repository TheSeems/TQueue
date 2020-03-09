package me.theseems.tqueue.commands.test;

import me.theseems.tqueue.TQueueBungeePlugin;
import me.theseems.tqueue.commands.SubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

public class QueueTestSaveConfigSub implements SubCommand {
    @Override
    public void pass(CommandSender sender, String[] args) {
        TQueueBungeePlugin.saveConfig();
        sender.sendMessage(new TextComponent("§aOK"));
    }

    @Override
    public String getPermission() {
        return "queue.test.config.save";
    }
}
