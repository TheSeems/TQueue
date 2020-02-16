package me.theseems.tqueue;

import java.util.UUID;
import java.util.concurrent.Future;

public class DummyDestination implements Destination {
  private Verdict verdict;
  private String name;
  private int priority;

  public DummyDestination(Verdict verdict, String name, int priority) {
    this.verdict = verdict;
    this.name = name;
    this.priority = priority;
  }

  @Override
  public Future<Verdict> query(UUID user) {
    return QueueAPI.getService().submit(() -> verdict);
  }

  @Override
  public int getPriority() {
    return priority;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "DummyDestination{" +
      "verdict=" + verdict +
      ", name='" + name + '\'' +
      ", priority=" + priority +
      '}';
  }
}
