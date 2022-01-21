package top.mrxiaom.doomsdayessentials.gui;

import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
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
import top.mrxiaom.doomsdayessentials.configs.TagConfig;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GuiTagListAll implements IGui{
	final Main plugin;
	final Player player;
	int page;
	public GuiTagListAll(Main plugin, Player player, int page) {
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
		Inventory inv = Bukkit.getServer().createInventory(null, 54, "§e§b§d§0" + plugin.getTagConfig().getGuiTagListAllTitle() + "§r" + "§a-第" + page + "页");

		Map<Integer, String> listAllTitle = plugin.getTagConfig().getTagMap();
		int maxPages = listAllTitle.size() / 45;
		int maxSlots = listAllTitle.size() % 45;
		if (maxSlots == 0) {
			maxSlots = 45;
		}
		int nowPages = page - 1;
		if (nowPages < maxPages) {
			maxSlots = 45;
		}
		inv.clear();
		List<Integer> titleKeys = Lists.newArrayList(listAllTitle.keySet());
		Collections.sort(titleKeys);
		if (listAllTitle.size() > 0 && nowPages <= maxPages) {
			int nowSlot = 0;
			for(int i = nowPages * 45; i < nowPages * 45 + maxSlots; i++) {
				if(i < 0 || i >= titleKeys.size()) break;
				int titleId = titleKeys.get(i);
				Material material = plugin.getTagConfig().getTagDisplayItemMaterial(titleId);
				List<String> lore = plugin.getTagConfig().getTagDisplayLore(titleId);
				if (plugin.getTagConfig().hasTag(player, titleId)) {
					lore.add(0, I18n.t("title.hava"));
				} else {
					lore.add(0, I18n.t("title.nohava"));
				}
				ItemStack item = ItemStackUtil.buildItem(material, TagConfig.packId(titleId)
						+ ChatColor.translateAlternateColorCodes('&', "&r" + listAllTitle.get(titleId)),
						lore);

				inv.setItem(nowSlot, item);
				nowSlot++;
			}
		}
		ItemStack IS2 = ItemStackUtil.buildItem(Material.PAPER, I18n.t("title.LastPage"));
		inv.setItem(45, IS2);
		ItemStack IS3 = ItemStackUtil.buildItem(Material.PAPER, I18n.t("title.NextPage"));
		inv.setItem(53, IS3);
		return inv;
	}

	@Override
	public void onClick(InventoryAction action,  ClickType click, InventoryType.SlotType slotType, int slot,
                       ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
		event.setCancelled(true);
		if(event.getCurrentItem() == null) return;
		String name = ItemStackUtil.getItemDisplayName(event.getCurrentItem());
		int extractId = TagConfig.extractId(name);
		if (name.equals(I18n.t("title.LastPage"))) {
			player.closeInventory();
			player.updateInventory();
			page =  page < 2 ? 1 : page - 1;
			plugin.getGuiManager().openGui(this);
		} else if (name.equals(I18n.t("title.NextPage"))) {
			player.closeInventory();
			player.updateInventory();
			page = page + 1;
			plugin.getGuiManager().openGui(this);
		} else if (name.equals(I18n.t("title.CancelTitle"))) {
			this.plugin.getTagConfig().setDefaultTag(player);
			player.closeInventory();
			plugin.getGuiManager().openGui(this);
		} else if (extractId < 1) {
			player.sendMessage(I18n.t("title.InvalidTitle", true));
			player.closeInventory();
		} else {
			if (!this.plugin.getTagConfig().hasTag(player, extractId)) {
				player.sendMessage(I18n.t("title.nohava", true));
			} else if (this.plugin.getTagConfig().getCost() <= 0.0d) {
				this.plugin.getTagConfig().setPlayerTag(player, extractId);
			} else if (!this.plugin.getTagConfig().setPlayerTagUseMoney(player, extractId)) {
				player.sendMessage(I18n.t("title.NotEnoughMoney", true));
				return;
			} else {
				player.sendMessage(I18n.t("title.ExpendMoney", true).replace("%1",
						String.valueOf(this.plugin.getTagConfig().getCost())));
			}
			player.closeInventory();
			player.updateInventory();
			plugin.getGuiManager().openGui(this);
		}
	}

	@Override
	public void onClose(InventoryView view) {
		// do nothing.
	}
}
