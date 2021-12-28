package top.mrxiaom.doomsdayessentials.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiBindRecall implements IGui{
	final Main plugin;
	final Player player;
	int page;
	public GuiBindRecall(Main plugin, Player player, int page) {
		this.plugin = plugin;
		this.player = player;
		this.page = page;
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public Inventory newInventory() {
		String guiTitle = "§0§8§3§6§0绑定列表";
		Inventory inv = Bukkit.createInventory(null, 54, guiTitle);

		Map<String, ItemStack> bindItems = plugin.getBindConfig().getPlayerBindList(player.getName(), true);
		int maxPages = (int) Math.ceil(bindItems.size() / 45.0D);
		int i = 0, j = 0;
		for(ItemStack item : bindItems.values()) {
			if(i >= (page - 1) * 45 && j < 45) {
				inv.setItem(j, item);
				j++;
			}
			i++;
		}

		ItemStack itemFrame = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemMeta im = itemFrame.hasItemMeta() ? itemFrame.getItemMeta()
				: NMSUtil.getMetaFormMaterial(itemFrame.getType());
		im.setDisplayName(ChatColor.WHITE + "*");
		itemFrame.setItemMeta(im);

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
		inv.setItem(49, itemFrame);
		inv.setItem(50, itemFrame);
		inv.setItem(51, itemFrame);
		inv.setItem(52, itemFrame);
		inv.setItem(53, page == maxPages ? itemFrame : itemNextPage);
		return inv;
	
	}

	@Override
	public void onClick( InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot,
                        ItemStack currentItem,ItemStack cursor, InventoryView view, InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.isLeftClick() && !event.isRightClick() && !event.isShiftClick()) {
			InventoryView inv = event.getView();
			if (inv.getItem(slot) == null) {
				return;
			}
			// 召回神器
			if (slot < 45) {
				ItemStack item = inv.getItem(slot);
				if (item != null && item.getType() != Material.AIR && item.hasItemMeta()) {
					List<String> lore = item.getItemMeta().getLore();
					String code = lore.get(lore.size() - 2).replace("§0", "");
					player.closeInventory();
					ItemStack itemRecall = plugin.getBindConfig().getItemStackFromCode(code);
					if(itemRecall == null) {
						player.sendMessage("§7[§9末日社团§7] §c储存的物品数据错误，无法召回");
						return;
					}
					plugin.getBindConfig().removeBind(code).saveConfig();
					HashMap<Integer, ItemStack> lost = player.getInventory().addItem(itemRecall);
					player.sendMessage("§7[§9末日社团§7] §6你已成功召回物品§c " + ItemStackUtil.getItemDisplayName(itemRecall));
					if (!lost.isEmpty()) {
						player.sendMessage("§7[§9末日社团§7] §e你的背包已满，召回的装备已掉落到你身边");
						for (ItemStack im : lost.values()) {
							player.getWorld().dropItem(player.getLocation(), im);
						}
					}
					// 检查所有人的背包
					for(Player p : Bukkit.getOnlinePlayers()) {
						plugin.getInventoryListener().checkPlayerInvBindingItems(p);
					}
				}
				return;
			}
			// 上一页
			if (slot == 45) {
				if (page - 1 < 1) {
					// player.sendMessage(I18n.t("warp.gui.no-prev-page-message", true));
					return;
				}
				page = page - 1;
				plugin.getGuiManager().openGui(this);
				return;

			}
			// 下一页
			if (slot == 53) {
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
