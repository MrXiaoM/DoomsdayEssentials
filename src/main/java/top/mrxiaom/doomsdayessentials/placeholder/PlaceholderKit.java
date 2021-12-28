package top.mrxiaom.doomsdayessentials.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.KitConfig.Kit;
import top.mrxiaom.doomsdayessentials.configs.PlayerConfig.LastSignInfo;
import top.mrxiaom.doomsdayessentials.configs.PlayerConfig.SignTime;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderKit extends PlaceholderExpansion {
	private final Main plugin;

	public PlaceholderKit(Main plugin) {
		this.plugin = plugin;
	}

	public String getAuthor() {
		return "mrxiaom";
	}

	public String getIdentifier() {
		return "doomsdaykit";
	}

	public String onRequest(OfflinePlayer player, String identifier) {
		// doomsdaykit_check#工具包ID#可使用消息#冷却中消息#次数已满消息
		if (identifier.toLowerCase().startsWith("check#")) {
			String[] args = identifier.toLowerCase().split("#");
			String ok = "ok";
			String cooldown = "cooldown";
			String limited = "limited";
			if (args.length > 2) {
				ok = args[2];
				if (args.length > 3) {
					cooldown = args[3];
					if (args.length > 4) {
						limited = args[4];
					}
				}
			}
			String kitId = args[1];
			Kit kit = plugin.getKitConfig().get(kitId);
			LastSignInfo last = plugin.getPlayerConfig().getLastSignInfo(player.getName(), kitId);
			if (last.times >= kit.getMaxTime() && kit.getMaxTime() != 0) {
				return limited;
			}
			if (last.times > 0) {
				if (kit.isEveryday() && last.signTime != null) {
					SignTime now = SignTime.getNowTime();
					SignTime time = last.signTime;
					if (now.year <= time.year && now.month <= time.month && now.day <= time.day) {
						return cooldown;
					}
				}
			}
			return ok;
		}
		return identifier;
	}

	public String getVersion() {
		return this.plugin.getDescription().getVersion();
	}

	public boolean persist() {
		return true;
	}

	public boolean canRegister() {
		return true;
	}

	public boolean isMatchPlayerName(String player) {
		Pattern p = Pattern.compile("[a-zA-Z0-9_]*{3,16}");
		Matcher m = p.matcher(player);
		return m.matches();
	}
}
