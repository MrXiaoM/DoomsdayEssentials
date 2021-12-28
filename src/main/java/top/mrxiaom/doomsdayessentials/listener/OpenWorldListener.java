package top.mrxiaom.doomsdayessentials.listener;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.OpenWorldConfig.OpenWorldPlayer;
import top.mrxiaom.doomsdayessentials.utils.I18n;

public class OpenWorldListener implements Listener {

	final Main plugin;
	public boolean enable = true;
	public final String openWorldName = "openworld";

	public OpenWorldListener(Main plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
		plugin.getLogger().info("初始化开放世界");
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		// debug
		// if(!event.getPlayer().getName().equalsIgnoreCase("lazycat")) return;
		World fromWorld = event.getFrom().getWorld();
		World toWorld = event.getTo().getWorld();
		// 如果来自开放世界
		if (fromWorld.getName().equalsIgnoreCase(openWorldName) && !toWorld.getName().equalsIgnoreCase(openWorldName)) {
			// 切换为原版背包
			this.switchInventory(event.getPlayer(), InventoryType.vanilla);
		}
		// 如果来自原版世界
		else if (toWorld.getName().equalsIgnoreCase(openWorldName)
				&& !fromWorld.getName().equalsIgnoreCase(openWorldName)) {
			// 切换为开放世界背包
			this.switchInventory(event.getPlayer(), InventoryType.openworld);
		}
	}

	public enum InventoryType {
		vanilla, openworld
	}

	/**
	 * 切换原版和开放世界的背包物品
	 * 
	 * @author MrXiaoM
	 * 
	 * @param player 玩家
	 * @param type 背包类型
	 */
	public void switchInventory(Player player, InventoryType type) {
		// 前往原版世界
		if (type == InventoryType.vanilla) {
			if (!player.getWorld().getName().equals(openWorldName)) {
				player.sendMessage(
						I18n.t("openworld.error.prefix", true).replace("%error%", I18n.t("openworld.error.unallow")));
				return;
			}
			OpenWorldPlayer owp = plugin.getOpenWorldConfig().get(player, false);
			// 先备份，后设置
			owp.setItemsOpenWorldLast(player.getInventory().getContents());
			ItemStack[] contents = owp.getItemsLast();
			if (contents == null) {
				player.getInventory().clear();
			} else {
				player.getInventory().setContents(owp.getItemsLast());
			}
		}
		if (type == InventoryType.openworld) {
			if (player.getWorld().getName().equals(openWorldName)) {
				player.sendMessage(
						I18n.t("openworld.error.prefix", true).replace("%error%", I18n.t("openworld.error.unallow")));
				return;
			}
			OpenWorldPlayer owp = plugin.getOpenWorldConfig().get(player, false);
			// 先备份，后设置
			owp.setItemsLast(player.getInventory().getContents());
			ItemStack[] contents = owp.getItemsLast();
			if (contents == null) {
				player.getInventory().clear();
			} else {
				player.getInventory().setContents(owp.getItemsOpenWorldLast());
			}
		}
		player.sendMessage(I18n.t("openworld.switch-inventory", true).replace("%type%",
				I18n.t("openworld.inv-type." + type.name())));
	}
}
