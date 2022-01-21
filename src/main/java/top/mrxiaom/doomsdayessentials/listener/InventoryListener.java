package top.mrxiaom.doomsdayessentials.listener;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;
import top.mrxiaom.doomsdayessentials.utils.TimeUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InventoryListener implements Listener {
	final Main plugin;
	private final File cheatLogDir;

	public InventoryListener(Main plugin) {
		this.plugin = plugin;
		this.cheatLogDir = new File(plugin.getDataFolder(), "cheats");
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player))
			return;
		if (event.getClickedInventory() instanceof AnvilInventory) {
			List<String> lore = ItemStackUtil.getItemLore(event.getCurrentItem());
			if (!lore.isEmpty() && lore.get(0).toLowerCase().startsWith("§t§a§r§o§t")) {
				event.setCancelled(true);
				event.getWhoClicked().sendMessage("§7[§9末日社团§7] §c你不能将塔罗牌放入铁砧中");
			}
		}
	}
	@EventHandler
	public void onInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (plugin.getInventoryListener().checkInventory(event.getPlayer().getInventory(), event.getPlayer())) {
			event.getPlayer().sendMessage("§7[§9末日社团§7] §c§l发现你的背包內有两件绑定代码一致的物品，"
					+ "我们怀疑你有刷物品的嫌疑，系统已记录本次操作，如确有此事或误报请向服务器管理员反馈，" + "系统已自动没收涉事物品");
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.isOp()) {
					p.sendMessage("§7[§9末日社团§7] §c发现玩家 §e" + player.getName() + " §c有刷绑定物品的可能，"
							+ "日志已生成到 ./plugins/DoomsdayEssentials/cheats/ 下，请自行查阅");
				}
			}
		}
		ItemStack item = player.getInventory().getItemInMainHand();
		if (plugin.getBindConfig().needToRecall(item)) {
			event.getPlayer().sendMessage("§7[§9末日社团§7] §c物品 §e" + ItemStackUtil.getItemDisplayName(item) + " §c已被召回");
			player.getInventory().setItemInMainHand(null);
		}

	}
	@EventHandler
	public void onClick(InventoryClickEvent event) {
		
		if (checkInventory(event.getWhoClicked().getInventory(), event.getWhoClicked())) {
			event.getWhoClicked().sendMessage("§7[§9末日社团§7] §c§l发现你的背包內有两件绑定代码一致的物品，"
					+ "我们怀疑你有刷物品的嫌疑，系统已记录本次操作，如确有此事或误报请向服务器管理员反馈，" + "系统已自动没收涉事物品");
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.isOp()) {
					player.sendMessage("§7[§9末日社团§7] §c发现玩家 §e" + event.getWhoClicked().getName() + " §c有刷绑定物品的可能，"
							+ "日志已生成到 ./plugins/DoomsdayEssentials/cheats/ 下，请自行查阅");
				}
			}
		}
		ItemStack item = event.getCurrentItem();
		if (plugin.getBindConfig().needToRecall(item)) {
			event.getWhoClicked().sendMessage("§7[§9末日社团§7] §c物品 §e" + ItemStackUtil.getItemDisplayName(item) + " §c已被召回");
			event.setCurrentItem(null);
		}
	}

	@EventHandler
	public void onOpenInv(InventoryOpenEvent event) {
		if (checkInventory(event.getPlayer().getInventory(), event.getPlayer())) {
			event.getPlayer().sendMessage("§7[§9末日社团§7] §c§l发现你的背包內有两件绑定代码一致的物品，"
					+ "我们怀疑你有刷物品的嫌疑，系统已记录本次操作，如确有此事或误报请向服务器管理员反馈，" + "系统已自动没收涉事物品");
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.isOp()) {
					player.sendMessage("§7[§9末日社团§7] §c发现玩家 §e" + event.getPlayer().getName() + " §c有刷绑定物品的可能，"
							+ "日志已生成到 ./plugins/DoomsdayEssentials/cheats/ 下，请自行查阅");
				}
			}
		}
		if (event.getPlayer() instanceof Player) {
			checkPlayerInvBindingItems((Player) event.getPlayer());
		}
	}

	public void checkPlayerInvBindingItems(Player player) {
		for (int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack item = player.getInventory().getItem(i);
			if (plugin.getBindConfig().needToRecall(item)) {
				player.sendMessage("§7[§9末日社团§7] §c物品 §e" + ItemStackUtil.getItemDisplayName(item) + " §c已被召回");
				player.getInventory().setItem(i, null);
			}
		}
	}

	/*
	 * 检查
	 * 
	 * @returns 背包是否存在异常
	 */
	public boolean checkInventory(PlayerInventory inv, HumanEntity player) {
		boolean flag = false;
		boolean flag2;
		YamlConfiguration log = new YamlConfiguration();
		List<ItemStack> items = new ArrayList<>();
		for (int i = 0; i < inv.getSize(); i++) {
			ItemStack item1 = inv.getItem(i);
			flag2 = false;
			String id1 = plugin.getBindConfig().getCodeFromItemStack(item1);
			if (id1 != null) {
				for (int j = 0; j < inv.getSize() && j != i; j++) {
					ItemStack item2 = inv.getItem(j);
					String id2 = plugin.getBindConfig().getCodeFromItemStack(item2);
					if (id1.equalsIgnoreCase(id2)) {
						if (!flag2) {
							items.add(item1);
							flag2 = true;
						}
						items.add(item2);
						inv.setItem(i, null);
						inv.setItem(j, null);

						flag = true;
					}
				}
			}
		}
		if (flag) {
			log.set("player-name", player.getName());
			log.set("world", player.getWorld().getName());
			log.set("x", player.getLocation().getX());
			log.set("y", player.getLocation().getY());
			log.set("z", player.getLocation().getZ());
			log.set("items", ItemStackUtil.itemStackArrayToBase64(items.toArray(ItemStack[]::new), true));
			try {
				log.save(new File(cheatLogDir, player.getName() + "-" + TimeUtil.getDateTimeString().replace(":", ".") + ".yml"));
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		return flag;
	}

}
