package top.mrxiaom.doomsdayessentials.configs;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.Util;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class RandomTPConfig {
	final File configFile;
	final File cacheFile;
	final Main plugin;
	private static final List<Material> damager = Lists.newArrayList(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR, Material.WATER, Material.LAVA,
			Material.COBWEB, Material.FIRE, Material.CAMPFIRE, Material.ACACIA_PRESSURE_PLATE,
			Material.BIRCH_PRESSURE_PLATE, Material.DARK_OAK_PRESSURE_PLATE, Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
			Material.JUNGLE_PRESSURE_PLATE, Material.LIGHT_WEIGHTED_PRESSURE_PLATE, Material.OAK_PRESSURE_PLATE,
			Material.SPRUCE_PRESSURE_PLATE, Material.STONE_PRESSURE_PLATE, Material.CACTUS, Material.NETHER_PORTAL,
			Material.END_PORTAL, Material.TRIPWIRE, Material.TRIPWIRE_HOOK, Material.END_GATEWAY, Material.MAGMA_BLOCK);

	public enum TeleportResult {
		SUCCESS, NO_LOC, NO_MONEY, NO_PERM
	}

	public enum TeleportMode {
		TOP, TOP_GROUND, GROUND;
	}

	public class Zone {
		boolean enable = true;
		private String name;
		private World world;
		private double x1, y1, z1, x2, y2, z2;
		private int price;
		private TeleportMode mode;
		private List<String> commands;

		Zone(String name, String worldName, double x1, double y1, double z1, double x2, double y2, double z2,
				int price, TeleportMode mode, List<String> commands) {

			this.world = Bukkit.getWorld(worldName);
			if (this.world == null) {
				Bukkit.getLogger().warning("[RL] 随机传送 " + name + " 找不到世界 " + worldName);
				enable = false;
				return;
			}
			this.name = name;
			this.x1 = x1;
			this.x2 = x2;
			this.y1 = y1;
			this.y2 = y2;
			this.z1 = z1;
			this.z2 = z2;
			this.price = price;
			this.mode = mode;
			this.commands = commands;
		}

		public TeleportMode getMode() {
			return this.mode;
		}

		public Zone setMode(TeleportMode mode) {
			this.mode = mode;
			return this;
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
		
		public int getY(int x, int z) {
			if (mode.equals(TeleportMode.TOP)) {
				Block block = world.getHighestBlockAt(x, z);
				if (!damager.contains(block.getType())) {
					return block.getY() + 1;
				}
				return -1;
			}
			int i = Util.getIntegerMin((int) y1, (int) y2);
			int j = Util.getIntegerMin(Util.getIntegerMax((int) y1, (int) y2), world.getHighestBlockYAt(x, z) - 2);
			if (mode.equals(TeleportMode.GROUND)) {
				// 从最小值开始遍历
				for (; i < j; i++) {
					Block block = world.getBlockAt(x, i, z);
					// 如果该方块不危险且上方两格都是空气则传送
					if (!damager.contains(block.getType())) {
						if (block.getRelative(BlockFace.UP, 1).getType().isAir()
								&& block.getRelative(BlockFace.UP, 2).getType().isAir()) {
							return i + 1;
						}
					}
				}
				return -1;
			}
			if (mode.equals(TeleportMode.TOP_GROUND)) {
				// 从最小值开始遍历，到这一格的最上面一个方块
				for (i = Util.getIntegerMax(i, 63); i < j; i++) {
					Block block = world.getBlockAt(x, i, z);
					// 如果该方块不危险且上方两格都是空气则传送
					if (!damager.contains(block.getType())) {
						if (block.getRelative(BlockFace.UP, 1).getType().isAir()
							&& block.getRelative(BlockFace.UP, 2).getType().isAir()) {
							return i + 1;
						}
					}
				}
				return -1;
			}
			return -1;
		}
		
		@Nullable
		public Location getLocation(int tryTimes) {
			if (tryTimes <= 0)
				return null;
			int randomX = Util.randomIntegerBetween((int) x1, (int) x2);
			int randomZ = Util.randomIntegerBetween((int) z1, (int) z2);
			int y = this.getY(randomX, randomZ);
			if (y < 1.145141919810) {
				return getLocation(tryTimes - 1);
			}
			return new Location(world, randomX, y, randomZ);
		}

		public TeleportResult teleport(Player p) {
			return teleport(p, false);
		}
		
		public TeleportResult teleport(Player p, boolean force) {
			if (!force && !p.hasPermission("randomlocation.teleport." + name)) {
				return TeleportResult.NO_PERM;
			}
			if (!force && price > 0 && plugin.getEcoApi().getBalance(p) < price) {
				return TeleportResult.NO_MONEY;
			}
			Location loc = randomCacheLocation(name, true);
			if (loc == null) {
				loc = getLocation(3);
				if(loc == null) {
					return TeleportResult.NO_LOC;
				}
			}
			plugin.getBackConfig().addBackPoint(p, p.getLocation());
			p.teleport(loc);
			return TeleportResult.SUCCESS;
		}

		public World getWorld() {
			return world;
		}

		public double getX1() {
			return x1;
		}

		public double getY1() {
			return y1;
		}

		public double getZ1() {
			return z1;
		}

		public double getX2() {
			return x2;
		}

		public double getY2() {
			return y2;
		}

		public double getZ2() {
			return z2;
		}

		public int getPrice() {
			return price;
		}

		public Zone setName(String name) {
			this.name = name;
			return this;
		}

		public Zone setWorld(World world) {
			this.world = world;
			return this;
		}

		public Zone setX1(double x1) {
			this.x1 = x1;
			return this;
		}

		public Zone setY1(double y1) {
			this.y1 = y1;
			return this;
		}

		public Zone setZ1(double z1) {
			this.z1 = z1;
			return this;
		}

		public Zone setX2(double x2) {
			this.x2 = x2;
			return this;
		}

		public Zone setY2(double y2) {
			this.y2 = y2;
			return this;
		}

		public Zone setZ2(double z2) {
			this.z2 = z2;
			return this;
		}

		public Zone setPrice(int price) {
			this.price = price;
			return this;
		}

		public List<String> getCommands() {
			return commands;
		}

		public Zone setCommands(List<String> commands) {
			this.commands = commands;
			return this;
		}
		
		public Zone save() {
			set(name, this);
			return this;
		}
	}

	private Map<String, Zone> zoneMap = new HashMap<>();
	private Map<String, List<Location>> cacheMap = new HashMap<>();
	public RandomTPConfig(Main plugin) {
		this.plugin = plugin;
		this.configFile = new File(plugin.getDataFolder().getAbsolutePath() + "\\random_tp\\");
		this.cacheFile = new File(plugin.getDataFolder().getAbsolutePath() + "\\random_tp_cache\\");
		this.reloadConfig().loadCache();
	}

	public void addToCache(String zone, Location loc) {
		if(this.contains(zone)) {
			List<Location> list = this.cacheMap.containsKey(zone) ? this.cacheMap.get(zone) : new ArrayList<>();
			list.add(loc);
			this.cacheMap.put(zone, list);
		}
	}

	public void addToCache(Zone zone, int x, int y, int z) {
		List<Location> list = this.cacheMap.containsKey(zone.getName()) ? this.cacheMap.get(zone.getName()) : new ArrayList<>();
		list.add(new Location(zone.getWorld(), x, y, z));
		this.cacheMap.put(zone.getName(), list);
	}
	
	public void addToCache(String zoneName, int x, int y, int z) {
		if(this.contains(zoneName)) {
			Zone zone = this.get(zoneName);
			if(zone == null) return;
			List<Location> list = this.cacheMap.containsKey(zone.getName()) ? this.cacheMap.get(zone.getName()) : new ArrayList<>();
			list.add(new Location(zone.getWorld(), x, y, z));
			this.cacheMap.put(zone.getName(), list);
		}
	}
	
	@Nullable
	public Location randomCacheLocation(String zone) {
		return randomCacheLocation(zone, true);
	}
	
	@Nullable
	public Location randomCacheLocation(String zone, boolean remove) {
		if(!this.cacheMap.containsKey(zone)) return null;
		List<Location> list = this.cacheMap.get(zone);
		int i = new Random().nextInt(list.size());
		Location loc = list.get(i);
		if(remove) {
			list.remove(i);
			this.cacheMap.put(zone, list);
		}
		return loc;
	}
	
	@Nullable
	public List<Location> getCacheLocations(String zone){
		if(!this.cacheMap.containsKey(zone)) return null;
		return this.cacheMap.get(zone);
	}
	
	public boolean needToCache(String zone) {
		if(!this.cacheMap.containsKey(zone)) return true;
		return this.cacheMap.get(zone).size() < 100;
	}
	
	public boolean contains(String zoneName) {
		return this.zoneMap.containsKey(zoneName);
	}
	
	public List<Zone> getZonesNeedToCache(){
		List<Zone> list = new ArrayList<>();
		for(Zone zone : zoneMap.values()) {
			if(needToCache(zone.getName())) list.add(zone);
		}
		return list;
	}

	public List<Zone> getAllZone() {
		return Lists.newArrayList(zoneMap.values());
	}

	public List<String> getZoneNameList(int page) {
		return this.getZoneNameList(10, page);
	}
	@Nullable
	public Zone newZone(String name, String world, double x1, double y1, double z1,
			double x2, double y2, double z2) {
		return newZone(name, world, x1, y1, z1, x2, y2, z2, 0, TeleportMode.TOP_GROUND, new ArrayList<>());
	}
	@Nullable
	public Zone newZone(String name, String world, double x1, double y1, double z1,
			double x2, double y2, double z2, int price) {
		return newZone(name, world, x1, y1, z1, x2, y2, z2, price, TeleportMode.TOP_GROUND, new ArrayList<>());
	}
	@Nullable
	public Zone newZone(String name, String world, double x1, double y1, double z1,
			double x2, double y2, double z2, int price, TeleportMode mode) {
		return newZone(name, world, x1, y1, z1, x2, y2, z2, price, mode, new ArrayList<>());
	}
	@Nullable
	public Zone newZone(String name, String world, double x1, double y1, double z1, 
			double x2, double y2, double z2, int price, TeleportMode mode, List<String> commands) {
		if(this.zoneMap.containsKey(name)) return null;
		Zone zone = new Zone(name, world, x1, y1, z1, x2, y2, z2, price, mode, commands);
		this.zoneMap.put(name, zone);
		this.saveConfig();
		return zone;
	}
	
	public List<String> getZoneNameList(int count, int page) {
		// “节 省 资 源” 垃 圾 佬
		int i = (page - 1) * count;
		List<String> allZoneList = new ArrayList<>();
		if (zoneMap.size() <= i)
			return allZoneList;
		allZoneList.addAll(zoneMap.keySet());

		if (page == 1 && allZoneList.size() < count) {
			return allZoneList;
		}

		List<String> zones = new ArrayList<>();
		for (; i < allZoneList.size(); i++) {
			zones.add(allZoneList.get(i));
		}
		return zones;
	}

	public List<String> getAllZoneName() {
		return Lists.newArrayList(this.zoneMap.keySet());
	}

	public int getPages() {
		return this.getPages(10);
	}

	public int getPages(int count) {
		return ((int) (this.zoneMap.size() / count)) + 1;
	}

	@Nullable
	public Zone get(String zoneName) {
		if (this.contains(zoneName)) {
			return this.zoneMap.get(zoneName);
		}
		return null;
	}

	public RandomTPConfig set(String zoneName, Zone zone) {
		this.zoneMap.put(zoneName, zone);
		this.saveConfig();
		return this;
	}

	public RandomTPConfig remove(String zoneName) {
		if (this.contains(zoneName)) {
			this.zoneMap.remove(zoneName);
			this.saveConfig();
		}
		return this;
	}
	
	public RandomTPConfig loadCache() {
		if (!cacheFile.exists()) {
			cacheFile.mkdirs();
		}
		this.cacheMap = new HashMap<>();
		try {
			File[] filelist = configFile.listFiles();
			if(filelist == null) return this;
			for (File file : filelist) {
				try {
					YamlConfiguration zoneConfig = YamlConfiguration.loadConfiguration(file);
					String name = file.getName().replace(".yml", "");
					Zone zone = this.get(name);
					if(zone == null) {
						plugin.getLogger().warning("载入缓存时无法找到对应配置 " + name + "，已删除缓存");
						file.delete();
						continue;
					}
					World world = zone.getWorld();
					List<Location> loc = new ArrayList<>();
					for(String s : zoneConfig.getStringList("cache")) {
						String[] a = s.split(",");
						if(a.length == 3) {
							try {
								int x = Integer.parseInt(a[0]);
								int y = Integer.parseInt(a[1]);
								int z = Integer.parseInt(a[2]);
								loc.add(new Location(world, x, y, z));
							} catch(NumberFormatException e) {
								plugin.getLogger().warning("随机传送缓存 " + name + " 中出现了一个异常项 " + s);
							}
						}
					}
					if(loc.isEmpty()) {
						continue;
					}
					this.cacheMap.put(name, loc);
				} catch (Throwable t) {
					// 收声
				}
			}
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return this;
	}

	public RandomTPConfig saveCache() {
		try {
			if (!cacheFile.exists()) {
				cacheFile.mkdirs();
			}
			List<String> files = new ArrayList<String>();
			for (String key : cacheMap.keySet()) {
				try {
					Zone zone = zoneMap.get(key);
					if(zone == null) continue;
					YamlConfiguration cacheConfig = new YamlConfiguration();
					List<String> locs = new ArrayList<>();
					for(Location loc : cacheMap.get(key)) {
						locs.add(loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ());
					}
					cacheConfig.set("cache", locs);
					cacheConfig.save(new File(cacheFile, key + ".yml"));
					files.add(key + ".yml");
				} catch (Throwable t) {
					// 收声
				}
			}
			File[] filelist = cacheFile.listFiles();
			if(filelist == null) return this;
			for (File file : filelist) {
				if (!files.contains(file.getName())) {
					file.delete();
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return this;
	}
	public RandomTPConfig reloadConfig() {
		if (!configFile.exists()) {
			this.saveConfig();
		}

		this.zoneMap = new HashMap<>();
		if (configFile.exists()) {
			File[] filelist = configFile.listFiles();
			if(filelist == null) return this;
			for (File file : filelist) {
				try {
					YamlConfiguration zoneConfig = YamlConfiguration.loadConfiguration(file);
					String name = zoneConfig.getString("name");
					String worldName = zoneConfig.getString("world");
					double x1 = zoneConfig.getDouble("x1");
					double y1 = zoneConfig.getDouble("y1");
					double z1 = zoneConfig.getDouble("z1");
					double x2 = zoneConfig.getDouble("x2");
					double y2 = zoneConfig.getDouble("y2");
					double z2 = zoneConfig.getDouble("z2");
					int price = zoneConfig.getInt("price");
					List<String> commands = zoneConfig.getStringList("commands");
					TeleportMode mode = Util.valueOf(TeleportMode.class, zoneConfig.getString("mode", "TOP_GROUND"),
							TeleportMode.TOP_GROUND);
					this.zoneMap.put(name, new Zone(name, worldName, x1, y1, z1, x2, y2, z2, price, mode, commands));
				} catch (Throwable t) {
					// 收声
				}
			}
		}
		return this;
	}
	
	public RandomTPConfig saveConfig() {
		try {
			if (!configFile.exists()) {
				configFile.mkdirs();
			}
			List<String> files = new ArrayList<String>();
			for (String key : zoneMap.keySet()) {
				try {
					Zone zone = zoneMap.get(key);
					YamlConfiguration zoneConfig = new YamlConfiguration();
					zoneConfig.set("name", zone.getName());
					zoneConfig.set("world", zone.getWorld().getName());
					zoneConfig.set("x1", zone.getX1());
					zoneConfig.set("y1", zone.getY1());
					zoneConfig.set("z1", zone.getZ1());
					zoneConfig.set("x2", zone.getX2());
					zoneConfig.set("y2", zone.getY2());
					zoneConfig.set("z2", zone.getZ2());
					zoneConfig.set("price", zone.getPrice());
					zoneConfig.set("mode", zone.getMode().name().toUpperCase());
					zoneConfig.set("commands", zone.getCommands());
					zoneConfig.save(new File(configFile, zone.getName() + ".yml"));
					files.add(zone.getName() + ".yml");
				} catch (Throwable t) {
					// 收声
				}
			}
			File[] filelist = configFile.listFiles();
			if(filelist == null) return this;
			for (File file : filelist) {
				if (!files.contains(file.getName())) {
					file.delete();
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return this;
	}
}
