package top.mrxiaom.doomsdayessentials.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.KitConfig.Kit;
import top.mrxiaom.doomsdayessentials.configs.PlayerConfig.LastSignInfo;
import top.mrxiaom.doomsdayessentials.configs.PlayerConfig.SignTime;

public class PlaceholderReplace extends PlaceholderExpansion {
	private final Main plugin;

	public PlaceholderReplace(Main plugin) {
		this.plugin = plugin;
	}

	public @NotNull String getAuthor() {
		return "mrxiaom";
	}

	public @NotNull String getIdentifier() {
		return "replace";
	}

	public String onRequest(OfflinePlayer player, String identifier) {
		if(identifier.contains("#")){
			String[] args = identifier.split("#");
			String target = PlaceholderAPI.setPlaceholders(player, args[0].replace("{", "%").replace("}", "%"));
			for (int i = 1; i < args.length; i++){
				String s = PlaceholderAPI.setPlaceholders(player, args[i].replace("{", "%").replace("}", "%"));
				if(s.contains(":")) {
					target = target.replace(
							s.substring(0, s.indexOf(":")),
							s.substring(s.indexOf(":") + 1)
					);
				}
			}
			return target;
		}
		return identifier;
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
