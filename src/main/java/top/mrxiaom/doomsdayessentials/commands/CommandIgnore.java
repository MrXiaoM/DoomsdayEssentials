package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.gui.GuiUnignore;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.TimeUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.time.LocalDateTime;

public class CommandIgnore extends ICommand {
	public CommandIgnore(Main plugin) {
		super(plugin, "ignore", new String[] {});
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if(!isPlayer){
			return Util.noPlayer(sender);
		}
		Player player = (Player) sender;
		if(args.length > 1){
			StringBuilder msg = new StringBuilder(args[1]);
			for(int i = 2; i < args.length; i++) msg.append(" ").append(args[i]);
			// 屏蔽玩家
			if(args[0].equalsIgnoreCase("player") || args[0].equalsIgnoreCase("p")){
				boolean isSuccess = plugin.getPlayerConfig().ignoreAddPlayer(player.getName(), msg.toString(), false);
				player.sendMessage(I18n.t("ignore.player." + (isSuccess?"success":"fail"), true).replace("%player%", msg.toString()));
				return true;
			}
			// 屏蔽玩家 (正则表达式)
			if(args[0].equalsIgnoreCase("playerregex") || args[0].equalsIgnoreCase("pr")){
				boolean isSuccess = plugin.getPlayerConfig().ignoreAddPlayer(player.getName(), msg.toString(), true);
				player.sendMessage(I18n.t("ignore.player." + (isSuccess?"success":"fail") + "-regex", true).replace("%player%", msg.toString()));
				return true;
			}
			// 屏蔽消息
			if(args[0].equalsIgnoreCase("message") || args[0].equalsIgnoreCase("m")){
				boolean isSuccess = plugin.getPlayerConfig().ignoreAddMsg(player.getName(), msg.toString(), false);
				player.sendMessage(I18n.t("ignore.message." + (isSuccess?"success":"fail"), true).replace("%msg%", msg.toString()));
				return true;
			}
			// 屏蔽消息（正则表达式）
			if(args[0].equalsIgnoreCase("messageregex") || args[0].equalsIgnoreCase("mr")){
				boolean isSuccess = plugin.getPlayerConfig().ignoreAddMsg(player.getName(), msg.toString(), true);
				player.sendMessage(I18n.t("ignore.message." + (isSuccess?"success":"fail") + "-regex", true).replace("%msg%", msg.toString()));
				return true;
			}
		}
		if(args.length >= 1 && args[0].equalsIgnoreCase("gui")){
			GuiUnignore gui = new GuiUnignore(plugin, player, GuiUnignore.IgnoreType.PLAYER, 1);
			if(args.length >=2 && args[1].equalsIgnoreCase("menu")) gui.isMenu = true;
			plugin.getGuiManager().openGui(gui);
			return true;
		}
		player.sendMessage(I18n.tn("ignore.help", true));
		return true;
	}
}
