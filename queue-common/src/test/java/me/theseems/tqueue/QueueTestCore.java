package me.theseems.tqueue;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Logger;

public class QueueTestCore {
    public static Map<UUID, Integer> priority;

    public static void setFor(Queue queue) {
        queue.setDestinations(
                new PriorityMappedContainer<>() {
                    @Override
                    public int getPriority(@NotNull String name, @NotNull Destination value) {
                        return value.getPriority();
                    }

                    @Override
                    public void add(Destination value) {
                        this.add(value.getName(), value);
                    }
                });

        queue.setHandlers(
                new SimpleMappedContainer<>() {
                    @Override
                    public void add(QueueHandler value) {
                        this.add(value.getName(), value);
                    }
                });

        queue.setPriorities(
                new PriorityMappedContainer<>() {
                    @Override
                    public int getPriority(@NotNull UUID name, @NotNull Integer value) {
                        return priority.get(name);
                    }

                    @Override
                    public void add(Integer value) {
                        throw new UnsupportedOperationException(
                                "You must specify the key. Adding priority nowhere is not supported");
                    }
                });
    }

    public static void setup() {
        priority = new TreeMap<>();
        new QueueAPI(5);
        QueueAPI.setLogManager(
                new QueueLogManager() {
                    @Override
                    public Logger prefix(String name) {
                        return Logger.getLogger(name);
                    }

                    @Override
                    public Logger get() {
                        return Logger.getGlobal();
                    }
                });
        QueueAPI.setQueueManager(
                new TQueueManager() {
                    @Override
                    public Queue make(@NotNull String name, int delay) {
                        Queue queue =
                                new TPriorityQueue(delay) {

                                    @Override
                                    public void add(UUID player) {
                                        super.add(player);
                                    }

                                    @Override
                                    public String getName() {
                                        return "test-" + name;
                                    }
                                };

                        setFor(queue);
                        return queue;
                    }
                });
  }
}
