package top.mrxiaom.doomsdayessentials;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Observer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HighfrequencyRedstoneCleaner implements Listener {
	private static final int time = 2;
	private static final int limit = 40;
	private static final boolean showMessage = true;
	private static final String message = ChatColor.translateAlternateColorCodes('&',
			"&b[服务器娘]&a发现&c高频&a红石，系统已自动清理，请不要&c恶意&a使用高频红石，否则可能被系统&c自动封号！&c位置为：%Location%， 附近玩家： %Player%");
	// 保存红石频率信息，按周期判断并且清空
	private final Map<Location, Integer> cache = new HashMap<>();

	final Main plugin;

	// NeverLag 高频红石清理
	public HighfrequencyRedstoneCleaner(Main plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this::redstone_run, 0L, time * 20L);

	}

	private static final List<Material> clearType = Lists.newArrayList(Material.REDSTONE, Material.REDSTONE_TORCH,
			Material.REDSTONE_WALL_TORCH, Material.REDSTONE_WIRE, Material.COMPARATOR, Material.REPEATER,
			Material.PISTON, Material.STICKY_PISTON, Material.LEVER);

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockRedstone(final BlockRedstoneEvent e) {
		Block block = e.getBlock();
		if(block.getType().equals(Material.OBSERVER)) {
			Observer data = (Observer) block.getBlockData();
			Block another = block.getRelative(data.getFacing(), 1);
			if(another.getType().equals(Material.OBSERVER)) {
				Observer data1 = (Observer) another.getBlockData();
				Block compare = another.getRelative(data1.getFacing(), 1);
				if(compare.getX() == block.getX() && compare.getY() == block.getY() && compare.getZ() == block.getZ()) {
					this.cache.put(block.getLocation(), limit);
					this.cache.put(another.getLocation(), limit);
					redstone_run();
					return;
				}
			}
		}
		if (!clearType.contains(block.getType())) {
			return;
		}
		if (this.cache.containsKey(block.getLocation())) {
			this.cache.put(block.getLocation(), this.cache.get(block.getLocation()) + 1);
		} else {
			this.cache.put(block.getLocation(), 1);
		}
	}

	public void redstone_run() {
		if (!this.cache.isEmpty()) {
			boolean flag = false;
			Location location = null;
			for (Location loc : this.cache.keySet()) {
				if (this.cache.get(loc) >= limit) {
					Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
						try {
							loc.getBlock().breakNaturally();
						} catch (Throwable t) {
							// 收声
						}
					}, 2L);
					if (location == null) {
						location = loc;
					}
					if (!flag) {
						flag = true;
					}
				}
			}
			if (flag && showMessage) {
				if (location == null) {
					Bukkit.getServer().broadcastMessage(message);
				} else {
					StringBuilder nameList = new StringBuilder();
					int count = 0;
					Entity[] entities;
					for (int length = (entities = location.getChunk().getEntities()).length, i = 0; i < length; ++i) {
						final Entity entity = entities[i];
						if (count >= 3) {
							break;
						}
						if (entity instanceof Player) {
							final Player p = (Player) entity;
							if (!p.hasMetadata("NPC") && !p.hasMetadata("shopkeeper") && !p.hasMetadata("MythicMobs")) {
								if (nameList.toString().equals("")) {
									nameList = new StringBuilder(p.getName());
								} else {
									nameList.append(", ").append(p.getName());
								}
								++count;
							}
						}
					}
					Bukkit.getServer()
							.broadcastMessage(message
									.replace("%Location%",
											"[" + (location.getWorld() != null ? location.getWorld().getName() : "???") + "," + location.getBlockX() + ","
													+ location.getBlockY() + "," + location.getBlockZ() + "]")
									.replace("%Player%", nameList.toString()));
				}
			}
			this.cache.clear();
		}
	}
}
