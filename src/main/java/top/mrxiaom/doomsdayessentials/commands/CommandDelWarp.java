package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.command.CommandSender;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class CommandDelWarp extends ICommand {
	public CommandDelWarp(Main plugin) {
		super(plugin, "delwarp", new String[] {});
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

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		boolean access = sender.isOp();
		if (!access) {
			return Util.noPerm(sender);
		}
		if (args.length == 1) {
			String warpName = args[0];
			if (!plugin.getWarpConfig().contains(warpName)) {
				sender.sendMessage("§7[§9末日社团§7]§c 地标不存在");
				return true;
			}
			plugin.getWarpConfig().remove(warpName);
			sender.sendMessage("§7[§9末日社团§7]§6 已删除地标 §c" + warpName);
			return true;
		}
		sender.sendMessage("§7[§9末日社团§7]§6 用法:§c /delwarp [地标名]");
		return true;
	}
}
