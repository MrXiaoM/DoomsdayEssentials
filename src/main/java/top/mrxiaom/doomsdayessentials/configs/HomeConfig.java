package top.mrxiaom.doomsdayessentials.configs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.Util;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

public class HomeConfig {
	final File configPath;
	Map<String, YamlConfiguration> playerDatas;
	final Main plugin;

	public HomeConfig(Main plugin) {
		this.plugin = plugin;
		this.configPath = new File(plugin.getDataFolder(), "home");
		this.reloadConfig();
	}

	public YamlConfiguration getPlayerConfig(String player) {
		if (!playerDatas.containsKey(player)) {
			return new YamlConfiguration();
		}
		return playerDatas.get(player);
	}

	public int getHomeCount(String player) {
		if (!playerDatas.containsKey(player)) {
			return 0;
		}
		int result = 0;
		for (String key : playerDatas.get(player).getKeys(false)) {
			if (playerDatas.get(player).isConfigurationSection(key)) {
				result++;
			}
		}
		return result;
	}

	public boolean hasHome(String player, String homeName) {
		if (!playerDatas.containsKey(player)) {
			return false;
		}
		YamlConfiguration config = playerDatas.get(player);
		if (config.contains(homeName) && config.isConfigurationSection(homeName)) {
			return true;
		}
		return false;
	}

	public List<String> getHomes(String player) {
		if (!playerDatas.containsKey(player)) {
			return new ArrayList<>();
		}
		List<String> homes = new ArrayList<String>();
		YamlConfiguration config = playerDatas.get(player);
		for (String key : config.getKeys(false)) {
			if (config.isConfigurationSection(key)) {
				homes.add(key);
			}
		}
		return homes;
	}

	@Nullable
	public Location getHome(String player, String homeName) {
		if (!hasHome(player, homeName))
			return null;

		YamlConfiguration config = playerDatas.get(player);
		ConfigurationSection home = config.getConfigurationSection(homeName);

		String worldName = home.getString("world");
		double x = home.getDouble("x");
		double y = home.getDouble("y");
		double z = home.getDouble("z");
		float yaw = Util.getFloatFromConfig(home, "yaw");
		float pitch = Util.getFloatFromConfig(home, "pitch");
		return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
	}

	public void setHome(String player, String homeName, Location loc) {
		YamlConfiguration config = playerDatas.containsKey(player) ? playerDatas.get(player) : new YamlConfiguration();
		config.set("playerName", player);
		ConfigurationSection home = (config.contains(homeName) && config.isConfigurationSection(homeName))
				? config.getConfigurationSection(homeName)
				: config.createSection(homeName);

		home.set("world", loc.getWorld().getName());
		home.set("x", loc.getX());
		home.set("y", loc.getY());
		home.set("z", loc.getZ());

		home.set("yaw", loc.getYaw());
		home.set("pitch", loc.getPitch());

		config.set(homeName, home);
		playerDatas.put(player, config);
		this.saveConfig();
	}

	public void delHome(String player, String homeName) {
		YamlConfiguration config = playerDatas.containsKey(player) ? playerDatas.get(player) : new YamlConfiguration();
		config.set("playerName", player);
		if (config.contains(homeName)) {
			playerDatas.remove(player);
			playerDatas.put(player, removeKey(config, homeName));
		}
		this.saveConfig();
	}

	private YamlConfiguration removeKey(YamlConfiguration con, String key) {
		YamlConfiguration result = new YamlConfiguration();
		for (String k : con.getKeys(false)) {
			if (!k.equalsIgnoreCase(key)) {
				result.set(k, con.get(k));
			}
		}
		return result;
	}

	public void reloadConfig() {
		try {
			if (!configPath.exists()) {
				configPath.mkdirs();
			}
			if (this.playerDatas == null) {
				this.playerDatas = new HashMap<String, YamlConfiguration>();
			}
			this.playerDatas.clear();
			File[] files = configPath.listFiles();
			if(files == null) return;
			for (File file : files) {
				try {
					if (file.getName().toLowerCase().endsWith(".yml")) {
						String playerName = file.getName().substring(0, file.getName().lastIndexOf(".yml"));
						if (!playerName.isEmpty()) {
							this.playerDatas.put(playerName, YamlConfiguration.loadConfiguration(file));
						}
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void saveConfig() {
		try {
			Set<String> players = this.playerDatas.keySet();
			for (String player : players) {
				playerDatas.get(player).save(new File(configPath, player + ".yml"));
			}
			File[] files = configPath.listFiles();
			if(files == null) return;
			for (File file : files) {
				String playerName = file.getName().substring(0, file.getName().lastIndexOf(".yml"));
				if (!players.contains(playerName)) {
					file.delete();
				}
			}

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
