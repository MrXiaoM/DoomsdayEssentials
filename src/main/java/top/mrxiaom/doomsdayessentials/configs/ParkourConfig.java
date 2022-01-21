package top.mrxiaom.doomsdayessentials.configs;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdayessentials.Main;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParkourConfig {

	FileConfiguration config;
	final File configFile;
	final Main plugin;
	final Map<String, Parkour> parkours = new HashMap<>();

	public ParkourConfig(Main plugin) {
		this.plugin = plugin;
		configFile = new File(plugin.getDataFolder().getAbsolutePath() + "\\parkours.yml");
		this.reloadConfig();
	}

	public FileConfiguration getConfig() {
		return this.config;
	}

	@Nullable
	public Parkour getParkourByLoc(Location loc) {
		if (loc == null)
			return null;
		ClaimedResidence res = plugin.getResidenceApi().getByLoc(loc);
		if (res == null)
			return null;
		for (Parkour p : parkours.values()) {
			if (res.getName().equals(p.getRes())) {
				return p;
			}
		}
		return null;
	}

	@Nullable
	public Parkour getParkourPlayerIn(Player player) {
		if (player == null)
			return null;
		return this.getParkourByLoc(player.getLocation());
	}

	public boolean has(String id) {
		return parkours.containsKey(id);
	}

	public Parkour get(String id) {
		if (!has(id))
			return null;
		return parkours.get(id);
	}

	public void set(String id, Parkour parkour) {
		if (has(id))
			this.parkours.remove(id);
		this.parkours.put(id, parkour);
	}

	public static class Parkour {
		String id;
		String displayName;
		List<String> description;
		String res;
		int minY;
		List<String> checkPoints;
		List<String> winCommands;
		boolean visitable;

		public Parkour(String id, String res, String displayName, List<String> description, int minY,
				List<String> checkPoints, List<String> winCommands, boolean visitable) {
			this.id = id;
			this.res = res;
			this.displayName = displayName;
			this.description = description;
			this.minY = minY;
			this.checkPoints = checkPoints;
			this.winCommands = winCommands;
			this.visitable = visitable;
		}

		public String getId() {
			return id;
		}

		public String getRes() {
			return res;
		}

		public boolean isInCheckPoint(Player player, int index) {
			if (index < 0 || index >= checkPoints.size())
				return false;
			Location loc = player.getLocation();
			return checkPoints.get(index)
					.equalsIgnoreCase(loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
		}

		public ClaimedResidence getResidence() {
			return Main.getInstance().getResidenceApi().getByName(res);
		}

		public int getMinY() {
			return minY;
		}

		public List<String> getCheckPoints() {
			return checkPoints;
		}

		public List<String> getWinCommands() {
			return winCommands;
		}

		public void processWinCommands(Player player) {
			if (player == null || !player.isOnline())
				return;
			for (String cmd : winCommands) {
				cmd = cmd.replace("&", "§").replace("%player%", player.getName());
				if (cmd.toLowerCase().startsWith("[message]")) {
					player.sendMessage(cmd.substring(9));
				} else if (cmd.toLowerCase().startsWith("[console]")) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.substring(9));
				} else if (cmd.toLowerCase().startsWith("[player]")) {
					Bukkit.dispatchCommand(player, cmd.substring(8));
				}
			}
		}

		public boolean isVisitable() {
			return visitable;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setRes(String res) {
			this.res = res;
		}

		public void setMinY(int minY) {
			this.minY = minY;
		}

		public void setCheckPoints(List<String> checkPoints) {
			this.checkPoints = checkPoints;
		}

		public void setWinCommands(List<String> winCommands) {
			this.winCommands = winCommands;
		}

		public void setVisitable(boolean visitable) {
			this.visitable = visitable;
		}

		public String getDisplayName() {
			return displayName;
		}

		public List<String> getDescription() {
			return description;
		}

		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public void setDescription(List<String> description) {
			this.description = description;
		}
	}

	public void reloadConfig() {
		try {
			if (!configFile.exists()) {
				this.plugin.saveResource("parkours.yml", true);
			}
			config = YamlConfiguration.loadConfiguration(configFile);
			this.parkours.clear();
			for (String id : config.getKeys(false)) {
				String bindingRes = config.getString(id + ".binding-res");
				String displayName = config.getString(id + ".display-name");
				List<String> description = config.getStringList(id + ".description");
				int minY = config.getInt(id + ".fail-y");
				List<String> checkpoints = config.getStringList(id + ".checkpoints");
				List<String> winCommands = config.getStringList(id + ".win-commands");
				boolean visitable = config.getBoolean(id + ".visitable");

				this.parkours.put(id, new Parkour(id, bindingRes, displayName, description, minY, checkpoints,
						winCommands, visitable));
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void saveConfig() {
		try {
			this.config = new YamlConfiguration();
			for (Parkour p : this.parkours.values()) {
				this.config.set(p.id + ".binding-res", p.getRes());
				this.config.set(p.id + ".display-name", p.getDisplayName());
				this.config.set(p.id + ".description", p.getDescription());
				this.config.set(p.id + ".fail-y", p.getMinY());
				this.config.set(p.id + ".checkpoints", p.getCheckPoints());
				this.config.set(p.id + ".win-commands", p.getWinCommands());
				this.config.set(p.id + ".visitable", p.isVisitable());
			}
			this.config.save(this.configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void set(String key, String value, boolean saveConfig) {
		this.config.set(key, value);
		if (saveConfig)
			this.saveConfig();
	}

	public void set(String key, String value) {
		this.set(key, value, false);
	}

	public List<Parkour> all() {
		return Lists.newArrayList(this.parkours.values());
	}
}
