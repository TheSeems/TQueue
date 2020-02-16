package me.theseems.tqueue;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static me.theseems.tqueue.RedisMessenger.getVerdictCompletableFuture;

public class BungeeMessenger implements Listener, Messenger {
  private Map<String, Pair<Boolean, String>> answerMap;

  public BungeeMessenger() {
    answerMap = new HashMap<>();
    TQueueBungeePlugin.getProxyServer().registerChannel("tqueue:info");
    TQueueBungeePlugin.getProxyServer()
        .getPluginManager()
        .registerListener(TQueueBungeePlugin.getPlugin(), this);
  }

  public static void writeUUID(UUID uuid, ByteArrayDataOutput out) {
    out.writeLong(uuid.getMostSignificantBits());
    out.writeLong(uuid.getLeastSignificantBits());
  }

  @EventHandler
  public void on(PluginMessageEvent event) {
    System.out.println(
        "[TQueue] Message received "
            + event.getTag()
            + " "
            + Arrays.toString(event.getData())
            + " from "
            + event.getSender());
    if (!event.getTag().equalsIgnoreCase("tqueue:info")) return;

    ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
    // request a user answer
    boolean result = in.readBoolean();
    String additional = in.readUTF();

    if (event.getSender() instanceof Server) {
      String name = ((Server) event.getSender()).getInfo().getName();
      answerMap.put(name, new Pair<>(result, additional));
    }
  }

  public static void forUserOut(UUID player, ByteArrayDataOutput out) {
    ProxiedPlayer pp = TQueueBungeePlugin.getProxyServer().getPlayer(player);
    assert pp != null;

    String hostname = pp.getPendingConnection().getVirtualHost().getHostName();
    String ip = pp.getPendingConnection().getVirtualHost().getHostString();

    out.writeUTF(hostname);
    out.writeUTF(ip);
    out.writeUTF(pp.getName());
    out.writeUTF(player.toString());
  }

  public CompletableFuture<Verdict> requestUser(String name, UUID player) {
    ProxiedPlayer pp = TQueueBungeePlugin.getProxyServer().getPlayer(player);
    if (pp == null) {
      return CompletableFuture.completedFuture(Verdict.FORBIDDEN);
    }

    ByteArrayDataOutput out = ByteStreams.newDataOutput();
    forUserOut(player, out);

    TQueueBungeePlugin.getProxyServer()
        .getServerInfo(name)
        .sendData("tqueue:info", out.toByteArray());

    return getVerdictCompletableFuture(name, answerMap);
  }
}
