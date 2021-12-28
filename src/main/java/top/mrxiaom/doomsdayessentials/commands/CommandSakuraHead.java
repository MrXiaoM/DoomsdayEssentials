package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.SkullConfig.Skull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandSakuraHead extends ICommand {
	public CommandSakuraHead(Main plugin) {
		super(plugin, "skurahead", new String[] { "sh" });
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (sender.isOp()) {
			if ("give".equals(args[0])) {
				try {
					Player player = Objects.requireNonNull(Bukkit.getPlayer(args[1]));

					Skull skull = this.plugin.getSkullConfig().getSkull(EntityType.valueOf(args[2].toUpperCase()));
					player.getInventory().addItem(skull.getItemStack());

					sender.sendMessage("头颅发送成功");

				} catch (ArrayIndexOutOfBoundsException e) {
					sender.sendMessage("参数不足");
				} catch (NullPointerException e) {
					sender.sendMessage("玩家当前不在线");
				} catch (IllegalArgumentException e) {
					sender.sendMessage("生物类型输入错误");
				}
			}

		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, boolean isPlayer) {
		if (args.length == 1) {

			List<String> tabComplete = new ArrayList<>();
			tabComplete.add("give");

			tabComplete.removeIf(s -> !s.startsWith(args[0].toLowerCase()));

			return tabComplete;

		} else if (args.length == 3) {

			Main plugin = Main.getInstance();

			List<String> tabComplete = new ArrayList<>(
					plugin.getSkullConfig().getConfig().getConfigurationSection("SkullType").getKeys(false));

			if (!args[args.length - 1].trim().isEmpty()) {
				String match = args[args.length - 1].trim().toLowerCase();
				tabComplete.removeIf(name -> !name.toLowerCase().startsWith(match));
			}

			tabComplete.replaceAll(String::toLowerCase);

			return tabComplete;
		}
		return null;
	}
}
