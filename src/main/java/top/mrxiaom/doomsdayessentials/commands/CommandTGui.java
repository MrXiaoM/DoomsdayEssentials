package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.gui.GuiTagListAll;
import top.mrxiaom.doomsdayessentials.gui.GuiTagListPlayer;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.HashMap;
import java.util.Map;

public class CommandTGui extends ICommand {
	public CommandTGui(Main plugin) {
		super(plugin, "tgui", new String[] {});
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if ((sender instanceof Player) && args.length > 0 && args[0].equalsIgnoreCase("open")) {
			plugin.getGuiManager().openGui(new GuiTagListPlayer(plugin, (Player)sender, 1));
		}
		if ((sender instanceof Player) && args.length == 0) {
			plugin.getGuiManager().openGui(new GuiTagListPlayer(plugin, (Player)sender, 1));
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("help")) {
				sender.sendMessage(I18n.tn("title.help", true));
			} else if (args[0].equalsIgnoreCase("list")) {
				if (sender instanceof Player) {
					plugin.getGuiManager().openGui(new GuiTagListAll(plugin, (Player)sender, 1));
					return true;
				}
				sender.sendMessage(I18n.t("title.List1"));
				Map<Integer, String> titleMap = plugin.getTagConfig().getTagMap();
				for (int id : titleMap.keySet()) {
					sender.sendMessage("§4" + id + "§b:---:" + titleMap.get(id));
				}
				sender.sendMessage(I18n.t("title.List2"));
			} else if (args[0].equalsIgnoreCase("reload")) {
				plugin.getTagConfig().reloadConfig();
				sender.sendMessage(I18n.t("title.reloaded"));
			}
		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("list")) {
				OfflinePlayer player = Util.getOfflinePlayer(args[1]);
				Map<Integer, String> playerAllTitle = new HashMap<>();
				if (player != null) {
					Map<Integer, String> titleMap = plugin.getTagConfig().getTagMap();
					for (int titleId : titleMap.keySet()) {
						if (plugin.getTagConfig().hasTag(player, titleId)) {
							playerAllTitle.put(titleId, titleMap.get(titleId));
						}
					}
				}
				sender.sendMessage("§2--------§b[§6" + args[1] + "的称号" + "§b]§2-------");
				for (int titleId : playerAllTitle.keySet()) {
					sender.sendMessage("§4" + titleId + "§b:---:" + playerAllTitle.get(titleId));
				}
				sender.sendMessage("§2--------§b[玩家称号列表]§2-------");
			}
		}
		return true;
	}
}
