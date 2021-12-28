package top.mrxiaom.doomsdayessentials.gui;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
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
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GuiBlockBaptism implements IGui {
	final Main plugin;
	final Player player;
	final ItemStack item;
	boolean flagClose = false;
	public GuiBlockBaptism(Main plugin, Player player, ItemStack item) {
		this.plugin = plugin;
		this.player = player;
		this.item = item;
	}

	public static class Ench {
		Enchantment ench;
		int level;

		public Ench(Enchantment ench, int level) {
			this.ench = ench;
			this.level = level;
		}

		public Enchantment getEnch() {
			return ench;
		}

		public int getLevel() {
			return level;
		}

		public void setEnch(Enchantment ench) {
			this.ench = ench;
		}

		public void setLevel(int level) {
			this.level = level;
		}

		public static List<Ench> getEnchs(Map<Enchantment, Integer> map) {
			List<Ench> result = new ArrayList<>();
			for (Enchantment e : map.keySet()) {
				result.add(new Ench(e, map.get(e)));
			}
			return result;
		}
	}

	@Override
	public Player getPlayer() {
		return this.player;
	}

	@Override
	public Inventory newInventory() {

		if (item == null || item.getEnchantments().isEmpty())
			return null;
		String guiTitle = "§0§8§3§6" + I18n.t("baptism.gui-title");
		Inventory inv = Bukkit.createInventory(null, 54, guiTitle);
		ItemStack itemB = ItemStackUtil.buildFrameItem(Material.BLACK_STAINED_GLASS_PANE);
		ItemStack itemG = ItemStackUtil.buildFrameItem(Material.GRAY_STAINED_GLASS_PANE);
		ItemStack itemR = ItemStackUtil.buildItem(Material.RED_STAINED_GLASS_PANE, "&c&l没有附魔", Lists.newArrayList("&7无法放入洗炼石"));
		ItemStack itemL = ItemStackUtil.buildItem(Material.LIME_STAINED_GLASS_PANE, "&a&l确认洗炼");
		ItemStack itemSign = ItemStackUtil.buildItem(Material.OAK_SIGN, "&e&l介绍",
				Lists.newArrayList("&8&o本功能最初概念提出者: ljzwal", "&7洗炼功能允许你将物品中的附魔提取出来", "&7需要到主城兑换洗炼石放入对应附魔的格子中",
						"&f注意: 放入的洗炼石越多， 洗炼失败几率越大", "&7在洗炼每本附魔书的时候， 都会根据洗炼石数量", "&7计算洗炼几率，成功几率是 &f1/洗炼石几率",
						"&7也就是1个洗炼石必成功，洗炼石越多，", "&7单本附魔书失败的几率越大", "&f请不要放多洗炼石，每个格子只需要放一个，放多了没用"));
		ItemStack itemBarrier = ItemStackUtil.buildItem(Material.BARRIER, "&c");
		ItemStack itemAnvil = ItemStackUtil.buildItem(Material.ANVIL, "&r");
		ItemStack itemTarget = item.clone();
		List<Ench> enchs = Ench.getEnchs(item.getEnchantments());
		for (int i = 0; i < 6; i++) {
			int p = 11 + i;
			if (i < enchs.size()) {
				Ench ench = enchs.get(i);
				ItemStack itemBook = ItemStackUtil.buildItem(Material.ENCHANTED_BOOK, "&e获得附魔书",
						Lists.newArrayList("&7要洗炼出这本附魔书，请在这本附魔书", "&7下面的空格中放入洗炼石"));
				itemBook.addUnsafeEnchantment(ench.getEnch(), ench.getLevel());
				inv.setItem(p, itemBook);
			} else {
				inv.setItem(p, itemBarrier);
				inv.setItem(p + 9, itemR);
			}
		}
		ItemStackUtil.setFrameItems(inv, itemB);
		ItemStackUtil.setRowItems(inv, 4, itemB);
		inv.setItem(10, itemTarget);
		inv.setItem(19, itemAnvil);

		inv.setItem(37, itemL);
		inv.setItem(38, itemG);
		inv.setItem(39, itemG);
		inv.setItem(40, itemG);
		inv.setItem(41, itemG);
		inv.setItem(42, itemG);
		inv.setItem(43, itemSign);
		return inv;
	}

	@Override
	public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot,
                        ItemStack currentItem, ItemStack cursor, InventoryView inv, InventoryClickEvent event) {

		if(event.isShiftClick()) {
			player.sendMessage("§7[§9末日社团§7]§e 禁止Shift点击!");
			event.setCancelled(true);
			return;
		}
		// 精妙的设计
		if (event.getRawSlot() - 9 > 0 && event.getRawSlot() < 54) {
			// 获取点击的格子上面的一格
			ItemStack item = inv.getItem(event.getRawSlot() - 9);
			// 如果不是附魔书就不允许移动
			if (item == null || item.getType() != Material.ENCHANTED_BOOK) {
				event.setCancelled(true);
			} else {
				// 检查指针上的物品是否为洗炼石
				ItemStack cur = event.getCursor();
				if (cur != null && !ItemStackUtil.hasLore(cur, "§b§a§e用途: 洗炼出物品上的附魔") && !cur.getType().equals(Material.AIR)) {
					
					player.sendMessage("§7[§9末日社团§7]§e 只能放入洗炼石!");
					event.setCancelled(true);
					return;
				}
			}
		}
		if (event.isLeftClick() && !event.isRightClick() && !event.isShiftClick()) {

			// 确认洗炼
			if (event.getRawSlot() == 37) {
				List<Ench> enchs = new ArrayList<>();
				for (int i = 11; i < 17; i++) {
					ItemStack itemBook = inv.getItem(i);
					if (itemBook == null || itemBook.getType() != Material.ENCHANTED_BOOK) {
						continue;
					}
					ItemStack itemStone = inv.getItem(i + 9);
					if (!ItemStackUtil.hasLore(itemStone, "§b§a§e用途: 洗炼出物品上的附魔")) {
						if (itemStone != null && itemStone.getType() != Material.AIR
								&& itemStone.getType() != Material.RED_STAINED_GLASS_PANE) {
							ItemStackUtil.giveItemToPlayer(player, "§7[§9末日社团§7] §e发现洗炼石格子卡了不正常的物品进去，已返还",
									"§7[§9末日社团§7] §e你的背包已满，物品已掉落到你附近", itemStone);
						}
						continue;
					}
					Map<Enchantment, Integer> m = itemBook.getEnchantments();
					for (Enchantment e : m.keySet()) {
						enchs.add(new Ench(e, m.get(e)));
					}
				}
				if (enchs.isEmpty()) {
					player.sendMessage("§7[§9末日社团§7] §e不需要洗炼");
					return;
				}
				double successLine = 1000.0D * (1.0D - 1.0D / enchs.size());
				List<ItemStack> returnItems = new ArrayList<>();
				for (Enchantment ench : item.getEnchantments().keySet()) {
					item.removeEnchantment(ench);
				}
				returnItems.add(item);
				for (Ench ench : enchs) {
					String enchName = ItemStackUtil.getEnchName(ench.getEnch());
					if (new Random().nextInt(1000) > successLine) {
						returnItems.add(ItemStackUtil.getEnchantedBook(ench.getEnch(), ench.getLevel()));
						player.sendMessage("§7[§9末日社团§7] §a你武器的附魔 §e" + enchName + " §a洗炼成功了");
					} else {
						player.sendMessage("§7[§9末日社团§7] §c你武器的附魔 §e" + enchName + " §c洗炼失败了");
					}
				}
				this.flagClose = true;
				player.closeInventory();
				if (!returnItems.isEmpty()) {
					ItemStackUtil.giveItemToPlayer(player, "§7[§9末日社团§7] §a洗炼结束，目标物品附魔已清除",
							"§7[§9末日社团§7] §e你的背包已满，物品已掉落到你附近", returnItems);
				} else {
					player.sendMessage("§7[§9末日社团§7] §e洗炼结束了，但是返回了空的物品列表，这是一个BUG，请联系管理员");
				}
				this.flagClose = false;
				return;
			}
		}
		
	}

	@Override
	public void onClose(InventoryView inv) {
		if (flagClose)
			return;
		List<ItemStack> items = new ArrayList<>();
		if (item == null || item.getType().equals(Material.AIR)) {
			player.sendMessage("§7[§9末日社团§7] §c界面中的目标武器不存在，无法归还");
		}
		for(int i = 11; i < 17; i++) {
			ItemStack itemBook = inv.getItem(i);
			if(itemBook == null) continue;
			if(!itemBook.getType().equals(Material.ENCHANTED_BOOK)) break;
			ItemStack itemStone = inv.getItem(i + 9);
			if(itemStone != null && !itemStone.getType().equals(Material.AIR)) items.add(itemStone);
		}
		ItemStackUtil.giveItemToPlayer(player, "§7[§9末日社团§7] §a你关闭了界面，取消洗炼，目标物品已归还", "§7[§9末日社团§7] §e你的背包已满，物品已掉落到你附近",
				items);
		
	}
}
