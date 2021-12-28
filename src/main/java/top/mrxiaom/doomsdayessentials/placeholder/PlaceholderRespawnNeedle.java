package top.mrxiaom.doomsdayessentials.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import top.mrxiaom.doomsdayessentials.Main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderRespawnNeedle extends PlaceholderExpansion {
	private final Main plugin;

	public PlaceholderRespawnNeedle(Main plugin) {
		this.plugin = plugin;
	}

	public String getAuthor() {
		return "mrxiaom";
	}

	public String getIdentifier() {
		return "respawnneedle";
	}

	public String onRequest(OfflinePlayer player, String identifier) {
		if (identifier.toLowerCase().startsWith("get")) {
			if (identifier.toLowerCase().startsWith("get_")) {
				String playername = identifier.substring(4);
				if (this.isMatchPlayerName(playername)) {
					return String.valueOf(plugin.getPlayerConfig().getNeedle(playername));
				}
			} else {
				if (player != null) {
					return String.valueOf(plugin.getPlayerConfig().getNeedle(player.getName()));
				}
			}
			return "";
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
