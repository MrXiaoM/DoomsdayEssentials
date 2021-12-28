package top.mrxiaom.doomsdayessentials.commands;

import net.mamoe.mirai.Bot;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.List;

public class CommandGroup extends ICommand {
	public CommandGroup(Main plugin) {
		super(plugin, "group", new String[] { "g" });
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (args.length > 0) {
			String msg = args[0];
			for (int i = 1; i < args.length; i++) {
				msg += " " + args[i];
			}
			if (plugin.getConfig().contains("blacklist-words")) {
				List<String> bw = plugin.getConfig().getStringList("blacklist-words");
				if (bw != null) {
					for (String s : bw) {
						if (msg.toLowerCase().contains(s)) {
							sender.sendMessage(I18n.t("chat.group.banwords", true));
							return true;
						}
					}
				}
			}
			Bot bot = me.albert.amazingbot.bot.Bot.getApi().getBot();
			if (bot == null) {
				sender.sendMessage(I18n.t("chat.group.bot-not-online", true));
				return true;
			}
			String str = plugin.getConfig().getString("chat.game.to-group").replace("%name%", sender.getName())
					.replace("%msg%", msg);
			String str2 = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("chat.game.to-game"))
					.replace("%name%", sender.getName()).replace("%msg%", Util.replaceColor(msg, sender));
			Util.alert(str2);
			bot.getGroup(951534513L).sendMessage(str);
			sender.sendMessage(I18n.t("chat.group.sent", true));
			return true;
		}
		return true;
	}
}
