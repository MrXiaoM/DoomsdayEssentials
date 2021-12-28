package top.mrxiaom.doomsdayessentials.gui;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.api.IGui;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;
import top.mrxiaom.doomsdayessentials.utils.NMSUtil;

import java.util.ArrayList;
import java.util.List;

public class GuiBackPoints implements IGui {
	final Main plugin;
	final Player player;
	public GuiBackPoints(Main plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
	}

	private Material getMaterial(Location loc) {
		String world = loc.getWorld().getName();
		if (world.equalsIgnoreCase("world"))
			return Material.GRASS_BLOCK;
		if (world.equalsIgnoreCase("zz"))
			return Material.CRAFTING_TABLE;
		if (world.equalsIgnoreCase("plotworld"))
			return Material.SANDSTONE_STAIRS;
		if (world.equalsIgnoreCase("world_nether"))
			return Material.NETHER_WART;
		if (world.equalsIgnoreCase("world_the_end"))
			return Material.END_STONE_BRICKS;
		if (world.equalsIgnoreCase("spawn"))
			return Material.NETHER_STAR;
		return Material.ITEM_FRAME;
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public Inventory newInventory() {
		String guiTitle = "§0§8§2§1" + I18n.t("back.gui.title");
		Inventory inv = Bukkit.createInventory(null, 9, guiTitle);

		Location[] locs = plugin.getBackConfig().getBackPoints(getPlayer().getName());
		for (int i = 0; i < 7; i++) {
			if (i > locs.length)
				break;
			Location location = locs[i];
			if (location == null)
				continue;
			ItemStack item = new ItemStack(getMaterial(location));
			ItemMeta im = item.hasItemMeta() ? item.getItemMeta()
					: NMSUtil.getMetaFormMaterial(item.getType());
			im.setDisplayName(I18n.t("back.gui.items.name").replace("%point%", String.valueOf(i + 1)));
			List<String> lore = new ArrayList<String>();
			for (String s : I18n.l("back.gui.items.lore")) {
				lore.add(s.replace("%world%", plugin.getWorldAlias(location.getWorld()))
						.replace("%x%", String.valueOf(location.getX())).replace("%y%", String.valueOf(location.getY()))
						.replace("%z%", String.valueOf(location.getZ()))
						.replace("%yaw%", String.valueOf(location.getYaw()))
						.replace("%pitch%", String.valueOf(location.getPitch()))
						.replace("%money%", String.valueOf(i * 100)));
			}
			im.setLore(lore);
			item.setItemMeta(im);
			inv.setItem(i, item);
		}
		inv.setItem(8, ItemStackUtil.buildItem(Material.BARRIER, "&c&l返回主菜单"));
		return inv;
	}

	@Override
	public void onClick(InventoryAction action,  ClickType click, InventoryType.SlotType slotType, int slot,
                        ItemStack currentItem,  ItemStack cursor, InventoryView view, InventoryClickEvent event) {
		player.getOpenInventory();
		event.setCancelled(true);
		if (event.isLeftClick() && !event.isRightClick() && !event.isShiftClick()) {
			InventoryView inv = event.getView();
			if (inv.getItem(event.getRawSlot()) == null) {
				return;
			}
			if (event.getRawSlot() == 8) {
			player.closeInventory();
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dm open 主菜单 " + player.getName());
				return;
			}
			if (event.getRawSlot() < 7) {
				Location loc = plugin.getBackConfig().getBackPoints(player.getName())[event.getRawSlot()];
				if (loc == null) {
					player.sendMessage(I18n.t("back.nopoint", true));
					return;
				}
				if(plugin.getEcoApi().getBalance(player) < event.getRawSlot() * 100) {
					player.sendMessage(I18n.t("back.nomoney", true));
					return;
				}
				plugin.getEcoApi().withdrawPlayer(player, event.getRawSlot() * 100);
				player.closeInventory();
				String playerName = player.getName();
				if (player.hasPermission("doomteam.teleport.cooldown.bypass")) {
					plugin.getBackConfig().addBackPoint(player, player.getLocation());
					player.teleport(loc);
					player.sendMessage(I18n.t("back.teleport", true));
					return;
				}
				if (plugin.getPlayerCooldownManager().isCooldown(playerName)) {
					plugin.getPlayerCooldownManager().cancelPlayerCooldownTask(playerName);
					player.sendMessage(I18n.t("teleport-move"));
				}
				player.sendMessage(I18n.t("teleport-intime", true).replace("%time%", String.valueOf(3)));
				plugin.getPlayerCooldownManager().put(playerName,
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						@Override
						public void run() {
							if (plugin.getPlayerCooldownManager().isCooldown(playerName)) {
								plugin.getPlayerCooldownManager().cancelPlayerCooldownTask(playerName);
							}
							plugin.getBackConfig().addBackPoint(player, player.getLocation());
							player.teleport(loc);
							player.sendMessage(I18n.t("back.teleport", true));
							}
						}, 3 * 20));
				return;
			}
		}
	}

	@Override
	public void onClose(InventoryView view) {
		// do nothing.
	}
}
