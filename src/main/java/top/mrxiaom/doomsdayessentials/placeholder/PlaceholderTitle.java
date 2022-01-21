package top.mrxiaom.doomsdayessentials.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.doomsdayessentials.Main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderTitle extends PlaceholderExpansion {
	private final Main plugin;

	public PlaceholderTitle(Main plugin) {
		this.plugin = plugin;
	}

	public @NotNull String getAuthor() {
		return "MrXiaoM";
	}

	public @NotNull String getIdentifier() {
		return "udtitle";
	}

	public String onRequest(OfflinePlayer player, @NotNull String identifier) {
		if (plugin.getTagConfig() == null)
			return identifier;
		if (identifier.toLowerCase().startsWith("title")) {
			if (identifier.toLowerCase().startsWith("title_")) {
				String playername = identifier.substring("title_".length());
				if (this.isMatchPlayerName(playername)) {
					return ChatColor.translateAlternateColorCodes('&', plugin.getTagConfig().getPlayerTag(playername));
				}
			} else {
				if (player != null) {
					return ChatColor.translateAlternateColorCodes('&',
							plugin.getTagConfig().getPlayerTag(player.getName()));
				}
			}
			return "";
		}
		if (identifier.equalsIgnoreCase("suffix")) {
			if (player.isOp()) {
				return "§7[§c操作员§7]";
			}
			return PlaceholderAPI.setPlaceholders(player, "%luckperms_suffix%");
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
