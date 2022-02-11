package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.TimeUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class CommandTpo extends ICommand {
	public CommandTpo(Main plugin) {
		super(plugin, "tpo", new String[] {});
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (!isPlayer) return Util.noPerm(sender);
		Player player = (Player) sender;
		if (plugin.getCmdManager().getCommandInstance(CommandGameMode.class).hasOpenWorldPerm(player)) {
			if (args.length == 3){
				try {
					boolean bx = args[0].startsWith("~");
					boolean by = args[1].startsWith("~");
					boolean bz = args[2].startsWith("~");
					Location loc = player.getLocation();
					double x = (bx ? loc.getX() : 0) + (bx && args[0].length() == 1 ? 0 : Double.parseDouble(bx ? args[0].substring(1) : args[0]));
					double y = (by ? loc.getY() : 0) + (by && args[1].length() == 1 ? 0 : Double.parseDouble(by ? args[1].substring(1) : args[1]));
					double z = (bz ? loc.getZ() : 0) + (bz && args[2].length() == 1 ? 0 : Double.parseDouble(bz ? args[2].substring(1) : args[2]));
					player.teleport(new Location(player.getWorld(), x, y, z, loc.getYaw(), loc.getPitch()));
					player.sendMessage("已传送到 §a" + x + "§f, §a" + y + "§f, §a" + z);
				} catch (Throwable t) {
					player.sendMessage("错误: " + t.getMessage());
				}
				return true;
			}
			player.sendMessage("用法: /tpo <x> <y> <z>");
			return true;
		}
		return true;
	}
}
