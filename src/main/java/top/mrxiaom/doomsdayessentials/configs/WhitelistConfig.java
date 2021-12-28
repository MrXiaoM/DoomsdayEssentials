package top.mrxiaom.doomsdayessentials.configs;

import com.google.common.collect.Lists;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.doomsdayessentials.Main;

import java.io.File;
import java.util.List;

public class WhitelistConfig {
	final Main plugin;
	final File configFile;
	FileConfiguration config;
	public WhitelistConfig(Main plugin) {
		this.plugin = plugin;
		this.configFile = new File(plugin.getDataFolder(), "whitelist.yml");
		this.reloadConfig();
	}
	
	public List<String> getSavedPlayers(){
		return Lists.newArrayList(this.config.getKeys(false));
	}
	
	public boolean isSavedPlayer(String player) {
		return this.config.contains(player);
	}
	
	public String getPlayerBindQQ(String player) {
		return this.config.getString(player, "");
	}
	
	public WhitelistConfig set(String key, Object value) {
		this.config.set(key, value);
		return this;
	}
	
	public FileConfiguration getConfig() {
		return config;
	}
	
	public WhitelistConfig reloadConfig() {
		try{
			if(configFile.exists()) {
				this.config = YamlConfiguration.loadConfiguration(configFile);
			}
			else {
				this.config = new YamlConfiguration();
			}
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return this;
	}
	
	public WhitelistConfig saveConfig() {
		try{
			this.config.save(configFile);
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return this;
	}
}
