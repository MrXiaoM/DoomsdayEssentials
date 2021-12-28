package top.mrxiaom.doomsdayessentials.configs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.doomsdayessentials.Main;

import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class KeyConfig {

	final File configFile;
	FileConfiguration config;
	final Main plugin;

	public KeyConfig(Main plugin) {
		this.plugin = plugin;
		this.configFile = new File(plugin.getDataFolder(), "keys.yml");
		this.reloadConfig();
	}

	public void reloadConfig() {
		if(!configFile.exists()) {
			plugin.saveResource("keys.yml", false);
		}
		try {
			config = YamlConfiguration.loadConfiguration(configFile);
		} catch (Throwable t) {
			t.printStackTrace();
			InputStreamReader isr = new InputStreamReader(plugin.getResource("keys.yml"));
			config = YamlConfiguration.loadConfiguration(isr);
		}
	}

	public int getKeyAmount() {
		if (!(config.contains("keys") && config.isList("keys")))
			return 0;
		return config.getStringList("keys").size();
	}

	public int getUsedKeyAmount() {
		if (!(config.contains("keys-used") && config.isList("keys-used")))
			return 0;
		return config.getStringList("keys-used").size();
	}

	public void saveConfig() {
		try {
			config.save(configFile);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public String useKey(String player, String key) {
		List<String> list = (config.contains("used-keys") && config.isList("used-keys"))
				? config.getStringList("used-keys")
				: new ArrayList<String>();
		;
		list.add(key);
		config.set("used-keys", list);
		this.saveConfig();
		plugin.getPlayerConfig().addNeedle(player, 1);
		plugin.getLogger().info("玩家 " + player + "兑换了 1 支复活针");
		return "你成功为玩家 " + player + "兑换了 1 支复活针";
	}

	public boolean canKeyBeUse(String key) {
		if (!(config.contains("keys") && config.isList("keys")))
			return false;
		if (config.contains("used-keys") && config.isList("used-keys")) {
			if (config.getStringList("used-keys").contains(key)) {
				return false;
			}
		}
		return config.getStringList("keys").contains(key);
	}
}
