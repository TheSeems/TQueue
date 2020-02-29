package me.theseems.tqueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SimplePingReplier implements SpigotPingReplier {
  private List<SpigotPingProcessor> pingProcessors;

  public SimplePingReplier() {
    pingProcessors = new ArrayList<>();
  }

  @Override
  public void addProcessor(SpigotPingProcessor processor) {
    pingProcessors.add(processor);
  }

  @Override
  public void removeProcessor(SpigotPingProcessor processor) {
    pingProcessors.remove(processor);
  }

  @Override
  public Verdict process(UUID player) {
    for (SpigotPingProcessor processor : pingProcessors) {
      Optional<Verdict> verdict = processor.hookup(player);
      if (verdict.isPresent()) return verdict.get();
    }
    return Verdict.OK;
  }
}
