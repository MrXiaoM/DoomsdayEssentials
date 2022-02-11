package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandSettings extends ICommand {
	public CommandSettings(Main plugin) {
		super(plugin, "settings", new String[] { "set" });
	}

	static final Map<String, Object> cfgs = new HashMap<>();
	static {
		cfgs.put("is-show-bullets", true);
		cfgs.put("show-back-message", true);
		cfgs.put("show-back-message-death", true);
		cfgs.put("show-alerts", true);
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (!isPlayer) {
			if (sender.isOp() && args.length >= 3) {
				String p = args[0];
				String k = args[1];
				String v = args[2];
				plugin.getPlayerConfig().getConfig().getConfigurationSection(p).set(k, v.equalsIgnoreCase("true"));
			}
			return true;
		}
		Player player = (Player) sender;

		if (args.length >= 1) {
			for (String s : cfgs.keySet()) {
				if (args[0].equalsIgnoreCase(s) && cfgs.get(s) instanceof Boolean) {
					if (args.length >= 2) {
						boolean value = false;
						boolean flag = false;
						if (args[1].equalsIgnoreCase("true")) {
							flag = true;
							value = true;
						}
						if (args[1].equalsIgnoreCase("false")) {
							flag = true;
						}
						if (!flag) {
							player.sendMessage(I18n.t("settings.no-boolean", true));
							return true;
						}
						plugin.getPlayerConfig().getConfig().set(player.getName() + "." + s, value);
						plugin.getPlayerConfig().saveConfig();
						String key = I18n.contains("settings.display." + s) ? I18n.t("settings.display." + s) : s;
						player.sendMessage(I18n.t("settings.set", true).replace("%key%", key).replace("%value%",
								(value ? I18n.t("settings.values.boolean-true")
										: I18n.t("settings.values.boolean-false"))));
						return true;
					} else {
						player.sendMessage(I18n.prefix() + player.getName() + "." + s + ": " + plugin.getPlayerConfig()
								.getConfig().getBoolean(player.getName() + "." + s, (boolean) cfgs.get(s)));
						return true;
					}
				}
			}
			if (args[0].equalsIgnoreCase("text")) {
				if (args.length >= 2) {
					String id = args[1];
					if (plugin.getConfig().contains("texts." + id)) {
						List<String> msgs = plugin.getConfig().getStringList("texts." + id);
						for (String msg : msgs) {
							player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
						}
						return true;
					} else {
						player.sendMessage(I18n.t("settings.no-found", true));
						return true;
					}
				}
			}
			if (args[0].equalsIgnoreCase("list")) {
				player.sendMessage(I18n.t("settings.list-title", true) + Util.listToString(cfgs.keySet()));
				return true;
			}
			player.sendMessage(I18n.t("settings.no-found", true));
			return true;
		}
		player.sendMessage(I18n.tn("settings.help"));
		return true;
	}

}
