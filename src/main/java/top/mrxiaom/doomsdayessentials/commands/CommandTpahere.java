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
import java.util.Optional;

public class CommandTpahere extends ICommand {
	public CommandTpahere(Main plugin) {
		super(plugin, "tpahere", new String[] {});
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
			Optional<Player> targetPlayer = Util.getOnlinePlayer(args[0]);
			if (targetPlayer.isEmpty()) {
				player.sendMessage(I18n.t("not-online", true));
				return true;
			}
			if(plugin.getChatListener().isIgnored(player, targetPlayer.get(), null)) {
				player.sendMessage(I18n.t("ignore.ignored", true));
				return true;
			}
			if (!plugin.getPlayerCooldownManager().getTpRequestsSender(player).isEmpty()) {
				player.sendMessage(I18n.t("teleport.has-requests"));
				return true;
			}
			plugin.getPlayerCooldownManager().putTpRequest(player, targetPlayer.get(), true);
			player.sendMessage(I18n.t("teleport.sent", true).replace("%player%", targetPlayer.get().getName()));
			targetPlayer.get().sendMessage(
					I18n.tn("teleport.tpahere", false).replace("%player%", player.getName()).replace("%time%", "120"));
			return true;
		}
		sender.sendMessage(I18n.t("teleport.tpahere-help", true));
		return true;
	}
}
