package top.mrxiaom.doomsdayessentials.modules.reviveme.version.v1_15_R1;

import net.minecraft.server.v1_15_R1.ChatMessageType;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_15_R1.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import top.mrxiaom.doomsdayessentials.modules.reviveme.utils.IActionBarUtil;

import java.util.HashMap;
import java.util.Map;

public class ActionBarUtil implements IActionBarUtil {
    private static final Map PENDING_MESSAGES = new HashMap();

    public void sendActionBarMessage(Player bukkitPlayer, String message) {
        this.sendRawActionBarMessage(bukkitPlayer, "{\"text\": \"" + message + "\"}");
    }

    public void sendRawActionBarMessage(Player bukkitPlayer, String rawMessage) {
        CraftPlayer player = (CraftPlayer) bukkitPlayer;
        IChatBaseComponent chatBaseComponent = ChatSerializer.a(rawMessage);
        PacketPlayOutChat packetPlayOutChat = new PacketPlayOutChat(chatBaseComponent, ChatMessageType.GAME_INFO);
        player.getHandle().playerConnection.sendPacket(packetPlayOutChat);
    }

    public void sendActionBarMessage(final Player bukkitPlayer, final String message, final int duration, Plugin plugin) {
        this.cancelPendingMessages(bukkitPlayer);
        BukkitTask messageTask = (new BukkitRunnable() {
            private int count = 0;

            public void run() {
                if (this.count >= duration - 3) {
                    this.cancel();
                }

                ActionBarUtil.this.sendActionBarMessage(bukkitPlayer, message);
                ++this.count;
            }
        }).runTaskTimer(plugin, 0L, 20L);
        PENDING_MESSAGES.put(bukkitPlayer, messageTask);
    }

    public void cancelPendingMessages(Player bukkitPlayer) {
        if (PENDING_MESSAGES.containsKey(bukkitPlayer)) {
            ((BukkitTask) PENDING_MESSAGES.get(bukkitPlayer)).cancel();
        }

    }
}
