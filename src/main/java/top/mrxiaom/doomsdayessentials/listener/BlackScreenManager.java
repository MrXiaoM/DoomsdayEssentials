package top.mrxiaom.doomsdayessentials.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.Pair;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BlackScreenManager implements Listener {
    Main plugin;
    Map<String, Pair<Location, Location>> blackScreenPlayers = new HashMap<>();
    public BlackScreenManager(Main plugin){
        this.plugin = plugin;
        plugin.getProtocolManager().addPacketListener(new BSPacketAdapter());
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    // 阻断方块更新
    public class BSPacketAdapter extends PacketAdapter {
        public BSPacketAdapter() {
            super(BlackScreenManager.this.plugin, ListenerPriority.NORMAL,
                    PacketType.Play.Server.BLOCK_BREAK,
                    PacketType.Play.Server.BLOCK_CHANGE,
                    PacketType.Play.Server.MULTI_BLOCK_CHANGE,
                    PacketType.Play.Server.MAP_CHUNK,
                    PacketType.Play.Server.ENTITY_EFFECT,
                    PacketType.Play.Server.PLAYER_INFO,
                    PacketType.Play.Client.BLOCK_DIG,
                    PacketType.Play.Client.POSITION);
        }
        @Override
        public void onPacketReceiving(PacketEvent event) {
            if (blackScreenPlayers.containsKey(event.getPlayer().getName())){
                if (event.getPacketType().equals(PacketType.Play.Client.BLOCK_DIG)) {
                    setBlockAround(event.getPlayer(), Material.BLACK_CONCRETE);
                    return;
                }
                if (event.getPacketType().equals(PacketType.Play.Client.POSITION)) {
                    event.setCancelled(true);
                    Location loc = blackScreenPlayers.get(event.getPlayer().getName()).getKey();
                    PacketContainer packet = event.getPacket();
                    if(loc.getX() != packet.getDoubles().read(0)
                      || loc.getY() != packet.getDoubles().read(1)
                      || loc.getZ() != packet.getDoubles().read(2)) {
                        updatePosition(event.getPlayer(), blackScreenPlayers.get(event.getPlayer().getName()).getKey());
                    }
                }
            }
        }
        @Override
        public void onPacketSending(PacketEvent event) {
            // 不阻断有元数据 sender 的包
            if (event.getPacket().getMeta("sender").isPresent()) return;
            if (blackScreenPlayers.containsKey(event.getPlayer().getName())){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (blackScreenPlayers.containsKey(event.getPlayer().getName())){
            //event.getPlayer().teleport(blackScreenPlayers.get(event.getPlayer().getName()).getKey());
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        if(isInBlackScreen(event.getPlayer().getName())){
            endBlackScreen(event.getPlayer());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(isInBlackScreen(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        if(isInBlackScreen(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

    public void startBlackScreen(Player player) {
        if (blackScreenPlayers.containsKey(player.getName())) return;
        Location oldLoc = player.getLocation();
        int x = oldLoc.getBlockX();
        int y = oldLoc.getBlockY();
        int z = oldLoc.getBlockZ();
        Location newLoc = new Location(player.getLocation().getWorld(),
                x + (x < 0 ? -1 : 1) * 0.5D,
                y,
                z + (z < 0 ? -1 : 1) * 0.5D);

        player.teleport(newLoc);
        setBlockAround(player, Material.BLACK_CONCRETE);
        blackScreenPlayers.put(player.getName(), Pair.of(newLoc, oldLoc));
    }

    public void updatePosition(Player player, Location loc) {
        updatePosition(player, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }
    public void updatePosition(Player player, double x, double y, double z, float yaw, float pitch) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.POSITION);
        packet.setMeta("sender", "DoomsdayEssentials");
        packet.getDoubles().write(0, x);
        packet.getDoubles().write(1, y);
        packet.getDoubles().write(2, z);
        packet.getFloat().write(0, yaw);
        packet.getFloat().write(1, pitch);
        packet.getSpecificModifier(Set.class).write(0, Sets.newHashSet());
        packet.getIntegers().write(0, 0);
        try {
            plugin.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void setBlockAround(Player player, Material block){
        World world = player.getLocation().getWorld();
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();
        sendBlockChange(player, x, y - 1, z, block);
        sendBlockChange(player, x, y + 2, z, block);
        sendBlockChange(player, x, y, z, block == null ? null : Material.BARRIER);
        sendBlockChange(player, x, y + 1, z, block == null ? null : Material.BARRIER);
        sendBlockChangeAround(player, x, y - 1, z, block);
        sendBlockChangeAround(player, x, y, z, block);
        sendBlockChangeAround(player, x, y + 1, z, block);
        sendBlockChangeAround(player, x, y + 2, z, block);
    }

    private void sendBlockChangeAround(Player player, int x, int y, int z, Material block) {
        sendBlockChange(player, x - 1, y, z, block);
        sendBlockChange(player, x + 1, y, z, block);
        sendBlockChange(player, x, y, z - 1, block);
        sendBlockChange(player, x, y, z + 1, block);
        sendBlockChange(player, x - 1, y, z + 1, block);
        sendBlockChange(player, x + 1, y, z + 1, block);
        sendBlockChange(player, x + 1, y, z - 1, block);
        sendBlockChange(player, x - 1, y, z - 1, block);
    }

    public void sendBlockChange(Player player, int x, int y, int z, Material block) {
        if (block == null) {
            Location loc = new Location(player.getWorld(), x, y, z);
            block = player.getWorld().getBlockAt(loc).getType();
        }
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.BLOCK_CHANGE);
        packet.setMeta("sender", "DoomsdayEssentials");
        packet.getBlockPositionModifier().write(0, new BlockPosition(x, y, z));
        packet.getBlockData().write(0, WrappedBlockData.createData(block));
        try {
            plugin.getProtocolManager().sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    public void endBlackScreen(Player player) {
        Pair<Location, Location> loc = blackScreenPlayers.getOrDefault(player.getName(), null);
        blackScreenPlayers.remove(player.getName());
        if (loc != null) updatePosition(player, loc.getValue());
        setBlockAround(player, null);
    }

    public boolean isInBlackScreen(String player){
        return blackScreenPlayers.containsKey(player);
    }
}
