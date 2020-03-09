package me.theseems.tqueue.commands.test;

import me.theseems.tqueue.Queue;
import me.theseems.tqueue.QueueMappedContainer;
import me.theseems.tqueue.commands.QueueCommandUtils;
import me.theseems.tqueue.commands.SubCommand;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Optional;

public class QueueTestGrabSub implements SubCommand {
    public static QueueMappedContainer getContainer(String[] args) {
        Queue queue = QueueCommandUtils.requireQueue(args[0]);
        switch (args[1]) {
            case "priorities":
                return queue.getPriorities();
            case "destinations":
                return queue.getDestinations();
            case "handlers":
                return queue.getHandlers();
            default:
                throw new IllegalStateException(
                        "No container for queue '" + queue.getName() + "': " + args[1]);
        }
    }

    @Override
    public void pass(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(
                    new TextComponent(
                            "§cSelect queue, key and container to grab that key from: destination, priority, handlers"));
            return;
        }

        QueueMappedContainer container = getContainer(args);
        Optional<?> optional = container.get(args[2]);
        if (!optional.isPresent()) {
            sender.sendMessage(new TextComponent("§cNothing found for key §7'" + args[2] + "'"));
        } else {
            sender.sendMessage(new TextComponent("§aFound: §f" + optional.get()));
        }
    }

    @Override
    public String getPermission() {
        return "queue.test.grab";
    }
}
