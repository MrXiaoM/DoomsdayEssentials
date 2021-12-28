package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.Util;

public class CommandTips extends ICommand {
	public CommandTips(Main plugin) {
		super(plugin, "tips", new String[] {});
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if(!isPlayer) {
			return Util.noPerm(sender);
		}
		Player player = (Player) sender;
		player.sendMessage("§a你知道吗\n§r" + plugin.getRandomTips());
		return true;
	}
}
