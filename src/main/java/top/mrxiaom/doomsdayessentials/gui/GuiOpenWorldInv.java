package top.mrxiaom.doomsdayessentials.gui;

import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.profile.PlayerProfile;
import com.onarandombox.multiverseinventories.profile.ProfileTypes;
import com.onarandombox.multiverseinventories.share.Sharables;
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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.api.IGui;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;

public class GuiOpenWorldInv implements IGui {
	final Main plugin;
	final Player player;
	boolean isEnderChest;
	public GuiOpenWorldInv(Main plugin, Player player, boolean isEnderChest) {
		this.plugin = plugin;
		this.player = player;
		this.isEnderChest = isEnderChest;
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public Inventory newInventory() {
		Inventory inv = Bukkit.createInventory(null, 54, "§0开放世界背包");
		if (player.getWorld().getName().equalsIgnoreCase(plugin.getOpenWorldListener().openWorldName)){
			ItemStack[] items = isEnderChest ? player.getEnderChest().getContents() : player.getInventory().getContents();
			if (items != null)
				for (int i = 0; i < items.length; i++) {
					inv.setItem(i, items[i]);
				}
			if (!isEnderChest) {
				ItemStack[] itemsArmour = player.getInventory().getArmorContents();
				if(itemsArmour != null)
					for (int i = 0; i<itemsArmour.length; i++){
						inv.setItem(36 + i, itemsArmour[i]);
					}
			}
		}
		else {
			MultiverseInventories mi = MultiverseInventories.getPlugin();
			PlayerProfile profile = mi.getWorldProfileContainerStore().getContainer("openworld").getPlayerData(ProfileTypes.SURVIVAL, player);
			ItemStack[] items = profile.get(isEnderChest ? Sharables.ENDER_CHEST : Sharables.INVENTORY);
			if (items != null)
				for (int i = 0; i < items.length; i++) {
					inv.setItem(i, items[i]);
				}
			if (!isEnderChest) {
				ItemStack[] itemsArmour = profile.get(Sharables.ARMOR);
				if (itemsArmour != null)
					for (int i = 0; i < itemsArmour.length; i++) {
						inv.setItem(36 + i, itemsArmour[i]);
					}
			}
		}
		ItemStackUtil.setRowItems(inv, 6, ItemStackUtil.buildFrameItem(Material.WHITE_STAINED_GLASS_PANE));
		ItemStack backBtn = ItemStackUtil.buildItem(Material.CLOCK, "&c返回菜单");
		backBtn.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
		backBtn.getItemMeta().addItemFlags(ItemFlag.HIDE_ENCHANTS);
		inv.setItem(49, backBtn);
		return inv;
	}

	@Override
	public void onClick(InventoryAction action, ClickType click, InventoryType.SlotType slotType, int slot,
                        ItemStack currentItem, ItemStack cursor, InventoryView view, InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.isLeftClick() && !event.isRightClick() && !event.isShiftClick()) {
			InventoryView inv = event.getView();
			// 返回菜单
			if (event.getRawSlot() == 49) {
				player.closeInventory();
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dm open 开放世界 " + player.getName());
			}
		}
	}

	@Override
	public void onClose(InventoryView view) {
		// do nothing.
	}
}
