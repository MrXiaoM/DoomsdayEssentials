package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.command.CommandSender;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;

public class CommandRespawnNeedle extends ICommand {
	public CommandRespawnNeedle(Main plugin) {
		super(plugin, "respawnneedle", new String[] { "rn" });
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		int count = isPlayer ? plugin.getPlayerConfig().getNeedle(sender.getName()) : 0;
		if (!sender.isOp()) {
			if (isPlayer) {
				for (String s : I18n.l("respawnneedle.help")) {
					sender.sendMessage(s.replace("%count%", String.valueOf(count)));
				}
				return true;
			}
		}
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("add")) {
				if (args.length == 3) {
					try {
						String player = args[1];
						int amount = Integer.parseInt(args[2]);
						plugin.getPlayerConfig().addNeedle(player, amount);
						sender.sendMessage(I18n.t("respawnneedle.added").replace("%player%", player).replace("%amount%",
								String.valueOf(amount)));
						return true;
					} catch (NumberFormatException e) {
						sender.sendMessage(I18n.t("not-integer", true));
						return true;
					}
				}
			}

			if (args[0].equalsIgnoreCase("remove")) {
				if (args.length == 3) {
					try {
						String player = args[1];
						int amount = Integer.parseInt(args[2]);
						plugin.getPlayerConfig().removeNeedle(player, amount);
						sender.sendMessage(I18n.t("respawnneedle.removed").replace("%player%", player)
								.replace("%amount%", String.valueOf(amount)));
						return true;
					} catch (NumberFormatException e) {
						sender.sendMessage(I18n.t("not-integer", true));
						return true;
					}
				}
			}

			if (args[0].equalsIgnoreCase("set")) {
				if (args.length == 3) {
					try {
						String player = args[1];
						int amount = Integer.parseInt(args[2]);
						plugin.getPlayerConfig().setNeedle(player, amount).saveConfig();
						sender.sendMessage(I18n.t("respawnneedle.set").replace("%player%", player).replace("%amount%",
								String.valueOf(amount)));
						return true;
					} catch (NumberFormatException e) {
						sender.sendMessage(I18n.t("not-integer", true));
						return true;
					}
				}
			}

			if (args[0].equalsIgnoreCase("get")) {
				if (args.length == 2) {
					String player = args[1];
					int amount = plugin.getPlayerConfig().getNeedle(player);
					sender.sendMessage(I18n.t("respawnneedle.get").replace("%player%", player).replace("%amount%",
							String.valueOf(amount)));
					return true;
				}
			}
		}
		if (isPlayer) {
			for (String s : I18n.l("respawnneedle.help")) {
				sender.sendMessage(s.replace("%count%", String.valueOf(count)));
			}
		}
		if (sender.isOp()) {
			sender.sendMessage(I18n.array("respawnneedle.helpop"));
		}
		return true;
	}

}
