package top.mrxiaom.doomsdayessentials.gui;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
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
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.api.IGui;
import top.mrxiaom.doomsdayessentials.configs.ParkourConfig.Parkour;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;

import java.util.ArrayList;
import java.util.List;

public class GuiParkours implements IGui {
	final Main main;
	final Player player;
	final boolean hasBackToMenuButton;
	public GuiParkours(Main main, Player player, boolean hasBackToMenuButton) {
		this.main = main;
		this.player = player;
		this.hasBackToMenuButton = hasBackToMenuButton;
	}

	final static String guiTitle = "§0§6§3§5§9末日社团 §8| §5跑酷";

	private static String getProcessBar(int process, int total, char processChar, int totalLength, char backColor,
			char foreColor) {
		StringBuilder result = new StringBuilder();
		double percent = (double) process / (double) total * (double) totalLength;
		for (int i = 0; i < totalLength; i++) {
			result.append(ChatColor.COLOR_CHAR).append(i < percent ? foreColor : backColor).append(processChar);
		}
		return result.toString();
	}
	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public Inventory newInventory() {
		Inventory inv = Bukkit.createInventory(null, 54, guiTitle);

		List<Parkour> parkourList = main.getParkoursConfig().all();
		for (int i = 0; i < parkourList.size(); i++) {
			Parkour parkour = parkourList.get(i);
			int checkpoint = main.getPlayerConfig().getConfig()
					.getInt(player.getName() + ".parkours." + parkour.getId() + ".checkpoint", -1);
			List<String> checkpoints = parkour.getCheckPoints();

			// 无进度: 草方块
			// 进行中: 皮革靴子
			// 已完成: 粘液块

			List<String> lore = new ArrayList<>();
			for (String s : parkour.getDescription()) {
				lore.add(ChatColor.translateAlternateColorCodes('&', s));
			}
			lore.add("§e服务器保存你的跑酷进度，但不提供存档读档功能");
			lore.add("§e就是搞你心态，诶就是玩~ §c跑酷系统当前测试中");
			lore.add("§f进度: §7" + (checkpoint + 1) + " §7/ §a" + checkpoints.size());
			lore.add(getProcessBar(checkpoint + 1, checkpoints.size(), '|', 50, '7', 'a'));
			lore.add("");
			lore.add("§a左键 §7| §f传送到该关卡起点");
			lore.add("§0" + parkour.getRes());
			ItemStack item = ItemStackUtil.buildItem(checkpoint == -1 ? Material.GRASS_BLOCK
							: checkpoint + 1 >= checkpoints.size() ? Material.SLIME_BLOCK : Material.LEATHER_BOOTS,
					ChatColor.translateAlternateColorCodes('&', "&b" + parkour.getDisplayName()),
					lore);
			inv.setItem(i, item);
		}

		ItemStack itemFrame = ItemStackUtil.buildItem(Material.WHITE_STAINED_GLASS_PANE, "&f*");
		ItemStack itemBack = ItemStackUtil.buildItem(Material.RED_STAINED_GLASS_PANE, "&c返回传送菜单");

		inv.setItem(45, itemFrame);
		inv.setItem(46, itemFrame);
		inv.setItem(47, itemFrame);
		inv.setItem(48, itemFrame);
		inv.setItem(49, hasBackToMenuButton ? itemBack : itemFrame);
		inv.setItem(50, itemFrame);
		inv.setItem(51, itemFrame);
		inv.setItem(52, itemFrame);
		inv.setItem(53, itemFrame);
		return inv;
	}

	@Override
	public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot,
                         ItemStack currentItem,  ItemStack cursor, InventoryView view, InventoryClickEvent event) {
		event.setCancelled(true);
		// 只允许左键点击
		if (event.isLeftClick() && !event.isRightClick() && !event.isShiftClick()) {
			InventoryView inv = event.getView();
			if (inv.getItem(event.getRawSlot()) == null) {
				return;
			}
			// 跑酷关卡
			if (event.getRawSlot() < 45) {
				ItemStack item = inv.getItem(event.getRawSlot());
				player.closeInventory();
				if (item != null) {
					List<String> lore = ItemStackUtil.getItemLore(item);
					if (lore.isEmpty()) return;
					String res = lore.get(lore.size() - 1).substring(2);
					if (res.length() > 0) {
						ClaimedResidence residence = ResidenceApi.getResidenceManager().getByName(res);
						if (residence != null) {
							Location loc = residence.getTeleportLocation(player);
							main.getBackConfig().addBackPoint(player, player.getLocation());
							player.teleport(loc);
							return;
						}
					}
				}
				player.sendMessage("§7[§9末日社团§7] §c错误: 欲传送的领地不存在，请联系管理员");
				return;
			}
			// 返回菜单
			if (event.getRawSlot() == 49) {
				ItemStack item49 = inv.getItem(49);
				if (item49 != null && item49.getType().equals(Material.RED_STAINED_GLASS_PANE)) {
					player.closeInventory();
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dm open 传送 " + player.getName());
				}
			}
		}
	}

	@Override
	public void onClose(InventoryView view) {
		// do nothing.
	}
}
