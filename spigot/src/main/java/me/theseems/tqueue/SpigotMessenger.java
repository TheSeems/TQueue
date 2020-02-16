package me.theseems.tqueue;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.Arrays;
import java.util.UUID;

public class SpigotMessenger implements PluginMessageListener, Listener {
  private static UUID readUUID(ByteArrayDataInput input) {
    long high = input.readLong();
    long low = input.readLong();
    return new UUID(high, low);
  }

  public SpigotMessenger() {
    Messenger messenger = TQueueSpigot.getPlugin().getServer().getMessenger();
    messenger.registerIncomingPluginChannel(TQueueSpigot.getPlugin(), "tqueue:info", this);
    messenger.registerOutgoingPluginChannel(TQueueSpigot.getPlugin(), "tqueue:info");
    TQueueSpigot.getPlugin()
        .getServer()
        .getPluginManager()
        .registerEvents(this, TQueueSpigot.getPlugin());
  }

  static void fillOutput(ByteArrayDataInput in, ByteArrayDataOutput out) {

    try {
      String host = in.readUTF();
      String ip = in.readUTF();
      String name = in.readUTF();
      UUID uuid = UUID.fromString(in.readUTF());

      Verdict verdict = TQueueSpigot.getReplier().process(uuid);
      out.writeBoolean(verdict.ok);
      out.writeUTF(verdict.desc);

    } catch (Exception e) {
      e.printStackTrace();
      out.writeBoolean(false);
      out.writeUTF("Internal error: " + e.getMessage());
    }
  }

  @Override
  public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
    System.out.println("[TQueue] Message received " + channel + " " + Arrays.toString(bytes));
    if (!channel.equals("tqueue:info")) return;

    ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
    ByteArrayDataOutput out = ByteStreams.newDataOutput();

    fillOutput(in, out);

    Iterables.getFirst(Bukkit.getOnlinePlayers(), null)
        .sendPluginMessage(TQueueSpigot.getPlugin(), "tqueue:info", out.toByteArray());
  }
}
