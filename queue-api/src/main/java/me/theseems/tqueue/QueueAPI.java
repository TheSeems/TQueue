package me.theseems.tqueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QueueAPI {
  /** Executor service for TQueue */
  private static ExecutorService service;

  /** Queue manager for registering queues and getting information about them */
  private static QueueManager queueManager;

  public QueueAPI(int threads) {
    setService(Executors.newFixedThreadPool(threads));
  }

  public static ExecutorService getService() {
    return service;
  }

  public static void setService(ExecutorService service) {
    QueueAPI.service = service;
  }

  public static QueueManager getQueueManager() {
    return queueManager;
  }

  public static void setQueueManager(QueueManager queueManager) {
    QueueAPI.queueManager = queueManager;
  }
}
