package top.mrxiaom.doomsdayessentials.placeholder;

import com.gmail.nossr50.api.PartyAPI;
import com.lenis0012.bukkit.marriage2.MarriageAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.modules.reviveme.ReviveMeApi;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholderMCMMO extends PlaceholderExpansion {
	final Main plugin;

	public PlaceholderMCMMO(Main plugin) {
		this.plugin = plugin;
	}

	public @NotNull String getAuthor() {
		return "mrxiaom";
	}

	public @NotNull String getIdentifier() {
		return "mypapi";
	}

	public String onRequest(OfflinePlayer player, String identifier) {
		String defaultName = "无团体";
		if (identifier.toLowerCase().startsWith("party")) {
			if (player != null) {
				return Util.limitLength(Util.removeColor(this.getPartyName((Player) player, defaultName)), 10);
			}
			return defaultName;
		}
		if (identifier.equalsIgnoreCase("marry")) {
			if (player != null && player.getPlayer() != null) {
				return MarriageAPI.getInstance().getMPlayer(player.getPlayer()).isMarried() ? "§7[§c已婚§7]" : "";
			}
		}
		if(identifier.equalsIgnoreCase("health")){
			Player p = player.getPlayer();
			if (p == null) return "";
			String msg = " &4❤ &f%player_health_rounded%&7/&f%player_max_health_rounded% %udtitle_suffix%";
			if(ReviveMeApi.isRelivingPlayer(p)){
				msg = "&e&l正在急救...";
			}
			else if (ReviveMeApi.isPlayerDowned(p)){
				msg = "&c&l靠近按住 Shift 进行急救 ";
			}
			return PlaceholderAPI.setPlaceholders(p, msg);
		}
		return identifier;
	}

	public String getPartyName(Player player, String defaultName) {
		String name = PartyAPI.getPartyName(player);
		return name != null ? name : defaultName;
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
