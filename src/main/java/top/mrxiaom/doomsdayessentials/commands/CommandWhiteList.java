package top.mrxiaom.doomsdayessentials.commands;

import fr.xephi.authme.api.v3.AuthMeApi;
import me.albert.amazingbot.AmazingBot;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.At;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class CommandWhiteList extends ICommand {
	public CommandWhiteList(Main plugin) {
		super(plugin, "whitelist", new String[] { "wlist" });
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if ((sender instanceof Player) && args.length == 2 && args[0].equalsIgnoreCase("bind")) {
			long qq = Util.strToLong(args[1], 0L);
			if (qq < 10000) {
				sender.sendMessage("§7[§9末日社团§7] §c请输入正确的qq号");
				return true;
			}
			Bot bot = Bot.getInstanceOrNull(AmazingBot.getInstance().getConfig().getLong("main.qq"));
			if (bot == null) {
				sender.sendMessage(I18n.t("chat.group.bot-not-online", true));
				return true;
			}
			Group group = bot.getGroup(951534513L);
			if (group == null){
				sender.sendMessage("§7[§9末日社团§7] §c错误: 机器人不在群内");
				return true;
			}
			if (!group.contains(qq)) {
				sender.sendMessage("§7[§9末日社团§7] §c此人不在群内，无法绑定，请先加群 951534513");
				return true;
			}
			for (String key : plugin.getWhitelistConfig().getSavedPlayers()) {
				if (plugin.getWhitelistConfig().getPlayerBindQQ(key).equalsIgnoreCase(String.valueOf(qq))) {
					sender.sendMessage("§7[§9末日社团§7] §c该QQ已绑定过账号，无法绑定");
					return true;
				}
			}
			Player player = (Player) sender;
			if (plugin.getWhitelistConfig().isSavedPlayer(player.getName())) {
				sender.sendMessage("§7[§9末日社团§7] §c你已绑定过QQ， 如要修改绑定请联系服务器管理员");
				return true;
			}
			group.sendMessage(new At(qq).plus(" 玩家 " + player.getName() + " 正在请求绑定你的QQ号，请发送文字“确认绑定”将你的QQ号绑定到游戏内"));
			plugin.getBotMsgListener().requestMap.put(player.getName(), qq);
			sender.sendMessage("§7[§9末日社团§7] §6绑定请求已发送，请在群内发送“确认绑定”");
			return true;
		} else if (args.length == 3 && args[0].equalsIgnoreCase("add")) {
			if (!sender.hasPermission("amw.admin")) {
				sender.sendMessage("§7[§9末日社团§7] §c你没有执行该命令的权限");
				return true;
			}
			if (plugin.getWhitelistConfig().isSavedPlayer(args[2]) || AuthMeApi.getInstance().isRegistered(args[2])) {
				sender.sendMessage("§7[§9末日社团§7]§6 玩家§c " + args[2] + " §6已经有白名单了，无需添加");
				return true;
			}
			for (String key : plugin.getWhitelistConfig().getSavedPlayers()) {
				if (plugin.getWhitelistConfig().getPlayerBindQQ(key).equals(args[1])) {
					sender.sendMessage("§7[§9末日社团§7]§6 QQ号§c " + args[1] + " §6已经有白名单了，无法添加");
					return true;
				}
			}
			plugin.getWhitelistConfig().set(args[2], args[1]).saveConfig();
			sender.sendMessage("§7[§9末日社团§7]§6 已添加§c " + args[2] + " §6为白名单");
			return true;
		} else if (args.length == 2 && args[0].equalsIgnoreCase("check")) {
			if (!sender.hasPermission("amw.admin")) {
				sender.sendMessage("§7[§9末日社团§7] §c你没有执行该命令的权限");
				return true;
			}
			if (!plugin.getWhitelistConfig().isSavedPlayer(args[1])) {
				sender.sendMessage("§7[§9末日社团§7]§a 没有该玩家的记录");
				return true;
			}
			sender.sendMessage(
					"§7[§9末日社团§7] §a 此玩家的QQ为: " + plugin.getWhitelistConfig().getPlayerBindQQ(args[1]));
			return true;
		} else if (args.length == 2 && args[0].equalsIgnoreCase("qq")) {
			if (!sender.hasPermission("amw.admin")) {
				sender.sendMessage("§7[§9末日社团§7] §c你没有执行该命令的权限");
				return true;
			}
			List<String> players = new ArrayList<>();
			for (final String key : plugin.getWhitelistConfig().getSavedPlayers()) {
				if (plugin.getWhitelistConfig().getPlayerBindQQ(key).equalsIgnoreCase(args[1])) {
					players.add(key);
				}
			}
			if (players.isEmpty()) {
				sender.sendMessage("§7[§9末日社团§7] §a 此QQ没有申请白名单的记录");
				return true;
			}

			String playerString = "";
			for (String player : players) {
				playerString = "§c" + player + "§6, ";
			}

			sender.sendMessage("§7[§9末日社团§7] §a 此QQ用户所绑定的玩家为: " + playerString);
			return true;
		} else {
			sender.sendMessage("§e群游互通帮助命令");
			if (!sender.hasPermission("amw.admin")) {
				sender.sendMessage("§a/wlist bind [qq] —— 绑定qq号 (绑定前需要加群 951534513，否则无效)");
				sender.sendMessage("§a/wlist key —— 查看可使用的礼包码情况");
				return true;
			}
			sender.sendMessage("§a/wlist add <qq> <玩家> —— 添加白名单");
			sender.sendMessage("§a/wlist check <玩家> —— 查看玩家QQ");
			sender.sendMessage("§a/wlist qq <QQ号码> —— 查看QQ所绑定的玩家");
			sender.sendMessage("§a/gs <消息> —— 转发消息到末日社团主群951534513");
			return true;
		}
	}
}
