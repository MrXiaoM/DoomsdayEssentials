package top.mrxiaom.doomsdayessentials.gui;

import com.google.common.collect.Lists;
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
import think.rpgitems.item.ItemManager;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.api.IGui;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;

public class GuiBlockSmitch implements IGui {
	final Main plugin;
	boolean flagClose = false;
	final Player player;
	public GuiBlockSmitch(Main main, Player player) {
		this.plugin = main;
		this.player = player;
	}

	public static boolean isEnableToBaptism(Material m) {
		return m.equals(Material.WOODEN_AXE) || m.equals(Material.WOODEN_HOE) || m.equals(Material.WOODEN_PICKAXE)
				|| m.equals(Material.WOODEN_SHOVEL) || m.equals(Material.WOODEN_SWORD) || m.equals(Material.STONE_AXE)
				|| m.equals(Material.STONE_HOE) || m.equals(Material.STONE_PICKAXE) || m.equals(Material.STONE_SHOVEL)
				|| m.equals(Material.STONE_SWORD) || m.equals(Material.IRON_AXE) || m.equals(Material.IRON_HOE)
				|| m.equals(Material.IRON_PICKAXE) || m.equals(Material.IRON_SHOVEL) || m.equals(Material.IRON_SWORD)
				|| m.equals(Material.GOLDEN_AXE) || m.equals(Material.GOLDEN_HOE) || m.equals(Material.GOLDEN_PICKAXE)
				|| m.equals(Material.GOLDEN_SHOVEL) || m.equals(Material.GOLDEN_SWORD) || m.equals(Material.DIAMOND_AXE)
				|| m.equals(Material.DIAMOND_HOE) || m.equals(Material.DIAMOND_PICKAXE)
				|| m.equals(Material.DIAMOND_SHOVEL) || m.equals(Material.DIAMOND_SWORD)
				|| m.equals(Material.LEATHER_HELMET) || m.equals(Material.LEATHER_CHESTPLATE)
				|| m.equals(Material.LEATHER_LEGGINGS) || m.equals(Material.LEATHER_BOOTS)
				|| m.equals(Material.CHAINMAIL_HELMET) || m.equals(Material.CHAINMAIL_CHESTPLATE)
				|| m.equals(Material.CHAINMAIL_LEGGINGS) || m.equals(Material.CHAINMAIL_BOOTS)
				|| m.equals(Material.IRON_HELMET) || m.equals(Material.IRON_CHESTPLATE)
				|| m.equals(Material.IRON_LEGGINGS) || m.equals(Material.IRON_BOOTS) || m.equals(Material.GOLDEN_HELMET)
				|| m.equals(Material.GOLDEN_CHESTPLATE) || m.equals(Material.GOLDEN_LEGGINGS)
				|| m.equals(Material.GOLDEN_BOOTS) || m.equals(Material.DIAMOND_HELMET)
				|| m.equals(Material.DIAMOND_CHESTPLATE) || m.equals(Material.DIAMOND_LEGGINGS)
				|| m.equals(Material.DIAMOND_BOOTS) || m.equals(Material.TURTLE_HELMET) || m.equals(Material.BOW)
				|| m.equals(Material.CROSSBOW) || m.equals(Material.TRIDENT) || m.equals(Material.SHIELD)
				|| m.equals(Material.FISHING_ROD) || m.equals(Material.SHEARS) || m.equals(Material.FLINT_AND_STEEL);
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public Inventory newInventory() {
		String guiTitle = "§0§8§3§7§0锻造台";
		Inventory inv = Bukkit.createInventory(null, 27, guiTitle);
		ItemStack itemB = ItemStackUtil.buildFrameItem(Material.BLACK_STAINED_GLASS_PANE);
		ItemStack itemBarrier = ItemStackUtil.buildItem(Material.BARRIER, "&c锻造物品", Lists.newArrayList("&7通过图纸和材料锻造出强大的装备或者稀有材料",
				"&7请把锻造图纸放入左边的格子中", "&7手持图纸直接右键可查看材料，这有助于你查看你是否已集齐材料", "&c暂未开放， 敬请期待"));
		ItemStack itemBaptism = ItemStackUtil.buildItem(Material.ANVIL, "&e洗炼物品",
				Lists.newArrayList("&7将你物品中的附魔提取成附魔书", "&7请把需要洗炼的物品放入左边的格子中", "&7这需要洗炼石，你需要在主城兑换获得"));
		ItemStackUtil.setFrameItemsSmall(inv, itemB);
		inv.setItem(11, itemB);
		inv.setItem(12, itemB);
		inv.setItem(13, itemB);
		inv.setItem(14, itemB);
		inv.setItem(15, itemBaptism);
		inv.setItem(16, itemBarrier);
		return inv;
	}

	@Override
	public void onClick( InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot,
                         ItemStack currentItem, ItemStack cursor, InventoryView inv, InventoryClickEvent event) {
		if (event.getRawSlot() != 10 && event.getRawSlot() < 27) {
			event.setCancelled(true);
		}
		if (event.isLeftClick() && !event.isRightClick() && !event.isShiftClick()) {
			if (inv.getItem(event.getRawSlot()) == null) {
				return;
			}
			// 洗炼
			if (event.getRawSlot() == 15) {
				ItemStack itemSlot = inv.getItem(10);
				if (itemSlot == null || itemSlot.getType() == Material.AIR
						|| itemSlot.getEnchantments().size() == 0) {
					player.sendMessage("§7[§9末日社团§7] §e请放入带附魔的物品");
					return;
				}
				if (!isEnableToBaptism(itemSlot.getType())) {
					player.sendMessage("§7[§9末日社团§7] §e这件物品不支持洗炼");
					return;
				}
				if (ItemManager.toRPGItemByMeta(itemSlot).orElse(null) != null
						// 没有被忘记的自我攻击剑
						|| plugin.getSkillSelfAttack().canItemStackRunSkill(itemSlot)) {
					player.sendMessage("§7[§9末日社团§7] §eRPG神器不支持洗炼");
					return;
				}
				this.flagClose = true;
				plugin.getGuiManager().openGui(new GuiBlockBaptism(plugin, player, itemSlot));
				this.flagClose = false;
				return;
			}
			if (event.getRawSlot() == 16) {
				player.sendMessage("§7[§9末日社团§7] §c锻造系统暂未开放");
			}
		}
		
	}

	@Override
	public void onClose(InventoryView inv) {
		if (flagClose)
			return;
		ItemStack item = inv.getItem(10);
		if (item == null || item.getType() == Material.AIR) {
			return;
		}
		ItemStackUtil.giveItemToPlayer(player, "§7[§9末日社团§7] §a你关闭了界面，目标物品已归还", "§7[§9末日社团§7] §e你的背包已满，物品已掉落到你附近", item);
	
	}
}
