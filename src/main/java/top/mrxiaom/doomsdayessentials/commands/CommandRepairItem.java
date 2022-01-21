package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import think.rpgitems.RPGItems;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.NMSUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

public class CommandRepairItem extends ICommand {
	public CommandRepairItem(Main plugin) {
		super(plugin, "repairitem", new String[] {});
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
					sender.sendMessage("§7[§9末日社团§7]§6 你无法修复你的手");
					return true;
				}
				ItemMeta im = item.hasItemMeta() ? item.getItemMeta()
						: NMSUtil.getMetaFormMaterial(item.getType());
				RPGItem rpgItem = ItemManager.toRPGItem(item).orElse(null);
				boolean hasDur = rpgItem != null && rpgItem.getMaxDurability() > 0;
				if (im == null || (rpgItem != null && !hasDur) || (rpgItem == null && !(im instanceof Damageable))) {
					sender.sendMessage("§7[§9末日社团§7]§6 你手中的物品无法修复");
					return true;
				}
				if (!((Damageable) im).hasDamage() || ((Damageable) im).getDamage() == 0 ||
					(hasDur && rpgItem.getItemStackDurability(item).orElse(-1) >= rpgItem.getMaxDurability())) {
					sender.sendMessage("§7[§9末日社团§7]§6 你手中的物品不需要修复");
					return true;
				}
				double cost = plugin.getConfig().getDouble("repair-cost");
				if (plugin.getEcoApi().has(player, cost)) {
					plugin.getEcoApi().withdrawPlayer(player, cost);
					if(hasDur){
						rpgItem.setItemStackDurability(item, rpgItem.getMaxDurability());
					}
					else {
						((Damageable) im).setDamage(0);
						item.setItemMeta(im);
					}
					player.getInventory().setItemInMainHand(item);
					sender.sendMessage("§7[§9末日社团§7]§6 你成功花费了 §c" + cost + " §6新币修复了你手中的物品");
					return true;
				} else {
					sender.sendMessage("§7[§9末日社团§7]§6 你的金钱不足，需要 §c" + cost + " §6新币来修复你手中的物品");
					return true;
				}
			}
		}
		player.sendMessage("§7[§9末日社团§7] §b/repairitem confirm §f- §6修理手中物品\n" + "§7[§9末日社团§7] §6提示: 你需要花费 §c"
				+ plugin.getConfig().getDouble("repair-cost") + " §6新币修复手中的物品");
		return true;
	}

}
