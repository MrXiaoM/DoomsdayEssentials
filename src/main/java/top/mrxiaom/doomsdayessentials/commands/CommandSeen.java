package top.mrxiaom.doomsdayessentials.commands;

import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.SQLQuery;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.TimeUtil;
import top.mrxiaom.pluginupdater.Main.PlayerOnlineStatus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CommandSeen extends ICommand {
	public CommandSeen(Main plugin) {
		super(plugin, "seen", new String[] {});
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, boolean isPlayer) {
		List<String> result = new ArrayList<String>();
		if (isPlayer) {
			if (args.length == 1) {
				for (Player value : Bukkit.getOnlinePlayers()) {
					if (value.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
						result.add(value.getName());
					}
				}
			}
		}
		return result;
	}

	public OfflinePlayer getOfflinePlayer(String name) {
		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			if (player == null || player.getName() == null)
				continue;
			if (player.getName().equalsIgnoreCase(name)) {
				return player;
			}
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (args.length < 1) {
			sender.sendMessage("§7[§9末日社团§7] §6用法: §c/seen <玩家>");
			return true;
		}
		String playerName = args[0];
		if (getOfflinePlayer(playerName) == null) {
			sender.sendMessage("§7[§9末日社团§7] §c错误: 玩家不存在");
			return true;
		}

		OfflinePlayer targetPlayer = getOfflinePlayer(playerName);
		if (targetPlayer.isOnline()) {
			if (!plugin.getUpdaterApi().getPlayerIPMap().containsKey(playerName)) {
				sender.sendMessage("§7[§9末日社团§7] §c错误: 玩家在线，但无法找到在线数据");
				return true;
			}
			PlayerOnlineStatus status = plugin.getUpdaterApi().getPlayerIPMap().get(playerName);
			if (status == null) {
				sender.sendMessage("§7[§9末日社团§7] §c错误: 玩家在线，但无法找到在线数据");
				return true;
			}
			long now = Calendar.getInstance().getTimeInMillis();
			long last = plugin.getUpdaterApi().getPlayerIPMap().get(playerName).getLoginTime();
			// TODO 更换过期接口
			String time = TimeUtil.getChineseTime_Old(now - last, "§c", "§6");
			sender.sendMessage("§7[§9末日社团§7] §6玩家 §b" + playerName + " §6已§a在线 " + time);
			if (sender.isOp()) {
				sender.sendMessage("§7[§9末日社团§7] §6目标IP地址: §c"
						+ plugin.getUpdaterApi().getPlayerIPMap().get(playerName).getIpAddress());
			}
			return true;
		} else {
			long now = Calendar.getInstance().getTimeInMillis();
			long last = targetPlayer.getLastPlayed();
			String time = TimeUtil.getChineseTime_Old(now - last, "§c", "§6");
			sender.sendMessage("§7[§9末日社团§7] §6玩家 §b" + playerName + " §6已§4离线 " + time);
			try {
				List<Punishment> pList = PunishmentManager.get().getPunishments(SQLQuery.SELECT_ALL_PUNISHMENTS_LIMIT,
						150);
				for (Punishment p : pList) {
					if (p.getName().equalsIgnoreCase(playerName)) {
						sender.sendMessage("§7[§9末日社团§7] §6该玩家正在被处罚中\n" + "§7[§9末日社团§7] 处罚类型: §c"
								+ p.getType().getName() + "\n" + "§7[§9末日社团§7] 处罚原因: §c" + p.getReason() + "\n"
								+ "§7[§9末日社团§7] 操作者: §c" + p.getOperator());
					}
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
			if (sender.isOp()) {
				sender.sendMessage("§7[§9末日社团§7] §6目标IP地址: §c" + plugin.getPlayerConfig().getConfig()
						.getString(targetPlayer.getName() + ".ip-address", "unknown"));
			}
			return true;
		}
	}

}
