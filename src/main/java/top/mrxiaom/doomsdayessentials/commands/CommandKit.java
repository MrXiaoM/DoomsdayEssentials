package top.mrxiaom.doomsdayessentials.commands;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.KitConfig.Kit;
import top.mrxiaom.doomsdayessentials.configs.PlayerConfig.LastSignInfo;
import top.mrxiaom.doomsdayessentials.configs.PlayerConfig.SignTime;
import top.mrxiaom.doomsdayessentials.gui.GuiKitEditor;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandKit extends ICommand {
	public CommandKit(Main plugin) {
		super(plugin, "doomsdaykit", new String[] { "kit" });
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, boolean isPlayer) {
		List<String> result = new ArrayList<String>();
		if (args.length == 2 && args[0].equalsIgnoreCase("use")) {
			for (String kitId : plugin.getKitConfig().getAllKits().keySet()) {
				if (kitId.startsWith(args[0])) {
					if (!(sender instanceof Player)) {
						result.add(kitId);
					} else if (sender.hasPermission("doomsdaykits.use." + kitId)) {
						Kit kit = plugin.getKitConfig().get(kitId);
						LastSignInfo last = plugin.getPlayerConfig().getLastSignInfo(sender.getName(), kitId);
						if ((last.times >= kit.getMaxTime() && kit.getMaxTime() != 0) || (last.times > 0
								&& kit.isEveryday() && last.signTime != null && last.signTime.isTimeNotUp())) {
							continue;
						}
						result.add(kitId);
					}
				}
			}
		}
		return result;
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
			if (!sender.isOp()) {
				sender.sendMessage(I18n.tn("kit.help-user", true));
			} else {
				sender.sendMessage(I18n.tn("kit.help", true));
			}
			return true;
		}
		if (args.length >= 2) {
			if (args[0].equalsIgnoreCase("use")) {
				if (!isPlayer) {
					return Util.noPlayer(sender);
				}
				final Player player = (Player) sender;
				final String playerName = player.getName();
				String kitId = args[1];
				if (!plugin.getKitConfig().contains(kitId)) {
					player.sendMessage(I18n.t("kit.nokit", true));
					return true;
				}
				if (!player.hasPermission("doomsdaykits.use." + kitId)) {
					return Util.noPerm(sender);
				}

				Kit kit = plugin.getKitConfig().get(kitId);
				LastSignInfo last = plugin.getPlayerConfig().getLastSignInfo(playerName, kitId);
				if (last.times >= kit.getMaxTime() && kit.getMaxTime() != 0) {
					player.sendMessage(I18n.t("kit.limited", true));
					return true;
				}
				if (last.times > 0) {
					if (kit.isEveryday() && last.signTime != null && last.signTime.isTimeNotUp()) {
						player.sendMessage(I18n.t("kit.cooldown", true));
						return true;
					}
				}
				plugin.getPlayerConfig().setSign(playerName, kitId, SignTime.getNowTime()).saveConfig();
				ItemStack[] items = kit.getItems();
				HashMap<Integer, ItemStack> map = player.getInventory().addItem(items);
				if (!map.isEmpty()) {
					player.sendMessage(I18n.t("kit.inv-full", true));
					for (ItemStack item : map.values()) {
						player.getWorld().dropItem(player.getLocation(), item);
					}
				}
				List<String> commands = kit.getCommands();
				for (String cmd_ : commands) {
					if (cmd_.contains(":")) {
						String prefix = cmd_.substring(0, cmd_.indexOf(":"));
						String cmd = cmd_.substring(cmd_.indexOf(":") + 1);
						if (prefix.equalsIgnoreCase("console")) {
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
									PlaceholderAPI.setPlaceholders(player, cmd));
						}
						if (prefix.equalsIgnoreCase("player")) {
							Bukkit.dispatchCommand(player, PlaceholderAPI.setPlaceholders(player, cmd));
						}
					}
				}

				player.sendMessage(I18n.t("kit.use", true).replace("%kit%", kit.getName()));
				return true;
			} else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
				if (!sender.isOp())
					return Util.noPerm(sender);
				String kitId = args[1];
				String playerName = args[2];
				boolean flag = false;
				Player player = null;
				for (Player p : Bukkit.getOnlinePlayers()) {
					if (p.getName().equalsIgnoreCase(playerName)) {
						player = p;
						flag = true;
						break;
					}
				}
				if (!flag) {
					sender.sendMessage(I18n.t("not-online", true));
					return true;
				}
				if (!plugin.getKitConfig().contains(kitId)) {
					player.sendMessage(I18n.t("kit.nokit", true));
					return true;
				}

				Kit kit = plugin.getKitConfig().get(kitId);
				ItemStack[] items = kit.getItems();
				HashMap<Integer, ItemStack> map = player.getInventory().addItem(items);
				if (!map.isEmpty()) {
					player.sendMessage(I18n.t("kit.inv-full", true));
					for (ItemStack item : map.values()) {
						player.getWorld().dropItem(player.getLocation(), item);
					}
				}
				List<String> commands = kit.getCommands();
				for (String cmd_ : commands) {
					if (cmd_.contains(":")) {
						String prefix = cmd_.substring(0, cmd_.indexOf(":"));
						String cmd = cmd_.substring(cmd_.indexOf(":") + 1);
						if (prefix.equalsIgnoreCase("console")) {
							Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
									PlaceholderAPI.setPlaceholders(player, cmd));
						}
						if (prefix.equalsIgnoreCase("player")) {
							Bukkit.dispatchCommand(player, PlaceholderAPI.setPlaceholders(player, cmd));
						}
					}
				}
				player.sendMessage(I18n.t("kit.given", true).replace("%kit%", kit.getName()));
				return true;
			} else if (args[0].equalsIgnoreCase("look")) {

				if (!isPlayer) {
					return Util.noPlayer(sender);
				}
				final Player player = (Player) sender;
				final String playerName = player.getName();
				String kitId = args[1];
				if (!plugin.getKitConfig().contains(kitId)) {
					player.sendMessage(I18n.t("kit.nokit", true));
					return true;
				}
				if (!player.hasPermission("doomsdaykits.use." + kitId)) {
					return Util.noPerm(sender);
				}

				Kit kit = plugin.getKitConfig().get(kitId);
				LastSignInfo last = plugin.getPlayerConfig().getLastSignInfo(playerName, kitId);

				String msg = I18n.t("kit.used-title").replace("%kit%", kit.getName()) + "\n";
				if (last.times == 0) {
					msg += I18n.t("kit.used-no").replace("%kit%", kit.getName());
				} else {
					SignTime time = last.signTime;
					msg += I18n.tn("kit.used-time", true).replace("%year%", String.valueOf(time.year))
							.replace("%month", String.valueOf(time.month)).replace("%day%", String.valueOf(time.day))
							.replace("%hour%", String.valueOf(time.hour))
							.replace("%minute%", String.valueOf(time.minute))
							.replace("%day%", String.valueOf(time.second))
							.replace("%times%", String.valueOf(last.times));
				}

				player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));

				return true;
			} else if (args[0].equalsIgnoreCase("edit")) {
				if (!isPlayer) {
					return Util.noPlayer(sender);
				}
				final Player player = (Player) sender;
				if (!player.isOp()) {
					return Util.noPerm(player);
				}
				String kitId = args[1];
				if (!plugin.getKitConfig().contains(kitId)) {
					player.sendMessage(I18n.t("kit.nokit", true));
					return true;
				}
				plugin.getGuiManager().openGui(new GuiKitEditor(plugin, player, plugin.getKitConfig().get(kitId)));
				return true;
			} else if (args[0].equalsIgnoreCase("create")) {
				if (!sender.isOp()) {
					return Util.noPerm(sender);
				}
				String kitId = args[1];
				String displayName = args.length == 3 ? args[2] : kitId;
				if (plugin.getKitConfig().contains(kitId)) {
					sender.sendMessage(I18n.t("kit.already-exists", true));
				}
				Kit kit = new Kit(kitId, displayName, "", false, new ArrayList<>(), 0);
				plugin.getKitConfig().set(kit);
				sender.sendMessage(I18n.t("kit.create", true).replace("%kit%", kitId).replace("%name%", displayName));
				return true;
			} else if (args.length == 3 && args[0].equalsIgnoreCase("display")) {
				if (!sender.isOp()) {
					return Util.noPerm(sender);
				}
				String kitId = args[1];
				if (!plugin.getKitConfig().contains(kitId)) {
					sender.sendMessage(I18n.t("kit.nokit", true));
				}
				Kit kit = plugin.getKitConfig().get(kitId);
				String displayName = args[2];
				kit.setName(displayName);
				plugin.getKitConfig().set(kit);
				sender.sendMessage(
						I18n.t("kit.set-display-name", true).replace("%kit%", kitId).replace("%name%", displayName));
				return true;
			} else if (args[0].equalsIgnoreCase("cmd")) {
				if (!sender.isOp()) {
					return Util.noPerm(sender);
				}
				String kitId = args[1];
				if (!plugin.getKitConfig().contains(kitId)) {
					sender.sendMessage(I18n.t("kit.nokit", true));
				}
				Kit kit = plugin.getKitConfig().get(kitId);
				List<String> commands = kit.getCommands();
				if (args.length == 3 && args[2].equalsIgnoreCase("look")) {
					String msg = I18n.t("kit.command-list.header", true);
					if (commands.isEmpty()) {
						msg += I18n.t("kit.command-list.empty");
					} else {
						for (int i = 0; i < commands.size(); i++) {
							msg += I18n.t("kit.command-list.prefix", true) + i + I18n.t("kit.command-list.suffix")
									+ commands.get(i);
						}
					}
					sender.sendMessage(msg);
					return true;
				}
				if (args.length > 3 && args[2].equalsIgnoreCase("add")) {
					String cmd = args[3];
					for (int i = 4; i < args.length; i++) {
						cmd += " " + args[i];
					}
					commands.add(cmd);
					kit.setCommands(commands);
					plugin.getKitConfig().set(kit);
					sender.sendMessage(I18n.t("kit.add-command", true));
					return true;
				}
				if (args.length == 4 && args[2].equalsIgnoreCase("remove")) {
					int line = Util.strToInt(args[3], -1);
					if (line < 0 || line >= commands.size()) {
						sender.sendMessage(
								I18n.t("kit.invalid-line", true).replace("%line%", String.valueOf(commands.size())));
						return true;
					}
					commands.remove(line);
					kit.setCommands(commands);
					plugin.getKitConfig().set(kit);
					sender.sendMessage(I18n.t("kit.remove-command", true));
					return true;
				}
				return true;
			} else if (args[0].equalsIgnoreCase("everyday")) {
				if (!sender.isOp()) {
					return Util.noPerm(sender);
				}
				String kitId = args[1];
				if (!plugin.getKitConfig().contains(kitId)) {
					sender.sendMessage(I18n.t("kit.nokit", true));
				}
				Kit kit = plugin.getKitConfig().get(kitId);
				kit.setIsEveryday(kit.isEveryday());
				plugin.getKitConfig().set(kit);
				sender.sendMessage(
						I18n.t("kit.set-everyday", true).replace("%value%", kit.isEveryday() ? "§a开启" : "§c关闭"));
				return true;
			} else if (args.length == 3 && args[0].equalsIgnoreCase("maxtime")) {
				if (!sender.isOp()) {
					return Util.noPerm(sender);
				}
				String kitId = args[1];
				if (!plugin.getKitConfig().contains(kitId)) {
					sender.sendMessage(I18n.t("kit.nokit", true));
				}
				int maxTime = Util.strToInt(args[2], -1);
				if (maxTime < 0) {
					sender.sendMessage(I18n.t("kit.invalid-line", true).replace("%line%", "+∞"));
					return true;
				}
				Kit kit = plugin.getKitConfig().get(kitId);
				kit.setMaxTime(maxTime);
				plugin.getKitConfig().set(kit);
				sender.sendMessage(
						I18n.t("kit.set-maxtime", true).replace("%value%", String.valueOf(kit.getMaxTime())));
				return true;
			}

		}
		if (args.length == 0) {
			String kitMsg = I18n.t("kit.list.header", true);
			Map<String, Kit> kits = plugin.getKitConfig().getAllKits();
			int i = 0;
			for (String kitId : kits.keySet()) {
				if (sender.hasPermission("doomsdaykits.use." + kitId)) {
					Kit kit = kits.get(kitId);
					kitMsg = kitMsg + I18n.t("kit.list.prefix") + kitId + " §7(§6" + kit.getName() + "§7)"
							+ ((i == kits.size() - 1) ? "" : I18n.t("kit.list.suffix"));
				}
				i++;
			}
			sender.sendMessage(kitMsg);
		}
		return true;
	}
}
