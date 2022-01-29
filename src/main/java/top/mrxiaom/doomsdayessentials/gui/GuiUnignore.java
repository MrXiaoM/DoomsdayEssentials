package top.mrxiaom.doomsdayessentials.gui;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class GuiUnignore implements IGui{
	public enum IgnoreType {
		PLAYER,
		PLAYER_REGEX,
		MSG,
		MSG_REGEX
	}
	final Main plugin;
	final Player player;
	IgnoreType type;
	int page;
	public boolean isMenu = false;
	Map<Integer, String> ignoreTexts = new HashMap<>();
	public GuiUnignore(Main plugin, Player player, IgnoreType type, int page) {
		this.plugin = plugin;
		this.player = player;
		this.type = type;
		this.page = page;
	}
	public IgnoreType nextType(){
		return IgnoreType.values()[(type.ordinal() + 1) < IgnoreType.values().length ? type.ordinal() + 1 : 0];
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	public List<String> getPlayerIgnoreList(){
		switch (type){
			case PLAYER:
				return plugin.getPlayerConfig().getIgnorePlayers(player.getName(), false);
			case PLAYER_REGEX:
				return plugin.getPlayerConfig().getIgnorePlayers(player.getName(), true);
			case MSG:
				return plugin.getPlayerConfig().getIgnoreMsgs(player.getName(), false);
			case MSG_REGEX:
				return plugin.getPlayerConfig().getIgnoreMsgs(player.getName(), true);
			default:
				return Lists.newArrayList();
		}
	}

	@Override
	public Inventory newInventory() {
		Inventory inv = Bukkit.getServer().createInventory(null, 54, "§f§a§7§0取消屏蔽§r" + "§8 第" + page + "页");
		List<String> ignoreList = this.getPlayerIgnoreList();
		int maxpages = (int) Math.ceil(ignoreList.size() / 45.0D);
		AtomicInteger i = new AtomicInteger();
		AtomicInteger j = new AtomicInteger();
		ignoreTexts.clear();
		ignoreList.forEach(s -> {
			if (j.getAndIncrement() < (page - 1) * 45) return;
			if (i.get() >= 45) return;
			List<String> lore = new ArrayList<>();
			lore.add("§7内容: ");
			ignoreTexts.put(i.get(), s);
			if(s.length() <= 20) lore.add("§f" + s);
			else {
				String str = s;
				while(str.length() > 20){
					lore.add("§f" + str.substring(0, 20));
					str = str.substring(20);
				}
			}
			lore.add("");
			lore.add("&a左键 &7| &f发送到聊天栏，方便预览或复制");
			lore.add("&eShift+左键 &7| &f删除此条目");
			inv.setItem(i.get(),ItemStackUtil.buildItem(Material.PAPER,"§c屏蔽条目", lore));
		});
		ItemStackUtil.setRowItems(inv, 6, ItemStackUtil.buildFrameItem(Material.WHITE_STAINED_GLASS_PANE));
		if(page >= 1) inv.setItem(45, ItemStackUtil.buildItem(Material.GREEN_STAINED_GLASS_PANE, "§a上一页"));
		if(page < maxpages) inv.setItem(53, ItemStackUtil.buildItem(Material.GREEN_STAINED_GLASS_PANE, "§a上一页"));
		inv.setItem(47, ItemStackUtil.buildItem(Material.NAME_TAG, "§e屏蔽类型", Lists.newArrayList(
				"",
				(type.equals(IgnoreType.PLAYER) ? "§e> §f" : "§7") + "玩家名",
				(type.equals(IgnoreType.PLAYER_REGEX) ? "§e> §f" : "§7") + "玩家名(正则表达式)",
				(type.equals(IgnoreType.MSG) ? "§e> §f" : "§7") + "消息关键词",
				(type.equals(IgnoreType.MSG_REGEX) ? "§e> §f" : "§7") + "消息正则表达式",
				"",
				"§a左键 §7| §f切换显示的屏蔽类型")));
		if(isMenu) inv.setItem(49, ItemStackUtil.buildItem(Material.RED_STAINED_GLASS_PANE, "§c返回菜单"));
		return inv;
	}

	public void removeIgnore(String s) {
		switch (type){
			case PLAYER: plugin.getPlayerConfig().ignoreRemovePlayer(player.getName(), s, false);
			case PLAYER_REGEX: plugin.getPlayerConfig().ignoreRemovePlayer(player.getName(), s, true);
			case MSG: plugin.getPlayerConfig().ignoreRemoveMsg(player.getName(), s, false);
			case MSG_REGEX: plugin.getPlayerConfig().ignoreRemoveMsg(player.getName(), s, true);
		}
	}

	@Override
	public void onClick(InventoryAction action,  ClickType click, InventoryType.SlotType slotType, int slot,
                       ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
		event.setCancelled(true);
		if(event.getCurrentItem() == null || event.getCurrentItem().getType().isAir()) return;
		if (slot < 45) {
			if(event.isLeftClick()){
				// 删除条目
				if (event.isShiftClick()){
					removeIgnore(ignoreTexts.get(slot));
					refresh();
				}
				// 聊天显示
				else {
					player.sendMessage(I18n.prefix() + "§a该屏蔽条目内容如下: §f" + ignoreTexts.get(slot));
				}
			}
		}
		// 上一页
		if(slot == 45 && currentItem.getType().equals(Material.GREEN_STAINED_GLASS_PANE)){
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
			page--;
			refresh();
		}
		// 下一页
		if(slot == 53 && currentItem.getType().equals(Material.GREEN_STAINED_GLASS_PANE)){
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
			page++;
			refresh();
		}
		// 返回菜单
		if(isMenu && slot == 49){
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dm open 主菜单 " + player.getName());
		}
		// 切换类型
		if(slot == 47 && event.isLeftClick() && !event.isShiftClick()) {
			this.type = nextType();
			player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0F, 1.0F);
			refresh();
		}
	}

	@Override
	public void onClose(InventoryView view) {
		// do nothing.
	}
}
