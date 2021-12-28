package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class CommandDelHome extends ICommand {
	public CommandDelHome(Main plugin) {
		super(plugin, "delhome", new String[] {});
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, boolean isPlayer) {
		List<String> result = new ArrayList<String>();
		if (isPlayer) {
			if (args.length == 1) {
				for (String value : plugin.getHomeConfig().getHomes(sender.getName())) {
					if (value.startsWith(args[0])) {
						result.add(value);
					}
				}
			}
		}
		return result;
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (!isPlayer) {
			return Util.noPlayer(sender);
		}
		Player player = (Player) sender;
		String playerName = player.getName();
		String homeName = args.length != 0 ? args[0] : "1";
		if (!plugin.getHomeConfig().hasHome(playerName, homeName)) {
			player.sendMessage(I18n.t("home.nohome", true));
			return true;
		}
		plugin.getHomeConfig().delHome(playerName, homeName);
		player.sendMessage(I18n.t("home.delete", true).replace("%home%", homeName));

		return false;
	}
}
