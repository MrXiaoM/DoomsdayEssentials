package top.mrxiaom.doomsdayessentials.configs;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.Util;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.*;

public class WarpConfig {
	final File configFile;
	final Main plugin;

	public static class Warp {
		boolean enable = true;
		private String name;
		private Location location;
		private boolean hidden;
		private Material material;
		public Warp(String name, String worldName, double x, double y, double z, float yaw, float pitch,
				boolean hidden, Material material) {
			boolean hasWorld = false;
			for (World w : Bukkit.getWorlds()) {
				if (w.getName().equals(worldName)) {
					hasWorld = true;
					break;
				}
			}
			if (!hasWorld) {
				Bukkit.getLogger().warning("[warp] 地标 " + name + " 找不到世界 " + worldName);
				enable = false;
				return;
			}
			this.name = name;
			this.location = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
			this.hidden = hidden;
			this.material = material;
		}

		public Warp(String name, Location location) {
			this(name, location, false, Material.ITEM_FRAME);
		}

		public Warp(String name, Location location, boolean hidden, Material material) {
			this.name = name;
			this.location = location;
			this.hidden = hidden;
			this.material = material;
		}

		public boolean isHidden() {
			return this.hidden;
		}

		public void setHidden(boolean hidden) {
			this.hidden = hidden;
		}

		public String getName() {
			return this.name;
		}

		public boolean isNameEqualIgnoreCase(String name) {
			return this.name.equalsIgnoreCase(name);
		}

		public boolean isEnable() {
			return this.enable;
		}

		public Location getLocation() {
			return this.location;
		}

		public boolean teleport(Player p) {
			return p.teleport(this.location);
		}

		public Material getMaterial() {
			return material;
		}

		public void setMaterial(Material material) {
			this.material = material;
		}
	}

	private Map<String, Warp> warpMap = new HashMap<String, Warp>();

	public WarpConfig(Main plugin) {
		this.plugin = plugin;
		this.configFile = new File(plugin.getDataFolder().getAbsolutePath() + "\\warps\\");
		this.reloadConfig();
	}

	public boolean contains(String warpName) {
		return this.warpMap.containsKey(warpName);
	}

	public List<String> getWarpNameList(int page) {
		return this.getWarpNameList(10, page);
	}

	public List<String> getWarpNameList(int count, int page) {
		// “节 省 资 源” 垃 圾 佬
		int i = (page - 1) * count;
		List<String> allWarpList = new ArrayList<String>();
		if (warpMap.size() <= i)
			return allWarpList;
		for (String key : warpMap.keySet()) {
			allWarpList.add(key);
		}

		if (page == 1 && allWarpList.size() < count) {
			return allWarpList;
		}

		List<String> warps = new ArrayList<String>();
		for (; i < allWarpList.size(); i++) {
			warps.add(allWarpList.get(i));
		}
		return warps;
	}

	public List<Warp> getWarpList() {
		return Lists.newArrayList(this.warpMap.values());
	}
	
	public List<Warp> getWarpList(int page) {
		return this.getWarpList(page, true);
	}

	public List<Warp> getWarpList(int page, boolean showHidden) {
		return this.getWarpList(45, page, showHidden);
	}

	public List<Warp> getWarpList(int count, int page, boolean showHidden) {
		// TODO 隐藏地标
		int i = (page - 1) * count;
		List<Warp> allWarpList = new ArrayList<Warp>();
		if (warpMap.size() <= i || page < 1)
			return allWarpList;
		List<String> keySet = new ArrayList<String>(warpMap.keySet());
		keySet.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return Collator.getInstance(Locale.CHINESE).compare(o1, o2);
			}
		});
		for (String key : keySet) {
			Warp w = warpMap.get(key);
			if (!w.isHidden() || showHidden) {
				allWarpList.add(warpMap.get(key));
			}
		}

		if (page == 1 && allWarpList.size() < count) {
			return allWarpList;
		}

		List<Warp> warps = new ArrayList<Warp>();
		for (; i < i + count; i++) {
			if (i >= allWarpList.size())
				break;
			warps.add(allWarpList.get(i));
		}
		return warps;
	}

	public List<String> getAllWarps() {
		List<String> allWarpList = new ArrayList<String>();
		for (String key : warpMap.keySet()) {
			allWarpList.add(key);
		}
		return allWarpList;
	}

	public int getPages() {
		return this.getPages(10);
	}

	public int getPages(int count) {
		return ((int) (this.warpMap.size() / count)) + 1;
	}

	@Nullable
	public Warp get(String warpName) {
		if (this.contains(warpName)) {
			return this.warpMap.get(warpName);
		}
		return null;
	}

	public void set(String warpName, Warp warp) {
		this.warpMap.put(warpName, warp);
		this.saveConfig();
	}

	public void remove(String warpName) {
		if (this.contains(warpName)) {
			this.warpMap.remove(warpName);
			this.saveConfig();
		}
	}

	public void reloadConfig() {
		if (!configFile.exists()) {
			this.saveConfig();
		}

		this.warpMap = new HashMap<String, Warp>();
		this.warpMap.clear();
		if (configFile.exists()) {
			for (File file : configFile.listFiles()) {
				try {
					YamlConfiguration warpConfig = YamlConfiguration.loadConfiguration(file);
					String name = warpConfig.getString("name");
					Material material = Util.valueOf(Material.class, warpConfig.getString("icon"), Material.ITEM_FRAME);
					String worldName = warpConfig.getString("world");
					double x = warpConfig.getDouble("x");
					double y = warpConfig.getDouble("y");
					double z = warpConfig.getDouble("z");
					boolean hidden = warpConfig.getBoolean("hidden");
					float yaw = Util.getFloatFromConfig(warpConfig, "yaw");
					float pitch = Util.getFloatFromConfig(warpConfig, "pitch");
					this.warpMap.put(name, new Warp(name, worldName, x, y, z, yaw, pitch, hidden, material));
				} catch (IllegalArgumentException e) {
					continue;
				}
			}
		}
	}

	public void saveConfig() {
		try {
			if (!configFile.exists()) {
				configFile.mkdirs();
			}
			List<String> files = new ArrayList<String>();
			for (String key : warpMap.keySet()) {
				try {
					Warp warp = warpMap.get(key);
					YamlConfiguration warpConfig = new YamlConfiguration();
					Location location = warp.getLocation();
					warpConfig.set("name", warp.getName());
					warpConfig.set("icon", warp.getMaterial().name());
					warpConfig.set("world", location.getWorld().getName());
					warpConfig.set("x", location.getX());
					warpConfig.set("y", location.getY());
					warpConfig.set("z", location.getZ());
					warpConfig.set("yaw", location.getYaw());
					warpConfig.set("pitch", location.getPitch());
					warpConfig.set("hidden", warp.isHidden());
					warpConfig.save(new File(configFile, warp.getName() + ".yml"));
					files.add(warp.getName() + ".yml");
				} catch (IllegalArgumentException e) {
					continue;
				}
			}
			for (File file : configFile.listFiles()) {
				if (!files.contains(file.getName())) {
					file.delete();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
