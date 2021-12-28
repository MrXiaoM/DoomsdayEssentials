package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.List;

public class CommandHomes extends ICommand {
	public CommandHomes(Main plugin) {
		super(plugin, "homes", new String[] {});
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (!isPlayer) {
			return Util.noPlayer(sender);
		}
		Player player = (Player) sender;
		String playerName = player.getName();
		List<String> homes = plugin.getHomeConfig().getHomes(playerName);
		String s = homes.size() == 0 ? "空" : "";
		for (int i = 0; i < homes.size(); i++) {
			Location loc = plugin.getHomeConfig().getHome(playerName, homes.get(i));
			boolean flag = plugin.getParkoursConfig().getParkourByLoc(loc) != null;
			s += (flag ? "§c§m" : "§6") + homes.get(i) + ((i + 1 < homes.size()) ? "§7, " : "");
		}
		player.sendMessage(I18n.t("home.homes", true).replace("%list%", s));

		return true;
	}

}
