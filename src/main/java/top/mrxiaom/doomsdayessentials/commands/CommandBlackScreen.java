package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.TimeUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class CommandBlackScreen extends ICommand {
	public CommandBlackScreen(Main plugin) {
		super(plugin, "blackscreen", new String[] {});
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (!sender.isOp()) return Util.noPerm(sender);
		if (args.length == 2) {
			Optional<Player> player = Util.getOnlinePlayer(args[1]);
			if (player.isEmpty()) return true;
			if (args[0].equalsIgnoreCase("start")) {
				plugin.getBlackScreenManager().startBlackScreen(player.get());
				return true;
			}
			if (args[0].equalsIgnoreCase("end")) {
				plugin.getBlackScreenManager().endBlackScreen(player.get());
				return true;
			}
		}
		return true;
	}
}
