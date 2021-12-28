package top.mrxiaom.doomsdayessentials.placeholder;

import com.gmail.nossr50.api.PartyAPI;
import com.lenis0012.bukkit.marriage2.MarriageAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdayessentials.Main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderMCMMO extends PlaceholderExpansion {
	final Main plugin;

	public PlaceholderMCMMO(Main plugin) {
		this.plugin = plugin;
	}

	public String getAuthor() {
		return "mrxiaom";
	}

	public String getIdentifier() {
		return "mypapi";
	}

	public String limitLength(String str, int length) {
		if (str.length() <= length)
			return str;
		return str.substring(0, length) + "…";
	}

	public String removeColor(String str) {
		String result = "";
		char[] ca = str.toCharArray();
		for (int i = 0; i < ca.length; i++) {
			if (ca[i] == '§' || ca[i] == '&') {
				i++;
				continue;
			}
			result += String.valueOf(ca[i]);
		}
		return result;
	}

	public String onRequest(OfflinePlayer player, String identifier) {
		String defaultName = "无团体";
		if (identifier.toLowerCase().startsWith("party")) {
			if (player != null) {
				return this.limitLength(this.removeColor(this.getPartyName((Player) player, defaultName)), 10);
			}
			return defaultName;
		}
		if (identifier.equalsIgnoreCase("marry")) {
			if (player != null && player.getPlayer() != null) {
				return MarriageAPI.getInstance().getMPlayer(player.getPlayer()).isMarried() ? "§7[§c已婚§7]" : "";
			}
		}
		return identifier;
	}

	public String getPartyName(Player player, String defaultName) {
		String name = PartyAPI.getPartyName(player);
		return name != null ? name : defaultName;
	}

	public String getVersion() {
		return "1.0";
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
