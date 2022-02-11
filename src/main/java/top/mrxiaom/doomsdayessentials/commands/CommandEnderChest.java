package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

public class CommandEnderChest extends ICommand {
	public CommandEnderChest(Main plugin) {
		super(plugin, "enderchest", new String[] { "ec", "echest" });
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (!isPlayer) {
			return Util.noPlayer(sender);
		}
		if (!sender.hasPermission("lpt.enderchest")) {
			return Util.noPerm(sender);
		}
		Player player = (Player) sender;
		if (plugin.getOpenWorldListener().isInOpenWorld(player)){
			player.sendMessage(I18n.t("openworld.cmd-disallow", true));
			return true;
		}
		player.openInventory(player.getEnderChest());
		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
		return true;
	}

}
