package me.theseems.tqueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TPriorityQueueTest {
  public static void main(String[] args) {
    QueueAPI.setService(Executors.newFixedThreadPool(5));

    TPriorityQueue queue = new TPriorityQueue(200) {
      @Override
      public Integer getPriority(UUID player) {
        return 0;
      }

      @Override
      public String getName() {
        return "test";
      }
    };

    List<UUID> players = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      UUID uuid = UUID.randomUUID();
      players.add(uuid);
      queue.add(uuid);
    }

    System.out.println("At start: " + queue.getPlayers());

    queue.addDestination(new Destination() {
      @Override
      public Future<Verdict> query(UUID user) {
        boolean random = new Random().nextBoolean();
        if (random)
          return CompletableFuture.completedFuture(Verdict.FORBIDDEN);
        return CompletableFuture.completedFuture(Verdict.OK);
      }

      @Override
      public int getPriority() {
        return 0;
      }

      @Override
      public String getName() {
        return "first";
      }
    });

    queue.addHandler(
        new QueueHandler() {
          @Override
          public boolean apply(UUID player, Destination destination, Verdict verdict) {
            System.out.println("4 " + player + " @ " + destination.getName() + " -> " + verdict);
            System.out.println("Now there " + queue.getPlayers());
            if (!verdict.ok) {
              return false;
            } else {
              queue.remove(player);
              return true;
            }
          }

          @Override
          public void join(UUID player) {
            System.out.println(">>>>>>>>> " + player);
          }

          @Override
          public void leave(UUID player) {
            System.out.println("<<<<<<<<< " + player);
          }

          @Override
          public String getName() {
            return "test";
          }
        });
  }
}
