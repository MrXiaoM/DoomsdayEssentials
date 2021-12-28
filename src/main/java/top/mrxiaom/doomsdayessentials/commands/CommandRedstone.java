package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.command.CommandSender;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;

public class CommandRedstone extends ICommand {
	public CommandRedstone(Main plugin) {
		super(plugin, "redstone", new String[] {});
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		sender.sendMessage(I18n.tn("redstone-sign.help", true));
		return true;
	}
}
