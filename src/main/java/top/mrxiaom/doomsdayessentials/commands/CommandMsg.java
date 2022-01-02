package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

public class CommandMsg extends ICommand {
	public CommandMsg(Main plugin) {
		super(plugin, "msg", new String[] { "m", "tell", "t", "whisper", "w" });
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {

		if (args.length >= 2) {
			String msgTemplateFrom = plugin.getConfig().getString("chat.msg-from");
			String msgTemplateTo = plugin.getConfig().getString("chat.msg-to");

			Player fromPlayer = isPlayer ? ((Player) sender) : null;
			Player toPlayer = Bukkit.getPlayer(args[0]);
			if (toPlayer == null) {
				sender.sendMessage(I18n.t("not-online", true));
				return true;
			}
			String msg = "";
			for (int i = 1; i < args.length; i++) {
				msg = msg + args[i] + " ";
			}
			msg = Util.replaceColor(msg, sender);

			fromPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', msgTemplateFrom)
					.replace("%from%", isPlayer ? fromPlayer.getName() : "控制台").replace("%to%", toPlayer.getName())
					.replace("%msg%", msg));
			toPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', msgTemplateTo)
					.replace("%from%", isPlayer ? fromPlayer.getName() : "控制台").replace("%to%", toPlayer.getName())
					.replace("%msg%", msg));
			return true;
		}
		sender.sendMessage(I18n.t("msg-help", true));
		return true;
	}
}