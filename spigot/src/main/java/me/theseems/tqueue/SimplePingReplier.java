package me.theseems.tqueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SimplePingReplier implements SpigotPingReplier {
  private List<SpigotPingProcessor> processorQueue;

  public SimplePingReplier() {
    processorQueue = new ArrayList<>();
  }

  @Override
  public void addProcessor(SpigotPingProcessor processor) {
    processorQueue.add(processor);
  }

  @Override
  public void removeProcessor(SpigotPingProcessor processor) {
    processorQueue.remove(processor);
  }

  @Override
  public Verdict process(UUID player) {
    for (SpigotPingProcessor processor : processorQueue) {
      Optional<Verdict> verdict = processor.hookup(player);
      if (verdict.isPresent()) return verdict.get();
    }
    return Verdict.OK;
  }
}
