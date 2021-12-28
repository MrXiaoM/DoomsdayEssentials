package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.WarpConfig.Warp;
import top.mrxiaom.doomsdayessentials.utils.I18n;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandWarp extends ICommand {
	public CommandWarp(Main plugin) {
		super(plugin, "warp", new String[] { "wtp" });
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, boolean isPlayer) {
		List<String> result = new ArrayList<String>();
		if (args.length == 1) {
			for (String value : plugin.getWarpConfig().getAllWarps()) {
				if (value.startsWith(args[0])) {
					result.add(value);
				}
			}
		}
		return result;
	}
	
	@Nullable
	public static Player getOnlinePlayer(String name) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player.getName().equalsIgnoreCase(name)) {
				return player;
			}
		}
		return null;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (!isPlayer) {
			if (args.length == 2) {
				Player player = getOnlinePlayer(args[0]);
				if(player == null) {
					sender.sendMessage(I18n.t("not-online"));
					return true;
				}
				if (!plugin.getWarpConfig().contains(args[1])) {
					sender.sendMessage(I18n.t("warp.nowarp", true));
					return true;
				}
				final Warp warp = plugin.getWarpConfig().get(args[1]);
				plugin.getBackConfig().addBackPoint(player, player.getLocation());
				warp.teleport(player);
				player.sendMessage(I18n.t("warp.teleport", true).replace("%warp%", warp.getName()));
				return true;
			}
			return true;
		}
		if (args.length >= 1) {
			final Player player = (Player) sender;
			final String playerName = player.getName();
			String warpName = args[0];
			if (!plugin.getWarpConfig().contains(warpName)) {
				sender.sendMessage(I18n.t("warp.nowarp", true));
				return true;
			}
			final Warp warp = plugin.getWarpConfig().get(warpName);
			final String wName = warp.getName();
			if (player.hasPermission("doomteam.teleport.cooldown.bypass")) {
				plugin.getBackConfig().addBackPoint(player, player.getLocation());
				warp.teleport(player);
				sender.sendMessage(I18n.t("warp.teleport", true).replace("%warp%", wName));
				return true;
			}

			if (plugin.getPlayerCooldownManager().isCooldown(playerName)) {
				plugin.getPlayerCooldownManager().cancelPlayerCooldownTask(playerName);
				player.sendMessage(I18n.t("teleport-move"));
			}
			if(plugin.getPlayerConfig().getConfig().getBoolean(player.getName() + ".tips-while-teleport", false)) {
				player.sendMessage("§a你知道吗\n§r" + plugin.getRandomTips());
			}
			sender.sendMessage(I18n.t("teleport-intime", true).replace("%time%", String.valueOf(3)));
			plugin.getPlayerCooldownManager().put(playerName,
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						@Override
						public void run() {
							if (plugin.getPlayerCooldownManager().isCooldown(playerName)) {
								plugin.getPlayerCooldownManager().cancelPlayerCooldownTask(playerName);
							}
							plugin.getBackConfig().addBackPoint(player, player.getLocation());
							warp.teleport(player);
							sender.sendMessage(I18n.t("warp.teleport", true).replace("%warp%", wName));
						}
					}, 3 * 20));
		}
		if (args.length == 0) {
			String warpMsg = I18n.t("warp.list.header", true);
			List<String> warps = plugin.getWarpConfig().getAllWarps();
			for (int i = 0; i < warps.size(); i++) {
				warpMsg = warpMsg + I18n.t("warp.list.prefix") + warps.get(i)
						+ ((i == warps.size() - 1) ? "" : I18n.t("warp.list.suffix"));
			}
			sender.sendMessage(warpMsg);
		}
		return true;
	}
}
