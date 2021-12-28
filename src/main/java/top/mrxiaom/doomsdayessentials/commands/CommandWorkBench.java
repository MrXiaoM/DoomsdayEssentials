package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.Util;

public class CommandWorkBench extends ICommand {
	public CommandWorkBench(Main plugin) {
		super(plugin, "workbench", new String[] { "wb", "ewb" });
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (!isPlayer) {
			return Util.noPlayer(sender);
		}
		if (!sender.hasPermission("lpt.workbench")) {
			return Util.noPerm(sender);
		}
		Player player = (Player) sender;
		player.openWorkbench(player.getLocation(), true);
		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
		return true;
	}

}
