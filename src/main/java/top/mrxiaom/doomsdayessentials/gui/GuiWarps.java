package top.mrxiaom.doomsdayessentials.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import top.mrxiaom.doomsdayessentials.utils.NMSUtil;

import java.util.ArrayList;
import java.util.List;

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
			ItemMeta im = item.hasItemMeta() ? item.getItemMeta()
					: NMSUtil.getMetaFormMaterial(item.getType());
			im.setDisplayName(I18n.t("warp.gui.items.name").replace("%warp%", warp.getName()));
			List<String> lore = new ArrayList<String>();
			lore.add(I18n.t("warp.gui.items.world").replace("%world%", location.getWorld().getName()));
			im.setLore(lore);
			item.setItemMeta(im);
			inv.setItem(i, item);
		}

		ItemStack itemFrame = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemMeta im = itemFrame.hasItemMeta() ? itemFrame.getItemMeta()
				: NMSUtil.getMetaFormMaterial(itemFrame.getType());
		im.setDisplayName(ChatColor.WHITE + "*");
		itemFrame.setItemMeta(im);

		ItemStack itemBack = new ItemStack(Material.RED_STAINED_GLASS_PANE);
		ItemMeta imBack = itemBack.hasItemMeta() ? itemBack.getItemMeta()
				: NMSUtil.getMetaFormMaterial(itemBack.getType());
		imBack.setDisplayName(ChatColor.RED + "返回传送菜单");
		itemBack.setItemMeta(imBack);

		ItemStack itemPrevPage = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
		ItemMeta imPrevPage = itemPrevPage.hasItemMeta() ? itemPrevPage.getItemMeta()
				: NMSUtil.getMetaFormMaterial(itemPrevPage.getType());
		imPrevPage.setDisplayName(I18n.t("warp.gui.prev-page"));
		List<String> lorePP = new ArrayList<String>();
		if (page == 1) {
			lorePP.add(I18n.t("warp.gui.no-prev-page"));
		}
		lorePP.add(ChatColor.BLACK + "" + page);
		imPrevPage.setLore(lorePP);

		itemPrevPage.setItemMeta(imPrevPage);

		ItemStack itemNextPage = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
		ItemMeta imNextPage = itemNextPage.hasItemMeta() ? itemNextPage.getItemMeta()
				: NMSUtil.getMetaFormMaterial(itemNextPage.getType());
		imNextPage.setDisplayName(I18n.t("warp.gui.next-page"));
		List<String> loreNP = new ArrayList<String>();
		if (page == maxPages) {
			loreNP.add(I18n.t("warp.gui.no-next-page"));
		}
		loreNP.add(ChatColor.BLACK + "" + page);
		imNextPage.setLore(loreNP);
		itemNextPage.setItemMeta(imNextPage);

		inv.setItem(45, page == 1 ? itemFrame : itemPrevPage);
		inv.setItem(46, itemFrame);
		inv.setItem(47, itemFrame);
		inv.setItem(48, itemFrame);
		inv.setItem(49, hasBackToMenuButton ? itemBack : itemFrame);
		inv.setItem(50, itemFrame);
		inv.setItem(51, itemFrame);
		inv.setItem(52, itemFrame);
		inv.setItem(53, page == maxPages ? itemFrame : itemNextPage);
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
				if (item.hasItemMeta()) {
					player.closeInventory();
					Bukkit.dispatchCommand(player, "warp " + item.getItemMeta().getDisplayName().replace("§e", ""));
				}
				return;
			}

			// 返回菜单
			if (event.getRawSlot() == 49) {
				if (inv.getItem(49).getType().equals(Material.RED_STAINED_GLASS_PANE)) {
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
				return;
			}
		}
	}

	@Override
	public void onClose(InventoryView view) {
		// do nothing.
	}
}
