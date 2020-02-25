package me.theseems.tqueue;

import java.util.logging.Logger;

public class SimpleLogManager implements QueueLogManager {
  private static final String DEFAULT_PREFIX = "TQueue";

  @Override
  public Logger prefix(String name) {
    return Logger.getLogger(name + "@" + DEFAULT_PREFIX);
  }

  @Override
  public Logger get() {
    return Logger.getLogger(DEFAULT_PREFIX);
  }
}
