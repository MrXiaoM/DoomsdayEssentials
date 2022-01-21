package top.mrxiaom.doomsdayessentials.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderPlayerPoints extends PlaceholderExpansion {
	private final PlayerPointsAPI api;

	public PlaceholderPlayerPoints(PlayerPointsAPI api) {
		this.api = api;
	}

	public @NotNull String getAuthor() {
		return "mrxiaom";
	}

	public @NotNull String getIdentifier() {
		return "playerpoints";
	}

	@SuppressWarnings("deprecation")
	public String onRequest(OfflinePlayer player, String identifier) {
		if (identifier.toLowerCase().startsWith("points")) {
			if (identifier.toLowerCase().startsWith("points_")) {
				String playername = identifier.substring(7);
				if (this.isMatchPlayerName(playername)) {
					return String.valueOf(api.look(playername));
				}
			} else {
				if (player != null) {
					return String.valueOf(api.look(player.getName()));
				}
			}
			return "0";
		}
		return identifier;
	}

	public @NotNull String getVersion() {
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
