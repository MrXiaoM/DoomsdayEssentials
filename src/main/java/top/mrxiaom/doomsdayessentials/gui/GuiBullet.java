package top.mrxiaom.doomsdayessentials.gui;

import net.minecraft.server.v1_15_R1.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemFactory;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
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
import top.mrxiaom.doomsdayessentials.configs.GunConfig.Gun;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.NMSUtil;

import java.util.Map;

public class GuiBullet implements IGui {
	final Main plugin;
	final Player player;
	public GuiBullet(Main plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
	}

	private void giveItem(Player player, ItemStack item) {
		Map<Integer, ItemStack> lost = player.getInventory().addItem(item);
		if (!lost.isEmpty()) {
			for (ItemStack i : lost.values()) {
				player.getWorld().dropItem(player.getLocation(), i);
			}
		}
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public Inventory newInventory() {
		String guiTitle = "§0§8§3§2" + I18n.t("gun.bullet-title");
		Inventory inv = Bukkit.createInventory(null, 9, guiTitle);

		ItemStack itemFrame = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
		ItemMeta im = itemFrame.hasItemMeta() ? itemFrame.getItemMeta()
				: CraftItemFactory.instance().getItemMeta(itemFrame.getType());
		im.setDisplayName(ChatColor.WHITE + "*");
		itemFrame.setItemMeta(im);

		ItemStack itemConfirm = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
		ItemMeta imConfirm = itemConfirm.hasItemMeta() ? itemConfirm.getItemMeta()
				: CraftItemFactory.instance().getItemMeta(itemConfirm.getType());
		imConfirm.setDisplayName("§a确认填充子弹");
		itemConfirm.setItemMeta(imConfirm);

		inv.setItem(1, itemFrame);
		inv.setItem(2, itemFrame);
		inv.setItem(3, itemFrame);
		inv.setItem(4, itemFrame);
		inv.setItem(5, itemFrame);
		inv.setItem(6, itemFrame);
		inv.setItem(7, itemFrame);
		inv.setItem(8, itemConfirm);
		return inv;
	}

	@Override
	public void onClick(InventoryAction action,  ClickType click, InventoryType.  SlotType slotType, int slot,
                     ItemStack currentItem,  ItemStack cursor, InventoryView view, InventoryClickEvent event) {
		if (slot > 0 && slot <= 8) {
			event.setCancelled(true);
		}
		if (event.isLeftClick() && !event.isRightClick() && !event.isShiftClick()) {
			InventoryView inv = event.getView();
			if (inv.getItem(slot) == null) {
				return;
			}

			// 确认填充子弹
			if (slot == 8) {
				if (inv.getItem(8).getType().equals(Material.LIME_STAINED_GLASS_PANE)) {
					ItemStack itemHand = player.getInventory().getItemInMainHand();
					if (itemHand.getType() == Material.AIR) {
						player.sendTitle(ChatColor.RED + "错误", ChatColor.YELLOW + "你手中的枪械无效", 10, 60, 10);
						player.closeInventory();
						return;
					}
					ItemMeta im = itemHand.hasItemMeta() ? itemHand.getItemMeta()
							: NMSUtil.getMetaFormMaterial(itemHand.getType());
					if (im == null || !im.hasLore() || im.getLore() == null
							|| !im.getLore().get(im.getLore().size() - 1).toLowerCase().startsWith("§g§u§n")) {
						player.sendTitle(ChatColor.RED + "错误", ChatColor.YELLOW + "你手中的枪械无效", 10, 60, 10);
						player.closeInventory();
						return;
					}
					net.minecraft.server.v1_15_R1.ItemStack nms = CraftItemStack.asNMSCopy(itemHand);
					String s = im.getLore().get(im.getLore().size() - 1).toLowerCase();
					String[] args = s.substring(6).contains(";") ? s.substring(6).split(";")
							: new String[] { s.substring(6) };
					String id = args[0].replace("§", "");
					NBTTagCompound tags = nms.hasTag() ? nms.getTag() : new NBTTagCompound();
					if(tags == null) tags = new NBTTagCompound();
					// Type Of NBTTagInt is 3
					if (!tags.hasKeyOfType("bullets", 3)) {
						tags.setInt("bullets", 0);
					}
					int oldbullets = tags.getInt("bullets");

					Gun gun = plugin.getGunConfig().get(id);
					ItemStack itemBullet = inv.getItem(0);
					if (itemBullet == null || itemBullet.getType() == Material.AIR) {
						player.sendTitle(ChatColor.RED + "错误", ChatColor.YELLOW + "你没有填入子弹", 10, 60, 10);
						inv.setItem(0, null);
						player.closeInventory();
						giveItem(player, itemBullet);
						return;
					}
					ItemMeta im2 = itemBullet.hasItemMeta() ? itemBullet.getItemMeta()
							: CraftItemFactory.instance().getItemMeta(itemBullet.getType());
					if (im2 == null || !im2.hasLore() || im2.getLore() == null
							|| !im2.getLore().get(im2.getLore().size() - 1).toLowerCase()
							.startsWith("§b§u§l§l§e§t")) {
						player.sendTitle(ChatColor.RED + "错误", ChatColor.YELLOW + "你填入的不是子弹", 10, 60, 10);
						inv.setItem(0, null);
						player.closeInventory();
						giveItem(player, itemBullet);
						return;
					}
					String bulletId = im2.getLore().get(im2.getLore().size() - 1).toLowerCase().substring(12)
							.replace("§", "");
					if (gun != null && !gun.getBullet().equals(bulletId)) {
						player.sendTitle(ChatColor.RED + "错误", ChatColor.YELLOW + "你填入子弹无法装入这把枪內", 10, 60, 10);
						inv.setItem(0, null);
						player.closeInventory();
						giveItem(player, itemBullet);
						return;
					}
					// 计算新的子弹数量
					int bullets = oldbullets + itemBullet.getAmount();
					inv.setItem(0, null);
					// 丢出多余子弹
					if (gun != null && bullets > gun.getBulletsInt()) {
						boolean bugflag = bullets - gun.getBulletsInt() > 64 || bullets - gun.getBulletsInt() < 1;
						itemBullet.setAmount(bullets - gun.getBulletsInt());
						bullets = gun.getBulletsInt();
						if (!bugflag)
							giveItem(player, itemBullet);
					}
					player.closeInventory();
					tags.setInt("bullets", bullets);
					nms.setTag(tags);
					player.getInventory().setItemInMainHand(CraftItemStack.asBukkitCopy(nms));
					player.sendTitle("§a装弹成功", "", 10, 60, 10);
				}

			}
		}
	}

	@Override
	public void onClose(InventoryView view) {
		ItemStack item = view.getItem(0);
		if(item != null) {
			giveItem(player, item);
			player.sendTitle(ChatColor.GREEN + "关闭界面", ChatColor.YELLOW + "剩余子弹已返还", 10, 60, 10);
		}
	}
}
