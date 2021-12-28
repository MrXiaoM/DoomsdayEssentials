package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.GunConfig.Gun;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.NMSUtil;
import top.mrxiaom.doomsdayessentials.utils.NMSUtil.NMSItemStack;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.Map;

public class CommandGun extends ICommand {
	public CommandGun(Main plugin) {
		super(plugin, "doomsdaygun", new String[] { "gun" });
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("look")) {
				if (!isPlayer) {
					return Util.noPlayer(sender);
				}
				Player player = (Player) sender;
				ItemStack item = player.getInventory().getItemInMainHand();
				if (item == null || !item.hasItemMeta()) {
					player.sendMessage(I18n.t("gun.item-not-correct", true));
					return true;
				}
				ItemMeta im = item.hasItemMeta() ? item.getItemMeta() : NMSUtil.getMetaFormMaterial(item.getType());
				if (!im.hasLore()) {
					player.sendMessage(I18n.t("gun.item-not-correct", true));
					return true;
				}
				String s = im.getLore().get(im.getLore().size() - 1).toLowerCase();
				NMSItemStack nms = NMSItemStack.fromBukkitItemStack(item);
				if (s.toLowerCase().startsWith("§g§u§n")) {
					int bullets = nms.getNBTTagInt("bullets", 0);
					player.sendMessage(I18n.tn("gun.look", true).replace("%bullets%", String.valueOf(bullets)));
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("give")) {
				if (!sender.isOp()) {
					return Util.noPerm(sender);
				}
				if (args.length < 2) {
					sender.sendMessage("参数过少");
					return true;
				}
				Player player = null;
				if (isPlayer) {
					player = (Player) sender;
				} else if (args.length < 3) {
					sender.sendMessage("请填写玩家名");
					return true;
				}
				if (args.length >= 3) {
					boolean flag = false;
					for (Player p : Bukkit.getOnlinePlayers()) {
						if (p.getName().equalsIgnoreCase(args[2])) {
							player = p;
							flag = true;
						}
					}
					if (!flag) {
						sender.sendMessage(I18n.t("not-online", true));
					}
				}
				String gunId = args[1];
				if (!plugin.getGunConfig().contains(gunId)) {
					sender.sendMessage(I18n.t("gun.not-found", true));
					return true;
				}
				Gun gun = plugin.getGunConfig().get(gunId);
				Map<Integer, ItemStack> lost = player.getInventory().addItem(gun.getItem());
				player.updateInventory();
				sender.sendMessage(I18n.t("gun.giving", true).replace("%gun%", gun.getName()).replace("%player%",
						player.getName()));
				player.sendMessage(I18n.t("gun.given", true).replace("%gun%", gun.getName()));
				if (!lost.isEmpty()) {
					player.sendMessage(I18n.t("gun.inv-full", true));
					for (ItemStack i : lost.values()) {
						player.getWorld().dropItem(player.getLocation(), i);
					}
				}
			}
		}
		return true;
	}

}
