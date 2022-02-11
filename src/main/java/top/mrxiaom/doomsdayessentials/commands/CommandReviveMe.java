package top.mrxiaom.doomsdayessentials.commands;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;

import java.util.ArrayList;
import java.util.List;

public class CommandReviveMe extends ICommand {
	public CommandReviveMe(Main plugin) {
		super(plugin, "reviveme", new String[] { "re" });
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (args.length == 0) {
			sender.sendMessage("§c用法: §e/reviveme Help");
			return true;
		} else if (args[0].equalsIgnoreCase("reload")) {
			return this.reload(sender, args);
		} else if (args[0].equalsIgnoreCase("revive")) {
			return this.revive(sender, args);
		} else if (args[0].equalsIgnoreCase("down")) {
			return this.down(sender, args);
		} else if (args[0].equalsIgnoreCase("help")) {
			return this.help(sender, args);
		} else if (args[0].equalsIgnoreCase("debug")) {
			return this.debug(sender, args);
		} else if (args[0].equalsIgnoreCase("mdisable")) {
			return this.disable(sender, args);
		} else {
			sender.sendMessage("§c用法: §e/Reviveme Help");
			return true;
		}
	}

	private boolean disable(CommandSender s, String[] a) {
		this.plugin.getModuleReviveMe().getConfig().set("newVersionPremiumMessageDisable", true);
		this.plugin.getModuleReviveMe().saveConfig();
		plugin.getModuleReviveMe().getManager().disable_message = true;
		s.sendMessage("§cMessage disabled :c");
		return true;
	}

	public boolean reload(CommandSender s, String[] a) {
		if (plugin.getModuleReviveMe().getManager().hasPermission(s, "ReviveMe.reload")) {
			this.plugin.getModuleReviveMe().reloadConfig();
			plugin.getModuleReviveMe().getManager().onReload();
			s.sendMessage(ChatColor.translateAlternateColorCodes('&', I18n.t("reviveme.reload")));
		}

		return true;
	}

	public boolean revive(CommandSender s, String[] a) {
		if (a.length == 2) {
			if (a[0].equalsIgnoreCase("revive")) {
				if (plugin.getModuleReviveMe().getManager().hasPermission(s, "ReviveMe.revive.others")) {
					String p2s = a[1];
					if (Bukkit.getPlayerExact(p2s) != null) {
						Player p2 = Bukkit.getPlayer(p2s);
						if (plugin.getModuleReviveMe().getManager().isDamaged(p2)) {
							plugin.getModuleReviveMe().getManager().endPose(p2, "Command. code: 005");
						} else {
							s.sendMessage("§c玩家 " + p2s + " 还没有倒地");
						}
					} else {
						s.sendMessage("§c该玩家不在线");
					}
				}

				return true;
			}
		} else {
			s.sendMessage("§c用法:  §e/reviveme revive §c<玩家>");
		}

		return true;
	}

	public boolean down(CommandSender s, String[] a) {
		if (a.length == 2) {
			if (plugin.getModuleReviveMe().getManager().hasPermission(s, "ReviveMe.down.others")) {
				Player p2 = Bukkit.getPlayer(a[1]);
				if (p2 != null) {
					if (!plugin.getModuleReviveMe().getManager().isDamaged(p2)) {
						plugin.getModuleReviveMe().getManager().startPose(p2, "Command. code: 006");
						s.sendMessage("§a现在玩家 " + a[1] + " 倒地了");
					} else {
						s.sendMessage("§c玩家 " + a[1] + " 已经倒地了");
					}
				} else {
					s.sendMessage("§c玩家 " + a[1] + " 不在线");
				}
			}
		} else {
			s.sendMessage("§c用法: §e/reviveme down §c<玩家>");
		}

		return true;
	}

	public boolean help(CommandSender s, String[] a) {
		PluginDescriptionFile pdf = this.plugin.getDescription();
		String version = pdf.getVersion();
		s.sendMessage("§3[§c*§3]    §c§l我已§e§l重伤倒地    §3[§c*§3]");
		s.sendMessage("    §7版本: " + version);
		s.sendMessage("§3| §e/reviveme help §3- 查看帮助");
		s.sendMessage("§3| §e/reviveme revive §c<玩家> §3- 救助玩家");
		s.sendMessage("§3| §e/reviveme reload §3- 重载配置");
		return true;
	}

	public boolean debug(CommandSender s, String[] a) {
		PluginDescriptionFile pdf = this.plugin.getDescription();
		String version = pdf.getVersion();
		if (s instanceof Player) {
			Player p = (Player)s;
			if (plugin.getModuleReviveMe().getManager().hasPermission(s, "ReviveMe.debug")) {
				if (!plugin.getModuleReviveMe().getManager().debugPlayers.contains(p)) {
					p.sendMessage("§eDebugMode enable for §6" + version + "§e version.");
					p.sendMessage("§eUse /reviveme debug for disable mode.");
					plugin.getModuleReviveMe().getManager().debugPlayers.add(p);
				} else {
					p.sendMessage("§cDebugMode disable for §6" + version + "§c version.");
					plugin.getModuleReviveMe().getManager().debugPlayers.remove(p);
				}
			}
		} else {
			s.sendMessage("§c仅玩家使用");
		}

		return true;
	}


	public List<String> onTabComplete(CommandSender sender, String[] args, boolean isPlayer) {
		List<String> playerList = new ArrayList<>();
		if (args.length == 1) {
			for (String text : Lists.newArrayList("revive", "down", "reload", "help", "debug", "mdisable")) {
				if (text.toLowerCase().startsWith(args[0].toLowerCase())) {
					playerList.add(text);
				}
			}
			return playerList;
		}

		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("down")) {
				for (Player p2 : Bukkit.getOnlinePlayers()) {
					if (p2.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
						playerList.add(p2.getName());
					}
				}
				return playerList;
			}

			if (args[0].equalsIgnoreCase("revive")) {
				if (!plugin.getModuleReviveMe().getManager().playersPose.isEmpty()) {
					for (Player p2 : plugin.getModuleReviveMe().getManager().playersPose) {
						if (p2.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
							playerList.add(p2.getName());
						}
					}
				}
				return playerList;
			}
		}
		return playerList;
	}

	public void debug(String text) {
		Bukkit.broadcastMessage(text);
	}
}
