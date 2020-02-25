package me.theseems.tqueue;

import org.junit.Test;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.*;

import static me.theseems.tqueue.QueueTestCore.setup;
import static org.junit.Assert.assertTrue;

public class TimeLimitHandlerTest {
  @Test
  public void timeHandler() throws InterruptedException, ExecutionException, TimeoutException {
    setup();
    Queue queue = QueueAPI.getQueueManager().make("TimeHandler", 10);
    UUID player = UUID.randomUUID();

    CompletableFuture<Boolean> wait = new CompletableFuture<>();
    Date joined = new Date();
    queue.addHandler(
        new TimeLimitHandler(100) {
          @Override
          public void timedOut(UUID player) {
            wait.complete(
                ChronoUnit.MILLIS.between(joined.toInstant(), new Date().toInstant()) >= 100);
          }
        });

    queue.add(player);
    queue.addDestination(new Destination() {
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
        return "tl-test";
      }
    });

    assertTrue(wait.get(300, TimeUnit.MILLISECONDS));
  }
}
