package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.gui.GuiBackPoints;
import top.mrxiaom.doomsdayessentials.modules.reviveme.ReviveMeApi;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

public class CommandBack extends ICommand {
	public CommandBack(Main plugin) {
		super(plugin, "back", new String[] {});
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
		if (args.length == 1 && args[0].equalsIgnoreCase("gui")) {
			plugin.getGuiManager().openGui(new GuiBackPoints(plugin, player));
			return true;
		}
		Location loc = plugin.getBackConfig().getBackPoints(player.getName())[0];
		if (loc == null) {
			player.sendMessage(I18n.t("back.nopoint", true));
			return true;
		}
		if (player.hasPermission("doomteam.teleport.cooldown.bypass")) {
			plugin.getBackConfig().addBackPoint(player, player.getLocation());
			player.teleport(loc);
			player.sendMessage(I18n.t("back.teleport", true));
			return true;
		}
		if (plugin.getPlayerCooldownManager().isCooldown(playerName)) {
			plugin.getPlayerCooldownManager().cancelPlayerCooldownTask(playerName);
			player.sendMessage(I18n.t("teleport-move"));
		}
		sender.sendMessage(I18n.t("teleport-intime", true).replace("%time%", String.valueOf(3)));
		plugin.getPlayerCooldownManager().put(playerName,
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
				if (plugin.getPlayerCooldownManager().isCooldown(playerName)) {
					plugin.getPlayerCooldownManager().cancelPlayerCooldownTask(playerName);
				}
				plugin.getBackConfig().addBackPoint(player, player.getLocation());
				player.teleport(loc);
				sender.sendMessage(I18n.t("back.teleport", true));
			}, 3 * 20));
		return true;
	}
}
