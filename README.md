# TQueue
A simple queue system in minecraft

## What is it for
This plugin provides partly async queue system.  
For example, if you have the server which is shut down, players can join a queue associated with a server and the will be joined when server stands up.
Queue also supports many servers for one queue.

## Quick Demo
![Alt Text](https://theseems.ru/tqueue/compressed.gif)

## Commands
/queue list - list of all registered queues  
/queue info <queue> - info about specific queue  
/queue join <queue> - join a specific queue  
/queue leave - leave all queues. // TODO: Make command to leave specific queue  
  
/queue add <queue_name> <[server, priotity]> - Add new queue with servers.
Example: /queue add hubs hub1,0 hub2,1 hub3,0, hub4 (players will join hub2 until it's full if there is no special destination container on a queue)  
  
/queue remove <queue_name> - remove a queue  
/queue clear <queue_name> - clears a queue (removes all players from it)  
/queue kick <player> <queue_name> - kick a player from a queue  

// TODO: add more commands  

## API
// TODO: Add API interaction examples
