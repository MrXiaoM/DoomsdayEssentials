package top.mrxiaom.doomsdayessentials.gui;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
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
import top.mrxiaom.doomsdayessentials.configs.MarketConfig.MarketData;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;
import top.mrxiaom.doomsdayessentials.utils.TimeUtil;

import java.time.LocalDateTime;

public class GuiMarket implements IGui {
	final Main plugin;
	final Player player;
	final MarketData data;
	public GuiMarket(Main plugin, Player player, MarketData data) {
		this.plugin = plugin;
		this.player = player;
		this.data = data;
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public Inventory newInventory() {
		Inventory inv = Bukkit.createInventory(null, 9, "§0§8§2§2§0摊位管理 " + data.getId());
		inv.setItem(0,
				ItemStackUtil.buildItem(Material.PAPER, "&e摊位信息", 
						"&7摊位: &f" + data.getId(), 
						"&7摊主: &f" + data.getOwner(),
						"&7到期时间: &f" + TimeUtil.getChineseTime(data.getOutdateTime()),
						"&7距离到期还有 &f" + TimeUtil.getChineseTimeBetweenNow(data.getOutdateTime())));
		inv.setItem(1,
				ItemStackUtil.buildItem(Material.GOLD_INGOT, "&e摊位续费", 
						"&7摊位到期后，摊位的箱子商店将会全部清空", 
						"&7请记得按时交费", 
						"&f费用: &e1000 新币/天",
						"", 
						"&a左键 &7| &f续费 1 天 (1000 新币)",
						"&b右键 &7| &f续费 5 天 (5000 新币)",
						"&eShift+左键 &7| &f续费 10 天 (10000 新币)", 
						"&dShift+右键 &7| &f续费 30 天 (30000 新币)"));

		inv.setItem(8,
				ItemStackUtil.buildItem(Material.BARRIER, "&c停止租用摊位", 
						"&7停止租用当前摊位，在停止时系统将会清空", 
						"&7摊位的箱子商店和牌子，请提前搬走你的物品",
						"&7管理组将不会处理因停止租用造成的物品丢失", 
						"&7在停止租用后，服务器将会返还你 剩余天数*500 的资金", 
						"&7剩余天数向下取整，若剩余天数小于1天，将不会返还资金",
						"&f现在停止租用可以获得 " + (TimeUtil.between(LocalDateTime.now(), data.getOutdateTime()).getDay() * 500) + " 新币",
						"",
						"&a左键 &7| &f停止租用该摊位"));
		return inv;
	}

	@Override
	public void onClick( InventoryAction action,  ClickType click, InventoryType. SlotType slotType, int slot,
                        ItemStack currentItem,  ItemStack cursor, InventoryView view, InventoryClickEvent event) {
		event.setCancelled(true);
		int rawSlot = event.getRawSlot();
		InventoryView inv = event.getView();
		if (inv.getItem(event.getRawSlot()) == null) {
			return;
		}
		if (rawSlot == 1 || rawSlot == 8) {
			if (data == null) {
				player.closeInventory();
				player.sendMessage("§7[§9末日社团§7] §c错误: 无法找到摊位数据");
				return;
			}
			if (data.getOwner() == null || !data.getOwner().equals(player.getName())) {
				player.closeInventory();
				player.sendMessage("§7[§9末日社团§7] §c你不是摊位主人， 无法进行此操作");
				return;
			}
			if (data.isOutdate()) {
				player.closeInventory();
				player.sendMessage("§7[§9末日社团§7] §c该摊位已过期， 无法进行此操作");
				return;
			}
			boolean left = event.isLeftClick();
			boolean right = event.isRightClick();
			boolean shift = event.isShiftClick();

			if (rawSlot == 1) {
				// Calendar outdateTime = data.getOutdateTime();
				
				// if(!player.isOp()) {
				// 	   player.sendMessage("§7[§9末日社团§7] §c存在严重bug，正在修复，敬请期待");
				//	   player.closeInventory();
				//	   return;
				// }
				if(!shift) {
					if(left && !right) {
						if(plugin.getEcoApi().getBalance(player) < 1000) {
							player.sendMessage("§7[§9末日社团§7] §e你的金钱不足， 无法进行此操作");
							return;
						}
						player.closeInventory();
						plugin.getEcoApi().withdrawPlayer(player, 1000);
						data.addOutdateTimeDay(1);
						plugin.getMarketConfig().putMarketData(data);
						player.sendMessage("§7[§9末日社团§7] §a你成功花费 §e1000 新币 §a来续费 §e" + data.getId() + " §a号摊位 §e1 §a天");
						return;
					}
					if(right && !left) {
						if(plugin.getEcoApi().getBalance(player) < 5000) {
							player.sendMessage("§7[§9末日社团§7] §e你的金钱不足， 无法进行此操作");
							return;
						}
						player.closeInventory();
						plugin.getEcoApi().withdrawPlayer(player, 5000);
						data.addOutdateTimeDay(5);
						plugin.getMarketConfig().putMarketData(data);
						player.sendMessage("§7[§9末日社团§7] §a你成功花费 §e5000 新币 §a来续费 §e" + data.getId() + " §a号摊位 §e5 §a天");
						return;
					}
				}
				else {
					if(left && !right) {
						if(plugin.getEcoApi().getBalance(player) < 10000) {
							player.sendMessage("§7[§9末日社团§7] §e你的金钱不足， 无法进行此操作");
							return;
						}
						player.closeInventory();
						plugin.getEcoApi().withdrawPlayer(player, 10000);
						data.addOutdateTimeDay(10);
						plugin.getMarketConfig().putMarketData(data);
						player.sendMessage("§7[§9末日社团§7] §a你成功花费 §e10000 新币 §a来续费 §e" + data.getId() + " §a号摊位 §e10 §a天");
						return;
					}
					if(right && !left) {
						if(plugin.getEcoApi().getBalance(player) < 30000) {
							player.sendMessage("§7[§9末日社团§7] §e你的金钱不足， 无法进行此操作");
							return;
						}
						player.closeInventory();
						plugin.getEcoApi().withdrawPlayer(player, 30000);
						data.addOutdateTimeDay(30);
						plugin.getMarketConfig().putMarketData(data);
						player.sendMessage("§7[§9末日社团§7] §a你成功花费 §e30000 新币 §a来续费 §e" + data.getId() + " §a号摊位 §e30 §a天");
						return;
					}
				}
			}
			if (rawSlot == 8) {
				if (left && !right && !shift) {
					player.closeInventory();
					int money = TimeUtil.between(LocalDateTime.now(), data.getOutdateTime()).getDay() * 500;
					if(money < 0) money = 0;
					if(money > 600000) {
						player.sendMessage("§7[§9末日社团§7] §a计算的目标金额似乎过高了， 请联系管理员反馈问题");
						return;
					}
					ClaimedResidence res = plugin.getMarketConfig().getMarketResidence(data.getId());
					if(res != null) {
						res.getPermissions().setPlayerFlag(Bukkit.getConsoleSender(), data.getOwner(), "container", "remove", true, false);
						res.getPermissions().setPlayerFlag(Bukkit.getConsoleSender(), data.getOwner(), "build", "remove", true, false);
						res.getPermissions().setPlayerFlag(Bukkit.getConsoleSender(), data.getOwner(), "destroy", "remove", true, false);
					}
					data.removeOutdateTime();
					data.setOwner("");
					plugin.getMarketConfig().putMarketData(data);
					plugin.getEcoApi().depositPlayer(player, money);
					plugin.getMarketConfig().onMarketRemoved(data.getId());
					player.sendMessage("§7[§9末日社团§7] §a你已成功停止租用你的摊位， 你获得了 §e" + money + " §a新币");
					return;
				}
			}
		}
	}

	@Override
	public void onClose(InventoryView view) {
		// do nothing.
	}
}
