package top.mrxiaom.doomsdayessentials.configs;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BackConfig {
	final File configPath;
	Map<String, YamlConfiguration> playerDatas;
	final Main plugin;

	public BackConfig(Main plugin) {
		this.plugin = plugin;
		this.configPath = new File(plugin.getDataFolder(), "back");
		this.reloadConfig();
	}

	public YamlConfiguration getPlayerConfig(String player) {
		if (!playerDatas.keySet().contains(player)) {
			return new YamlConfiguration();
		}
		return playerDatas.get(player);
	}

	public Location[] getBackPoints(String player) {
		Location[] locs = new Location[7];
		if (!playerDatas.keySet().contains(player)) {
			return locs;
		}
		YamlConfiguration config = playerDatas.get(player);
		for (int i = 0; i < 7; i++) {
			if (config.contains(String.valueOf(i)) && config.isConfigurationSection(String.valueOf(i))) {
				ConfigurationSection conf = config.getConfigurationSection(String.valueOf(i));
				World world = Bukkit.getWorld(conf.getString("world"));
				if (world != null) {
					locs[i] = new Location(world, conf.getDouble("x"), conf.getDouble("y"), conf.getDouble("z"),
							Util.getFloatFromConfig(conf, "yaw"), Util.getFloatFromConfig(conf, "pitch"));
				}
			}
		}
		return locs;
	}

	public void addBackPoint(Player player, Location location) {
		addBackPoint(player, location, false);
	}

	public boolean addBackPoint(Player player, Location location, boolean death) {
		if(plugin.getParkoursConfig().getParkourByLoc(location) != null) return false;
		if (plugin.getPlayerConfig().getConfig()
				.getBoolean(player.getName() + ".show-back-message" + (death ? "-death" : ""), true)) {
			player.sendMessage(I18n.t(death ? "back.death" : "back.saved", true));
		}
		YamlConfiguration config = new YamlConfiguration();
		Location[] locs = this.getBackPoints(player.getName());

		for (int i = locs.length - 2; i >= 0; i--) {
			locs[i + 1] = locs[i];
		}
		locs[0] = location;
		for (int i = 0; i < locs.length; i++) {
			if (locs[i] == null)
				continue;
			Location loc = locs[i];
			config.set(i + ".world", loc.getWorld().getName());
			config.set(i + ".x", loc.getX());
			config.set(i + ".y", loc.getY());
			config.set(i + ".z", loc.getZ());
			config.set(i + ".yaw", loc.getYaw());
			config.set(i + ".pitch", loc.getPitch());
		}
		playerDatas.put(player.getName(), config);
		this.saveConfig();
		return true;
	}

	public boolean addBackPoint(String player, Location location) {
		return addBackPoint(player, location, false);
	}

	public boolean addBackPoint(String player, Location location, boolean death) {
		Player p = Bukkit.getPlayer(player);
		if (p != null) {
			return addBackPoint(p, location, death);
		}
		return false;
	}

	public BackConfig reloadConfig() {
		try {
			if (!configPath.exists()) {
				configPath.mkdirs();
			}
			if (this.playerDatas == null) {
				this.playerDatas = new HashMap<String, YamlConfiguration>();
			}
			this.playerDatas.clear();
			for (File file : configPath.listFiles()) {
				try {
					if (file.getName().toLowerCase().endsWith(".yml")) {
						String playerName = file.getName().substring(0, file.getName().lastIndexOf(".yml"));
						if (!playerName.isEmpty()) {
							this.playerDatas.put(playerName, YamlConfiguration.loadConfiguration(file));
						}
					}
				} catch (Throwable t) {
					t.printStackTrace();
					continue;
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return this;
	}

	public BackConfig saveConfig() {
		try {
			Set<String> players = this.playerDatas.keySet();
			for (String player : players) {
				playerDatas.get(player).save(new File(configPath, player + ".yml"));
			}

			for (File file : configPath.listFiles()) {
				String playerName = file.getName().substring(0, file.getName().lastIndexOf(".yml"));
				if (!players.contains(playerName)) {
					file.delete();
				}
			}

		} catch (Throwable t) {
			t.printStackTrace();
		}
		return this;
	}
}
