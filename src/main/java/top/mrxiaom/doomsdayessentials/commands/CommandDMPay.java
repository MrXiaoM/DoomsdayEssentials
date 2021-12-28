package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.Util;

public class CommandDMPay extends ICommand {
	public CommandDMPay(Main plugin) {
		super(plugin, "dmpay", new String[] {});
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (args.length == 1 && args[0].equalsIgnoreCase("refresh")) {
			if (!isPlayer) {
				return Util.noPlayer(sender);
			}
			plugin.checkPoints((Player) sender);
		}
		return true;
	}
}
