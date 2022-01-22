package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.modules.reviveme.ReviveMeApi;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandTpa extends ICommand {
	public CommandTpa(Main plugin) {
		super(plugin, "tpa", new String[] {});
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, boolean isPlayer) {
		List<String> result = new ArrayList<>();
		if (args.length == 1) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
					result.add(player.getName());
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
		if (args.length == 1) {
			Player player = (Player) sender;
			if (ReviveMeApi.isPlayerDowned(player)){
				player.sendMessage(I18n.t("reviveme.no-command",true));
				return true;
			}
			if (player.getName().equalsIgnoreCase(args[0])) {
				player.sendMessage(I18n.t("teleport.self", true));
				return true;
			}
			Player targetPlayer = Util.getOnlinePlayer(args[0]);
			if (targetPlayer == null) {
				player.sendMessage(I18n.t("not-online", true));
				return true;
			}
			if (!plugin.getPlayerCooldownManager().getTpRequestsSender(player).isEmpty()) {
				player.sendMessage(I18n.t("teleport.has-requests", true));
				return true;
			}
			plugin.getPlayerCooldownManager().putTpRequest(player, targetPlayer, false);
			player.sendMessage(I18n.t("teleport.sent", true).replace("%player%", targetPlayer.getName()));
			targetPlayer.sendMessage(
					I18n.tn("teleport.tpa", false).replace("%player%", player.getName()).replace("%time%", "120"));
			return true;
		}
		sender.sendMessage(I18n.t("teleport.tpa-help", true));
		return true;
	}
}
