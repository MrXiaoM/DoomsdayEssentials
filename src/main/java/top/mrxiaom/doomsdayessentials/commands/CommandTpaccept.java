package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.PlayerCooldownManager.TPRequest;
import top.mrxiaom.doomsdayessentials.modules.reviveme.ReviveMeApi;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class CommandTpaccept extends ICommand {
	public CommandTpaccept(Main plugin) {
		super(plugin, "tpaccept", new String[] { "tpaccept", "接tp", "tpyes" });
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, boolean isPlayer) {
		List<String> result = new ArrayList<>();
		if (!(sender instanceof Player))
			return result;
		result.add("ALL");
		if (args.length == 1) {
			for (TPRequest request : plugin.getPlayerCooldownManager().getTpRequests((Player) sender)) {
				if (request.getSender().getName().toLowerCase().startsWith(args[0].toLowerCase())) {
					result.add(request.getSender().getName());
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
		Player player = (Player) sender;
		if (ReviveMeApi.isPlayerDowned(player)){
			player.sendMessage(I18n.t("reviveme.no-command",true));
			return true;
		}
		List<TPRequest> requests = plugin.getPlayerCooldownManager().getTpRequests(player);
		if (requests.isEmpty()) {
			player.sendMessage(I18n.t("teleport.no-requests", true));
			return true;
		}

		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("list")) {
				StringBuilder str = new StringBuilder(I18n.t("teleport.multi-requests-accept", true));
				for (TPRequest request : requests) {
					String p1 = request.isTpahere() ? "你" : request.getSender().getName();
					String p2 = request.isTpahere() ? request.getSender().getName() : "你";
					str.append("\n").append(I18n.t("teleport.multi-requests-temp").replace("%player1%", p1)
							.replace("%player2%", p2).replace("%time%", String.valueOf(request.getTime())));
				}
				player.sendMessage(str.toString());
				return true;
			}
			if (args[0].equalsIgnoreCase("all")) {
				for (TPRequest request : requests) {
					request.accept();
				}
				return true;
			}
			Player target = this.getOnlinePlayer(args[0]);
			if (target == null) {
				player.sendMessage(I18n.t("not-online", true));
				return true;
			}
			TPRequest request = plugin.getPlayerCooldownManager().getTpRequest(target, player);
			if (request == null) {
				player.sendMessage(I18n.t("teleport.no-request", true));
				return true;
			}
			if(plugin.getPlayerConfig().getConfig().getBoolean(player.getName() + ".tips-while-teleport", false)) {
				player.sendMessage("§a你知道吗\n§r" + plugin.getRandomTips());
			}
			request.accept();
			return true;
		}
		if (requests.size() > 1) {
			StringBuilder str = new StringBuilder(I18n.t("teleport.multi-requests-accept", true));
			for (TPRequest request : requests) {
				String p1 = request.isTpahere() ? "你" : request.getSender().getName();
				String p2 = request.isTpahere() ? request.getSender().getName() : "你";
				str.append("\n").append(I18n.t("teleport.multi-requests-temp").replace("%player1%", p1).replace("%player2%", p2)
						.replace("%time%", String.valueOf(request.getTime())));
			}
			player.sendMessage(str.toString());
			return true;
		}
		if(plugin.getPlayerConfig().getConfig().getBoolean(player.getName() + ".tips-while-teleport", false)) {
			player.sendMessage("§a你知道吗\n§r" + plugin.getRandomTips());
		}
		requests.get(0).accept();
		return true;
	}
}
