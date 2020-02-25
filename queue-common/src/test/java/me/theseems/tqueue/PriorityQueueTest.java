package me.theseems.tqueue;

import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;

import static me.theseems.tqueue.QueueTestCore.setup;

public class PriorityQueueTest {
  private static final int THRESHOLD_PLAYERS = 10000;
  private static final int THRESHOLD_DESTINATIONS = 100;


  @Test(timeout = 20 * 1000L)
  public void test() throws InterruptedException, ExecutionException, TimeoutException {
    setup();
    Queue queue = QueueAPI.getQueueManager().make("TPriorityQueue", 10);
    for (int i = 0; i < THRESHOLD_DESTINATIONS; i++) {
      Destination destination =
          new Destination() {
            @Override
            public Future<Verdict> query(UUID user) {
              return CompletableFuture.completedFuture(Verdict.OK);
            }

            @Override
            public int getPriority() {
              return 0;
            }

            @Override
            public String getName() {
              return UUID.randomUUID().toString();
            }
          };

      queue.addDestination(destination);
    }

    List<UUID> dummies = new ArrayList<>();
    for (int i = 0; i < THRESHOLD_PLAYERS; i++) {
      UUID current = UUID.randomUUID();
      int randomPriority = new Random().nextInt();
      QueueTestCore.priority.put(current, randomPriority);
      dummies.add(current);
    }

    Set<UUID> joined = new ConcurrentSkipListSet<>();
    Set<UUID> left = new ConcurrentSkipListSet<>();

    CompletableFuture<Void> wait = new CompletableFuture<>();
    queue.addHandler(
        new QueueHandler() {
          @Override
          public String getName() {
            return "test";
          }

          @Override
          public void join(UUID player) {
            joined.add(player);
          }

          @Override
          public void leave(UUID player) {
            left.add(player);
            if (left.size() == joined.size())
              wait.complete(null);
          }

          @Override
          public boolean apply(UUID player, Destination destination, Verdict verdict) {
            queue.remove(player);
            return true;
          }
        });

    dummies.forEach(queue::add);
    wait.get(20, TimeUnit.SECONDS);
  }
}
