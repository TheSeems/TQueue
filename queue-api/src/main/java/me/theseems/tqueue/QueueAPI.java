package me.theseems.tqueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QueueAPI {
    // Default executor service
    private static ExecutorService service;

    // Queue manager for registering queues and getting information about them
    private static QueueManager queueManager;

    // Log manager
    private static QueueLogManager logManager;

    // Handler manager
    private static QueueHandlerManager handlerManager;

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

    public static QueueLogManager logs() {
        return logManager;
    }

    public static void setLogManager(QueueLogManager logManager) {
        QueueAPI.logManager = logManager;
    }

    public static QueueHandlerManager getHandlerManager() {
        return handlerManager;
    }

    public static void setHandlerManager(QueueHandlerManager handlerManager) {
        QueueAPI.handlerManager = handlerManager;
    }
}
