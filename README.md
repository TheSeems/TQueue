# TQueue
A simple queue system in minecraft  
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/596992c399e9467fbc1910ca84b6e294)](https://app.codacy.com/manual/TheSeems/TQueue?utm_source=github.com&utm_medium=referral&utm_content=TheSeems/TQueue&utm_campaign=Badge_Grade_Dashboard)
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

## Installation
From here: https://github.com/TheSeems/TQueue/releases/tag/0.1D  
Download TQueueBungee-...jar and put it in the bungeecord plugins folder  
Download TQueueSpigot-...jar and put it in the spigot/bukkit plugins folder  

Configure your redis server and fill in configs for both bungee and spigot.  

## Sample configs
For spigot (plugins/TQueue/config.json):  
https://gist.github.com/TheSeems/ed180d964112f2e78f95a460d7d81c65  

For bungee (plugins/TQueue/config.json):  
https://gist.github.com/TheSeems/44d2bd4344002277aa2169a61e8fd86d  

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
