package top.mrxiaom.doomsdayessentials.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPoseChangeEvent;
import org.bukkit.event.entity.EntityToggleSwimEvent;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.commands.CommandPose;
import top.mrxiaom.doomsdayessentials.utils.NMSUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeathListener implements Listener {
    Main plugin;
    public Map<String, Pose> playersPose = new HashMap<>();
    public DeathListener(Main plugin){
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onEntityPoseChange(EntityToggleSwimEvent event) {
        if(!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if(!event.isSwimming() && plugin.getCmdManager().getCommandInstance(CommandPose.class).a(player)) event.setCancelled(true);
    }
}
