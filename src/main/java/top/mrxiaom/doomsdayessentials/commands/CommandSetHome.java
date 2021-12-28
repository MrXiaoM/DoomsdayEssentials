package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

public class CommandSetHome extends ICommand {
	public CommandSetHome(Main plugin) {
		super(plugin, "sethome", new String[] {});
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (!isPlayer) {
			return Util.noPlayer(sender);
		}
		Player player = (Player) sender;
		if (plugin.getParkoursConfig().getParkourPlayerIn(player) != null) {
			player.sendMessage(I18n.t("home.sethome-parkour", true));
			return true;
		}
		String playerName = player.getName();
		String homeName = args.length != 0 ? args[0] : "1";
		int homecount = plugin.getHomeConfig().getHomeCount(playerName);
		if (homecount > 3 && !plugin.getHomeConfig().hasHome(playerName, homeName)) {
			player.sendMessage(I18n.t("home.limited", true).replace("%count%", "3"));
			return true;
		}
		plugin.getHomeConfig().setHome(playerName, homeName, player.getLocation());
		player.sendMessage(I18n.t("home.sethome", true).replace("%home%", homeName));

		return true;
	}

}
