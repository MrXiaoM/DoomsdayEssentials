package top.mrxiaom.doomsdayessentials.configs;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class McbbsConfig {
	final File configFile;
	FileConfiguration config;
	final Main plugin;
	public McbbsConfig(Main plugin) {
		this.plugin = plugin;
		this.configFile = new File(plugin.getDataFolder(), "mcbbs.yml");
		this.reloadConfig();
	}
	
	public boolean isNowMatchedLastDate() {
		return this.config.getString("last-date", "").equalsIgnoreCase(new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTimeInMillis()));
	}
	
	public void setLastDateToNow(String player) {
		if(this.isNowMatchedLastDate()) return;
		this.config.set("players." + player + ".times", this.getPlayerUpTimes(player) + 1);
		this.config.set("last-date", new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
		this.config.set("last-player", player);
		this.saveConfig();
	}
	
	public String getLastDate() {
		return this.config.getString("last-date", "");
	}
	
	public String getLastPlayer() {
		return this.config.getString("last-player", "");
	}
	
	public FileConfiguration getConfig() {
		return config;
	}
	
	public void setReward(ItemStack item) {
		if(item == null) return;
		config.set("reward-item", ItemStackUtil.itemStackArrayToBase64(new ItemStack[] { item }));
		this.saveConfig();
	}
	
	@Nullable
	public ItemStack genReward() {
		ItemStack[] items = ItemStackUtil.itemStackArrayFromBase64(config.getString("reward-item", ""), true);
		if(items.length < 1) return null;
		return items[0];
	}
	
	public int getLastSize() {
		return config.getInt("last-size", 0);
	}
	
	public void setLastSize(int size) {
		this.config.set("last-size", size);
		this.saveConfig();
	}
	
	public boolean isPlayerBound(String player) {
		return this.config.contains("players." + player + ".uid");
	}

	public int getPlayerUpTimes(String player) {
		return this.config.contains("players." + player + ".times") ? this.config.getInt("players." + player + ".times") : 0;
	}
	
	public void addPlayerUpTimes(String player) {
		this.setPlayerUpTimes(player, this.getPlayerUpTimes(player) + 1);
	}
	
	public void setPlayerUpTimes(String player, int times) {
		this.config.set("players." + player + ".times", times);
		this.saveConfig();
	}
	
	@Nullable
	public String getPlayerUid(String player) {
		if(!this.config.contains("players." + player + ".uid")) {
			return null;
		}
		return this.config.getString("players." + player + ".uid");
	}
	
	@Nullable
	public String getUidPlayer() {
		ConfigurationSection c = this.config.getConfigurationSection("players");
		if(c == null) return null;
		for(String player : c.getKeys(false)) {
			if(getPlayerUid(player) != null) return player;
		}
		return null;
	}
	
	public void setPlayerUid(String player, String uid) {
		this.config.set("players." + player + ".uid", uid);
		this.saveConfig();
	}
	
	public void removeUidBinding() {
		for(String player : this.config.getConfigurationSection("players").getKeys(false)) {
			this.setPlayerUid(player, "");
		}
		this.saveConfig();
	}
	
	public void reloadConfig() {
		try {
			if(!this.configFile.exists()) {
				plugin.saveResource("mcbbs.yml", true);
			}
			this.config = YamlConfiguration.loadConfiguration(configFile);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void saveConfig() {
		try {
			this.config.save(configFile);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
