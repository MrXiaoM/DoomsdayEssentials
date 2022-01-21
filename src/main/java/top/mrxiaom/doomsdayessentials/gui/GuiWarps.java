package top.mrxiaom.doomsdayessentials.gui;

import com.google.common.collect.Lists;
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
import top.mrxiaom.doomsdayessentials.configs.WarpConfig.Warp;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;
import top.mrxiaom.doomsdayessentials.utils.NMSUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GuiWarps implements IGui {
	final Main plugin;
	final Player player;
	int page;
	final boolean hasBackToMenuButton;
	public GuiWarps(Main plugin, Player player, int page, boolean hasBackToMenuButton) {
		this.plugin = plugin;
		this.player = player;
		this.page = page;
		this.hasBackToMenuButton = hasBackToMenuButton;
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public Inventory newInventory() {
		String guiTitle = "§0§8§3§1" + I18n.t("warp.gui.title.text");
		Inventory inv = Bukkit.createInventory(null, 54,
				guiTitle + I18n.t("warp.gui.title.pages").replace("%page%", String.valueOf(page)));

		int maxPages = plugin.getWarpConfig().getPages(45);

		List<Warp> warpList = plugin.getWarpConfig().getWarpList(page, false);
		for (int i = 0; i < warpList.size(); i++) {
			Warp warp = warpList.get(i);
			Location location = warp.getLocation();
			ItemStack item = new ItemStack(warp.getMaterial());
			ItemMeta im = Objects.requireNonNullElse(item.getItemMeta(), NMSUtil.getMetaFormMaterial(item.getType()));
			im.setDisplayName(I18n.t("warp.gui.items.name").replace("%warp%", warp.getName()));
			List<String> lore = new ArrayList<>();
			lore.add(I18n.t("warp.gui.items.world").replace("%world%", location.getWorld() != null ? location.getWorld().getName() : "???"));
			im.setLore(lore);
			item.setItemMeta(im);
			inv.setItem(i, item);
		}

		String pages = "&0" + page;

		ItemStack itemFrame = ItemStackUtil.buildItem(Material.WHITE_STAINED_GLASS_PANE, "&f*");

		ItemStack itemBack = ItemStackUtil.buildItem(Material.RED_STAINED_GLASS_PANE, I18n.t("warp.gui.back"));

		ItemStack itemPrevPage = ItemStackUtil.buildItem(Material.GREEN_STAINED_GLASS_PANE, I18n.t("warp.gui.prev-page"),
				page <= 1 ? Lists.newArrayList(I18n.t("warp.gui.no-prev-page"), pages) : Lists.newArrayList(pages));

		ItemStack itemNextPage = ItemStackUtil.buildItem(Material.GREEN_STAINED_GLASS_PANE, I18n.t("warp.gui.next-page"),
				page >= maxPages ? Lists.newArrayList(I18n.t("warp.gui.no-next-page"), pages) : Lists.newArrayList(pages));

		inv.setItem(45, page <= 1 ? itemFrame : itemPrevPage);
		inv.setItem(46, itemFrame);
		inv.setItem(47, itemFrame);
		inv.setItem(48, itemFrame);
		inv.setItem(49, hasBackToMenuButton ? itemBack : itemFrame);
		inv.setItem(50, itemFrame);
		inv.setItem(51, itemFrame);
		inv.setItem(52, itemFrame);
		inv.setItem(53, page >= maxPages ? itemFrame : itemNextPage);
		return inv;
	}

	@Override
	public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot,
                        ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.isLeftClick() && !event.isRightClick() && !event.isShiftClick()) {
			InventoryView inv = event.getView();
			if (inv.getItem(event.getRawSlot()) == null) {
				return;
			}

			if (event.getRawSlot() < 45) {
				ItemStack item = inv.getItem(event.getRawSlot());
				String displayName = ItemStackUtil.getItemDisplayName(item);
				player.closeInventory();
				Bukkit.dispatchCommand(player, "warp " + Util.removeColor(displayName));

				return;
			}

			// 返回菜单
			if (event.getRawSlot() == 49) {
				ItemStack item49 = inv.getItem(49);
				if (item49 != null && item49.getType().equals(Material.RED_STAINED_GLASS_PANE)) {
					player.closeInventory();
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dm open 传送 " + player.getName());
				}
				return;
			}
			// 上一页
			if (event.getRawSlot() == 45) {
				if (page - 1 < 1) {
					// player.sendMessage(I18n.t("warp.gui.no-prev-page-message", true));
					return;
				}
				page = page - 1;
				plugin.getGuiManager().openGui(this);
			}
			// 下一页
			if (event.getRawSlot() == 53) {
				int maxPages = plugin.getWarpConfig().getPages(45);
				if (page + 1 > maxPages) {
					// player.sendMessage(I18n.t("warp.gui.no-next-page-message", true));
					return;
				}
				page = page + 1;
				plugin.getGuiManager().openGui(this);
			}
		}
	}

	@Override
	public void onClose(InventoryView view) {
		// do nothing.
	}
}
