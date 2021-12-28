package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.WarpConfig.Warp;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

public class CommandSetWarp extends ICommand {
	public CommandSetWarp(Main plugin) {
		super(plugin, "setwarp", new String[] {});
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		boolean access = sender.isOp();
		if (!isPlayer) {
			return Util.noPlayer(sender);
		}
		final Player player = (Player) sender;
		if (!access) {
			return Util.noPerm(sender);
		}
		if (args.length == 1) {
			String warpName = args[0];
			boolean contains = plugin.getWarpConfig().contains(warpName);
			plugin.getWarpConfig().set(warpName, new Warp(warpName, player.getLocation()));
			player.sendMessage(I18n.t("warp.setwarp.text", true)
					.replace("%type%", (contains ? I18n.t("warp.setwarp.cover") : I18n.t("warp.setwarp.set")))
					.replace("%warp%", warpName));
			return true;
		}
		sender.sendMessage(I18n.t("warp.setwarp.help"));
		return true;
	}
}
