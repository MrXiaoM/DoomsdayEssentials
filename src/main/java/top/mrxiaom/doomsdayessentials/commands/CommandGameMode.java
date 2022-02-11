package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.TimeUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class CommandGameMode extends ICommand {
	Map<Integer, GameMode> gameModeMap = new HashMap<>();
	public CommandGameMode(Main plugin) {
		super(plugin, "gamemode", new String[] { "gm" });
		gameModeMap.put(0, GameMode.SURVIVAL);
		gameModeMap.put(1, GameMode.CREATIVE);
		gameModeMap.put(2, GameMode.ADVENTURE);
		gameModeMap.put(3, GameMode.SPECTATOR);
	}

	public boolean hasOpenWorldPerm(Player player) {
		return player.getWorld().getName().equalsIgnoreCase(plugin.getOpenWorldListener().openWorldName) && player.hasPermission("doomsdayessentials.gamemode.openworld");
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (!isPlayer) return Util.noPlayer(sender);
		Player player = (Player) sender;
		if (!player.isOp() && !hasOpenWorldPerm(player)) return Util.noPerm(sender);
		if(args.length > 0) {
			GameMode gm = Util.valueOf(GameMode.class, args[0]
					.replace("生存", "SURVIVAL")
					.replace("创造", "CREATIVE")
					.replace("冒险", "ADVENTURE")
					.replace("旁观", "SPECTATOR"));
			if(gm == null){
				int gmInt = Util.strToInt(args[0], -1);
				if (gameModeMap.containsKey(gmInt)){
					gm = gameModeMap.get(gmInt);
				}
			}
			if(gm != null) {
				player.setGameMode(gm);
				player.sendMessage(I18n.t("gamemode.set").replace("%gamemode%", I18n.t("gamemode.type." + gm.name().toUpperCase())));
				return true;
			}
		}
		player.sendMessage(I18n.tn("gamemode.usage"));
		return true;
	}
}
