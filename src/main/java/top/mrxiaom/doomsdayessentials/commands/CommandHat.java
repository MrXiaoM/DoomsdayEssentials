package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.Util;

public class CommandHat extends ICommand {
	public CommandHat(Main plugin) {
		super(plugin, "hat", new String[] {});
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (!isPlayer) {
			return Util.noPlayer(sender);
		}
		if (!sender.hasPermission("doomteam.hat")) {
			return Util.noPerm(sender);
		}

		Player user = (Player) sender;

		if (user.getInventory().getItemInMainHand().getType() != Material.AIR) {
			ItemStack hand = user.getInventory().getItemInMainHand();
			if (hand.getType().getMaxDurability() == 0) {
				PlayerInventory inv2 = user.getInventory();
				ItemStack head2 = inv2.getHelmet();
				inv2.setHelmet(hand);
				inv2.setItemInMainHand(head2);
				user.sendMessage("§7[§9末日社团§7] §6享受你的新帽子吧");
				return true;
			} else {
				user.sendMessage("§7[§9末日社团§7] §c错误:你无法使用这个物品作为帽子");
				return true;
			}
		} else {
			user.sendMessage("§7[§9末日社团§7] §c你必须把想要带的帽子拿在手中");
			return true;
		}
	}

}
