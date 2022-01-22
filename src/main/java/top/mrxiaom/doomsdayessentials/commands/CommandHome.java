package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.modules.reviveme.ReviveMeApi;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class CommandHome extends ICommand {
	public CommandHome(Main plugin) {
		super(plugin, "home", new String[] {});
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, boolean isPlayer) {
		List<String> result = new ArrayList<>();
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
		if (ReviveMeApi.isPlayerDowned(player)){
			player.sendMessage(I18n.t("reviveme.no-command",true));
			return true;
		}
		String playerName = player.getName();
		String homeName = args.length != 0 ? args[0] : "1";
		if (!plugin.getHomeConfig().hasHome(playerName, homeName)) {
			player.sendMessage(I18n.t("home.nohome", true));
			return true;
		}
		Location loc = plugin.getHomeConfig().getHome(playerName, homeName);
		if (loc == null){
			player.sendMessage(I18n.t("home.nohome", true));
			return true;
		}
		if (plugin.getParkoursConfig().getParkourByLoc(loc) != null) {
			player.sendMessage(I18n.t("home.home-parkour", true));
			return true;
		}
		if (player.hasPermission("doomteam.teleport.cooldown.bypass")) {
			plugin.getBackConfig().addBackPoint(player, player.getLocation());
			player.teleport(loc);
			sender.sendMessage(I18n.t("home.teleport", true).replace("%home%", homeName));
			return true;
		}
		if (plugin.getPlayerCooldownManager().isCooldown(playerName)) {
			plugin.getPlayerCooldownManager().cancelPlayerCooldownTask(playerName);
		}
		sender.sendMessage(I18n.t("teleport-intime", true).replace("%time%", "3"));
		plugin.getPlayerCooldownManager().put(playerName,
				Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
					if (plugin.getPlayerCooldownManager().isCooldown(playerName)) {
						plugin.getPlayerCooldownManager().cancelPlayerCooldownTask(playerName);
					}
					plugin.getBackConfig().addBackPoint(player, player.getLocation());
					player.teleport(loc);
					sender.sendMessage(I18n.t("home.teleport", true).replace("%home%", homeName));
				}, 3 * 20));
		return true;
	}

}
