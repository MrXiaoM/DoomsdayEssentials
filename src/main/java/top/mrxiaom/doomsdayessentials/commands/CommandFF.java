package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.command.CommandSender;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.TimeUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.time.LocalDateTime;

public class CommandFF extends ICommand {
	public CommandFF(Main plugin) {
		super(plugin, "funnyfunctions", new String[] { "ff" });
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (!sender.isOp()) {
			sender.sendMessage("§7超机密设置");
			return true;
		}

		return true;
	}
}
