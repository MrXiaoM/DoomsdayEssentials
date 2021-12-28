package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.NMSUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

public class CommandClearEnch extends ICommand {
	public CommandClearEnch(Main plugin) {
		super(plugin, "clearench", new String[] {});
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (plugin.getEcoApi() == null) {
			return Util.noEcoApi(sender);
		}
		if (!isPlayer) {
			return Util.noPlayer(sender);
		}
		Player player = (Player) sender;
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("confirm")) {
				ItemStack item = player.getInventory().getItemInMainHand();
				if (item.getType().equals(Material.AIR)) {
					sender.sendMessage(I18n.t("clear-ench.air", true));
					return true;
				}
				ItemMeta im = item.hasItemMeta() ? item.getItemMeta()
						: NMSUtil.getMetaFormMaterial(item.getType());

				if (!im.hasEnchants()) {
					sender.sendMessage(I18n.t("clear-ench.no-ench", true));
					return true;
				}
				double cost = plugin.getConfig().getDouble("clear-ench-cost");
				if (plugin.getEcoApi().has(player, cost)) {
					plugin.getEcoApi().withdrawPlayer(player, cost);
					for (Enchantment enchantment : im.getEnchants().keySet()) {
						im.removeEnchant(enchantment);
					}
					item.setItemMeta(im);
					player.getInventory().setItemInMainHand(item);
					sender.sendMessage(I18n.t("clear-ench.success", true).replace("%cost%", String.valueOf(cost)));
					return true;
				} else {
					sender.sendMessage(I18n.t("clear-ench.no-money", true).replace("%cost%", String.valueOf(cost)));
					return true;
				}
			}
		}
		for (String s : I18n.l("clear-ench.help")) {
			player.sendMessage(s.replace("%cost%", String.valueOf(plugin.getConfig().getDouble("clear-ench-cost"))));
		}
		return true;
	}

}
