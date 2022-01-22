package top.mrxiaom.doomsdayessentials.modules.reviveme.version.v1_15_R1;

import net.minecraft.server.v1_15_R1.*;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdayessentials.modules.reviveme.version.ISendPlay;

import java.util.List;

public class SendPlay implements ISendPlay {
    public void play(List<Player> others, Player target) {
        EntityPlayer entityPlayer = ((CraftPlayer) target).getHandle();
        DataWatcher watcher = entityPlayer.getDataWatcher();
        watcher.set(DataWatcherRegistry.s.a(6), EntityPose.SWIMMING);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entityPlayer.getId(), watcher, true);

        for (Player receiver : others) {
            if (receiver != target) {
                ((CraftPlayer) receiver).getHandle().playerConnection.sendPacket(metadata);
            }
        }
    }

    public void playStand(Player target, List<Player> others) {
        EntityPlayer entityPlayer = ((CraftPlayer) target).getHandle();
        DataWatcher watcher = entityPlayer.getDataWatcher();
        watcher.set(DataWatcherRegistry.s.a(6), EntityPose.STANDING);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entityPlayer.getId(), watcher, true);

        for (Player receiver : others) {
            if (receiver != target) {
                ((CraftPlayer) receiver).getHandle().playerConnection.sendPacket(metadata);
            }
        }
    }
}
