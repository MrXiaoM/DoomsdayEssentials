package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.command.CommandSender;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.TimeUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class CommandAbout extends ICommand {
	public CommandAbout(Main plugin) {
		super(plugin, "about", new String[] {});
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		LocalDateTime time = LocalDateTime.of(2021, 2, 9, 20, 49);
		sender.sendMessage("§7[§9末日社团§7] §cDoomsdayEssentials §6基础插件\n" +
				"§7[§9末日社团§7] §6作者: §c懒怠的小猫§7/§cMrXiaoM\n" +
				"§7[§9末日社团§7] §cDoomTeamApi §6基础接口\n" +
				"§7[§9末日社团§7] §6作者: §c萌萌萌萌星§7/§cMX233\n" +
				"§7[§9末日社团§7] \n" +
				"§7[§9末日社团§7] §6网址: §b§nhttps://github.com/DoomsdaySociety\n" +
				"§7[§9末日社团§7] §6官网: §b§nhttps://www.doomteam.fun\n" +
				"§7[§9末日社团§7] §6充值地址: §b§nhttps://pay.doomteam.fun\n" +
				"§7[§9末日社团§7] \n" +
				"§7[§9末日社团§7] §6末日社团服务器专用，非公开插件\n" +
				"§7[§9末日社团§7] §6服务器内大多特色玩法都由该插件提供\n" +
				"§7[§9末日社团§7] §6部分配色方案来自插件 §cEssentials\n" +
				"§7[§9末日社团§7] §6感谢大家一直以来对末日社团服务器的支持\n" +
				"§7[§9末日社团§7] §6最后一次构建时间: §c" + Util.getDoomsdayEssneitialsUpdateTimeString() + "\n" +
				"§7[§9末日社团§7] §6大约已运营 §c" + time.until(LocalDateTime.now(), ChronoUnit.DAYS) + "§6天 或者 §c" + TimeUtil.getChineseTimeBetweenNow(time));

		return true;
	}
}
