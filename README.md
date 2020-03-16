# TQueue
A simple queue system in minecraft  
[![Build Status](https://travis-ci.com/TheSeems/TQueue.svg?branch=master)](https://travis-ci.com/TheSeems/TQueue)

## What is it for
This plugin provides partly async queue system.  
For example, if you have the server which is shut down, players can join a queue associated with a server and the will be joined when server stands up.
Queue also supports many servers for one queue.

## Quick Demo
![Alt Text](https://theseems.ru/tqueue/compressed.gif)

## Commands
`/queue list` - list of all registered queues  
`/queue add <player> <queue>` - add a player to a queue
`/queue info <queue>` - info about specific queue  
`/queue join <queue>` - join a specific queue  
`/queue leave` - leave all queues. `// TODO: Make command to leave specific queue`  
  
`/queue create <queue> <[server, priotity]>` - add new queue with servers.
Example: `/queue add hubs hub1,0 hub2,1 hub3,0 hub4,0`  
Players will join hub2 until it's full if there is no special destination container on a queue  
  
`/queue remove <queue>` - remove a queue  
`/queue clear <queue>` - clears a queue (removes all players from it)  
`/queue kick <player> <queue>` - kick a player from a queue  
`/queue addh <handler>` - add handler to a queue
`/queue removeh <handler>` - remove handler from a queue

## API
### Creating a queue (with registration)
```Java
    Queue queue = QueueAPI.getQueueManager().make("<name>", 100);
    QueueAPI.getQueueManager().register(queue);
```

### Getting a queue
```Java
    QueueAPI.getQueueManager().getQueue("<name>").ifPresent(queue -> {
      // Do something incredible with a queue
    });
```

### Adding a single handler
```Java
queue.getHandlers().add(new QueueHandler() {
      @Override
      public String getName() {
        return "<handler name>";
      }

      @Override
      public boolean onApply(UUID player, Destination destination, Verdict verdict) {}

      @Override
      public void onJoin(UUID player) {}

      @Override
      public void onLeave(UUID player) {}
    });
```
Or you can produce some handler from a factory
```Java
    Queue queue;
    QueueAPI.getHandlerManager()
      .requestFor("<handler name>", queue)
      .ifPresent(queueHandler -> queue.getHandlers().add(queueHandler));
```

### Registering a handler factory
```Java
    QueueAPI.getHandlerManager().register(new QueueHandlerFactory() {
      @Override
      public QueueHandler produce(Queue queue) {
        // Produce handler for specific queue
      }

      @Override
      public String getName() {
        return "<handler name>";
      }
    });
```
