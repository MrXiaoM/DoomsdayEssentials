package top.mrxiaom.doomsdayessentials.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.doomsdayessentials.Main;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderSettings extends PlaceholderExpansion {
	private final Main plugin;

	public PlaceholderSettings(Main plugin) {
		this.plugin = plugin;
	}

	public @NotNull String getAuthor() {
		return "mrxiaom";
	}

	public @NotNull String getIdentifier() {
		return "settings";
	}

	public String onRequest(OfflinePlayer offlinePlayer, @NotNull String identifier) {
		Object obj = plugin.getPlayerConfig().getConfig().get(offlinePlayer.getName() + "." + identifier, "未配置");
		if (obj != null) {
			if (obj instanceof Boolean) {
				return (Boolean) obj ? "§a开" : "§c关";
			}
			return obj.toString();
		}
		return "未配置";
	}

	public @NotNull String getVersion() {
		return this.plugin.getDescription().getVersion();
	}

	public boolean persist() {
		return true;
	}

	public boolean canRegister() {
		return true;
	}
}
