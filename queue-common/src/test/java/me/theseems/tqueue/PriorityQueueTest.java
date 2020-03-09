package me.theseems.tqueue;

import org.junit.Test;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;

import static me.theseems.tqueue.QueueTestCore.setup;

public class PriorityQueueTest {
    private static int THRESHOLD_PLAYERS = 10000;
    private static final int THRESHOLD_DESTINATIONS = 100;
    private Queue queue;

    {
        setup();
        queue = QueueAPI.getQueueManager().make("TPriorityQueue", 0);
    for (int i = 0; i < THRESHOLD_DESTINATIONS; i++) {
      Destination destination =
          new Destination() {
              @Override
              public CompletableFuture<Verdict> query(UUID user) {
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

        queue.getDestinations().add(destination);
    }
    }

    @Test(timeout = 20 * 60 * 1000L)
    public void testDelay() throws InterruptedException, ExecutionException, TimeoutException {
        THRESHOLD_PLAYERS = 2;
        queue.getDestinations().clear();
        queue
                .getDestinations()
                .add(
                        new Destination() {
                            @Override
                            public CompletableFuture<Verdict> query(UUID user) {
                                return CompletableFuture.supplyAsync(
                                        () -> {
                                            try {
                                                TimeUnit.MILLISECONDS.sleep(new SecureRandom().nextInt(1010));
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            return Verdict.OK;
                                        });
                            }

                            @Override
                            public int getPriority() {
                                return 0;
                            }

                            @Override
                            public String getName() {
                                return "test-delayed";
                            }
                        });

        testDummies();
    }

    @Test(timeout = 20 * 60 * 1000L)
    public void testDummies() throws InterruptedException, ExecutionException, TimeoutException {
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
        queue
                .getHandlers()
                .add(
                        "test",
                        new QueueHandler() {
                            @Override
                            public String getName() {
                                return "test";
                            }

                            @Override
                            public void onJoin(UUID player) {
                                joined.add(player);
                            }

                            @Override
                            public void onLeave(UUID player) {
                                left.add(player);
                                if (left.size() == joined.size()) wait.complete(null);
                            }

                            @Override
                            public boolean onApply(UUID player, Destination destination, Verdict verdict) {
                                if (verdict != Verdict.TIMED_OUT) queue.remove(player);
                                return true;
                            }
                        });

        dummies.forEach(queue::add);
        System.out.println(queue.getPlayers());
        wait.get(20, TimeUnit.SECONDS);
        QueueTestCore.priority.clear();
    }
}
