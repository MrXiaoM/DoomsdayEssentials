package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.Collection;
import java.util.logging.Logger;

public class CommandBroadcast extends ICommand {
	final Logger logger;

	public CommandBroadcast(Main plugin) {
		super(plugin, "broadcast", new String[] { "bc", "alert" });
		logger = Logger.getLogger("公告");
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (!sender.isOp()) {
			return Util.noPerm(sender);
		}
		if (args.length > 0) {
			String msg = "§7[§c公告§7] §6";
			for (String arg : args) {
				msg += arg.replace('&', ChatColor.COLOR_CHAR).replace("\\n", "\n");
			}

			final Collection<? extends Player> players = Bukkit.getOnlinePlayers();
			for (final Player player : players) {
				player.sendMessage(msg);
			}
			logger.info(msg);
			return true;
		} else {
			sender.sendMessage(I18n.t("no-bc-content", true));
			return true;
		}
	}
}
