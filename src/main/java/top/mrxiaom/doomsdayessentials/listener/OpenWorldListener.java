package top.mrxiaom.doomsdayessentials.listener;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.OpenWorldConfig.OpenWorldPlayer;
import top.mrxiaom.doomsdayessentials.utils.I18n;

import java.util.*;

public class OpenWorldListener implements Listener {

	final Main plugin;
	public final String openWorldName = "openworld";
	Set<String> showBorderTipsPlayers = new HashSet<>();
	public OpenWorldListener(Main plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
		Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::onTick, 10, 10);
		plugin.getLogger().info("初始化开放世界");
	}

	public boolean isInOpenWorld(Player player) {
		return player.getWorld().getName().equalsIgnoreCase(openWorldName);
	}

	private void onTick() {
		if (Bukkit.getOnlinePlayers().size() < 1) return;
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!player.getWorld().getName().equalsIgnoreCase(openWorldName)) continue;
			WorldBorder border = player.getWorld().getWorldBorder();
			Location center = border.getCenter();
			Location loc = player.getLocation();
			int warningDistance = border.getWarningDistance();
			double x = Math.abs(center.getX() - loc.getX());
			double z = Math.abs(center.getZ() - loc.getZ());
			if (x > warningDistance || z > warningDistance) {
				player.sendTitle("§c区域管制", "§e前面的区域， 以后再来探索吧",
						showBorderTipsPlayers.contains(player.getName()) ? 0 : 10, 40, 10);
				showBorderTipsPlayers.add(player.getName());
			}
			else showBorderTipsPlayers.remove(player.getName());
		}
	}
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event){
		if (!event.getPlayer().isOp() && event.getPlayer().getWorld().getName().equalsIgnoreCase(openWorldName)
				&& !event.getView().getTitle().contains("§")){
			event.getPlayer().sendMessage("§c禁止打开容器");
			event.setCancelled(true);
		}
	}
}
