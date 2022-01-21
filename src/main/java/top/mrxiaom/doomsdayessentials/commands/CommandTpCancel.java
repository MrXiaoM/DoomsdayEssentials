package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.PlayerCooldownManager.TPRequest;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandTpCancel extends ICommand {
	public CommandTpCancel(Main plugin) {
		super(plugin, "tpcancel", new String[] {});
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, boolean isPlayer) {
		List<String> result = new ArrayList<String>();
		if (!(sender instanceof Player))
			return result;
		if (args.length == 1) {
			for (TPRequest request : plugin.getPlayerCooldownManager().getTpRequestsSender((Player) sender)) {
				if (request.getReceiver().getName().toLowerCase().startsWith(args[0].toLowerCase())) {
					result.add(request.getReceiver().getName());
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
		Player player = (Player) sender;
		List<TPRequest> requests = plugin.getPlayerCooldownManager().getTpRequestsSender(player);
		if (requests.isEmpty()) {
			player.sendMessage(I18n.t("teleport.no-requests", true));
			return true;
		}
		for (TPRequest request : requests) {
			request.cancelBySender();
		}
		return true;
	}
}
