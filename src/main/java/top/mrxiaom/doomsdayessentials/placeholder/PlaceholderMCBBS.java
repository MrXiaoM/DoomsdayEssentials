package top.mrxiaom.doomsdayessentials.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.doomsdayessentials.Main;

public class PlaceholderMCBBS extends PlaceholderExpansion {
	final Main plugin;

	public PlaceholderMCBBS(Main plugin) {
		this.plugin = plugin;
	}

	public @NotNull String getAuthor() {
		return "mrxiaom";
	}

	public @NotNull String getIdentifier() {
		return "mcbbs";
	}

	public String onRequest(OfflinePlayer player, String identifier) {
		if (identifier.equalsIgnoreCase("is_bound")) {
			return plugin.getMcbbsConfig().isPlayerBound(player.getName()) ? "yes" : "no";
		}
		if (identifier.equalsIgnoreCase("uid")) {
			return plugin.getMcbbsConfig().isPlayerBound(player.getName()) ? plugin.getMcbbsConfig().getPlayerUid(player.getName()) : "未绑定";
		}
		if (identifier.equalsIgnoreCase("up_times")) {
			return plugin.getMcbbsConfig().isPlayerBound(player.getName()) ? String.valueOf(plugin.getMcbbsConfig().getPlayerUpTimes(player.getName())) : "0";
		}
		if (identifier.equalsIgnoreCase("is_claimed")) {
			return plugin.getMcbbsConfig().isNowMatchedLastDate() ? "yes" : "no";
		}
		if (identifier.equalsIgnoreCase("today_claimed")) {
			if(!plugin.getMcbbsConfig().isNowMatchedLastDate()) return "";
			return plugin.getMcbbsConfig().getLastPlayer();
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

}
