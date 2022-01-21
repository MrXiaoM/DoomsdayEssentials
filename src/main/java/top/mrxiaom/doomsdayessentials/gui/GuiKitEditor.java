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
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.api.IGui;
import top.mrxiaom.doomsdayessentials.configs.KitConfig.Kit;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public class GuiKitEditor implements IGui{
	final Main plugin;
	final Player player;
	final Kit kit;
	public GuiKitEditor(Main plugin, Player player, Kit kit) {
		this.plugin = plugin;
		this.player = player;
		this.kit = kit;
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public Inventory newInventory() {
		String guiTitle = "§0§8§3§3§0" + I18n.t("kit.editor-title") + " ";
		Inventory inv = Bukkit.createInventory(null, 54, guiTitle + kit.getName());

		ItemStack[] items = kit.getItems();
		if (items != null) {
			inv.addItem(items);
		}

		ItemStack itemFrame = ItemStackUtil.buildItem(Material.WHITE_STAINED_GLASS_PANE, "&f*");

		ItemStack itemSave = ItemStackUtil.buildItem(Material.CHEST, "&c保存工具包");

		inv.setItem(36, itemFrame);
		inv.setItem(37, itemFrame);
		inv.setItem(38, itemFrame);
		inv.setItem(39, itemFrame);
		inv.setItem(40, itemFrame);
		inv.setItem(41, itemFrame);
		inv.setItem(42, itemFrame);
		inv.setItem(43, itemFrame);
		inv.setItem(44, itemFrame);
		inv.setItem(45, itemFrame);
		inv.setItem(46, itemFrame);
		inv.setItem(47, itemFrame);
		inv.setItem(48, itemFrame);
		inv.setItem(49, itemSave);
		inv.setItem(50, itemFrame);
		inv.setItem(51, itemFrame);
		inv.setItem(52, itemFrame);
		inv.setItem(53, itemFrame);
		return inv;
	}

	@Override
	public void onClick( InventoryAction action,   ClickType click, InventoryType. SlotType slotType, int slot,
                         ItemStack currentItem,   ItemStack cursor, InventoryView view, InventoryClickEvent event) {
		InventoryView inv = event.getView();
		if (inv.getItem(event.getRawSlot()) == null) {
			return;
		}

		if (event.getRawSlot() >= 36 && event.getRawSlot() <= 53) {
			event.setCancelled(true);
		}
		if (event.isLeftClick() && !event.isRightClick() && !event.isShiftClick()) {

			// 返回菜单
			if (event.getRawSlot() == 49) {
				ItemStack item49 = inv.getItem(49);
				if (item49 != null && item49.getType().equals(Material.CHEST)) {
					List<ItemStack> itemList = new ArrayList<>();
					for (int i = 0; i < 36; i++) {
						ItemStack is = inv.getItem(i);
						if (is != null) {
							itemList.add(is);
						}
					}
					kit.setItems(itemList.toArray(new ItemStack[0]));
					plugin.getKitConfig().set(kit);
					player.closeInventory();
					player.sendMessage(I18n.t("kit.edit-success", true).replace("%kit%", kit.getId()).replace("%items%",
							String.valueOf(itemList.size())));
				}
			}
		}
	}

	@Override
	public void onClose(InventoryView view) {
		// do nothing.
	}
}
