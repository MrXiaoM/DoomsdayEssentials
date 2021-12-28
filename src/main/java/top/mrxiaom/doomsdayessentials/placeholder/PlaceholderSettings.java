package top.mrxiaom.doomsdayessentials.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import top.mrxiaom.doomsdayessentials.Main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderSettings extends PlaceholderExpansion {
	private final Main plugin;

	public PlaceholderSettings(Main plugin) {
		this.plugin = plugin;
	}

	public String getAuthor() {
		return "mrxiaom";
	}

	public String getIdentifier() {
		return "settings";
	}

	public String onRequest(OfflinePlayer offlinePlayer, String identifier) {
		Object obj = plugin.getPlayerConfig().getConfig().get(offlinePlayer.getName() + "." + identifier, "未配置");
		if (obj != null) {
			if (obj instanceof Boolean) {
				return ((Boolean) obj).booleanValue() ? "§a开" : "§c关";
			}
			return obj.toString();
		}
		return "未配置";
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
