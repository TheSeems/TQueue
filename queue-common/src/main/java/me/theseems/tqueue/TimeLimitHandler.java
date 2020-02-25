package me.theseems.tqueue;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

public abstract class TimeLimitHandler implements QueueHandler {
  private static Map<UUID, Date> joined;
  private static Function<UUID, Long> limitFunction;

  public TimeLimitHandler(Function<UUID, Long> limitFunction) {
    joined = new ConcurrentSkipListMap<>();
    TimeLimitHandler.limitFunction = limitFunction;
  }

  public TimeLimitHandler(long delay) {
    this(uuid -> delay);
  }

  // Default constructor for 30 seconds timeout
  public TimeLimitHandler() {
    this(uuid -> 30000L);
  }

  public abstract void timedOut(UUID player);

  @Override
  public void join(UUID player) {
    joined.putIfAbsent(player, new Date());
  }

  @Override
  public void leave(UUID player) {
    joined.remove(player);
  }

  public void check(UUID player) {
    Date joinDate = joined.getOrDefault(player, new Date());
    long diff = ChronoUnit.MILLIS.between(joinDate.toInstant(), new Date().toInstant());
    if (diff > limitFunction.apply(player)) {
      joined.remove(player);
      timedOut(player);
    }
  }

  @Override
  public boolean apply(UUID player, Destination destination, Verdict verdict) {
    check(player);
    return !joined.containsKey(player);
  }

  @Override
  public String getName() {
    return "time-limit";
  }
}
