package me.theseems.tqueue;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleQueueHandlerManager implements QueueHandlerManager {
    private Map<String, QueueHandlerFactory> factoryMap;

    public SimpleQueueHandlerManager() {
        factoryMap = new ConcurrentHashMap<>();
    }


    @Override
    public Optional<QueueHandler> requestFor(String name, Queue queue) {
        if (!factoryMap.containsKey(name))
            return Optional.empty();
        return Optional.of(factoryMap.get(name).produce(queue));
    }

    @Override
    public void register(QueueHandlerFactory factory) {
        factoryMap.put(factory.getName(), factory);
    }

    @Override
    public void unregister(String name) {
        factoryMap.remove(name);
    }

    @Override
    public Collection<String> getFactories() {
        return factoryMap.keySet();
    }
}
