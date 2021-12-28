package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandTpahere extends ICommand {
	public CommandTpahere(Main plugin) {
		super(plugin, "tpahere", new String[] {});
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, boolean isPlayer) {
		List<String> result = new ArrayList<String>();
		if (args.length == 1) {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
					result.add(player.getName());
				}
			}
		}
		return result;
	}

	@Nullable
	public Player getOnlinePlayer(String name) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getName().equalsIgnoreCase(name)) {
				return p;
			}
		}
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (!isPlayer) {
			return Util.noPlayer(sender);
		}
		if (args.length == 1) {
			Player player = (Player) sender;
			if (player.getName().equalsIgnoreCase(args[0])) {
				player.sendMessage(I18n.t("teleport.self", true));
				return true;
			}
			Player targetPlayer = this.getOnlinePlayer(args[0]);
			if (targetPlayer == null) {
				player.sendMessage(I18n.t("not-online", true));
				return true;
			}
			if (!plugin.getPlayerCooldownManager().getTpRequestsSender(player).isEmpty()) {
				player.sendMessage(I18n.t("teleport.has-requests"));
				return true;
			}
			plugin.getPlayerCooldownManager().putTpRequest(player, targetPlayer, true);
			player.sendMessage(I18n.t("teleport.sent", true).replace("%player%", targetPlayer.getName()));
			targetPlayer.sendMessage(
					I18n.tn("teleport.tpahere", false).replace("%player%", player.getName()).replace("%time%", "120"));
			return true;
		}
		sender.sendMessage(I18n.t("teleport.tpahere-help", true));
		return true;
	}
}
