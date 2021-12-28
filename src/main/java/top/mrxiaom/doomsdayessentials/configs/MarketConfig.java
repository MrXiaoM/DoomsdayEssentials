package top.mrxiaom.doomsdayessentials.configs;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.google.common.collect.Lists;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.shop.Shop;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.*;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MarketConfig {

	FileConfiguration config;
	final File configFile;
	FileConfiguration data;
	final File dataFile;
	final Main plugin;
	final Map<String, String> markets = new HashMap<>();
	final Map<String, MarketData> marketsData = new HashMap<>();

	public MarketConfig(Main plugin) {
		this.plugin = plugin;
		configFile = new File(plugin.getDataFolder().getAbsolutePath() + "\\market.yml");
		dataFile = new File(plugin.getDataFolder().getAbsolutePath() + "\\market_data.yml");
		this.reloadConfig();
	}

	public FileConfiguration getConfig() {
		return this.config;
	}
	
	public ClaimedResidence getMarketResidence(String id) {
		return has(id) ? ResidenceApi.getResidenceManager().getByName(markets.get(id)) : null;
	}
	
	@Nullable
	public MarketData getMarketDataById(String id) {
		for (MarketData marketData : marketsData.values()) {
			if (marketData.getId().equals(id)) {
				return marketData;
			}
		}
		return null;
	}

	@Nullable
	public MarketData getMarketDataByOwner(Player player) {
		return this.getMarketDataByOwner(player.getName());
	}

	@Nullable
	public MarketData getMarketDataByOwner(String player) {
		for (MarketData marketData : marketsData.values()) {
			if (marketData.getOwner().equals(player)) {
				return marketData;
			}
		}
		return null;
	}

	@Nullable
	public String getMarketByLoc(Location loc) {
		if (loc == null)
			return null;
		ClaimedResidence res = plugin.getResidenceApi().getByLoc(loc);
		if (res == null)
			return null;
		for (String m : markets.keySet()) {
			if (res.getName().equals(markets.get(m))) {
				return m;
			}
		}
		return null;
	}

	public void onMarketRemoved(String id) {
		try {
			ClaimedResidence res = this.getMarketResidence(id);
			if (res == null)
				return;
			MarketData data = this.getMarketDataById(id);
			if (data != null && data.getSignLoc() != null) {
				Block b = data.getSignLoc().getBlock();
				if(b.getState() instanceof Sign) {
					((Sign) b.getState()).setLine(2, "");
					((Sign) b.getState()).setLine(3, "");
				}
			}
			World world = res.getMainArea().getWorld();
			Location lower = res.getMainArea().getLowLoc();
			Location higher = res.getMainArea().getHighLoc();
			int x1 = Util.getIntegerMin(lower.getBlockX(), higher.getBlockX());
			int x2 = Util.getIntegerMax(lower.getBlockX(), higher.getBlockX());
			int z1 = Util.getIntegerMin(lower.getBlockZ(), higher.getBlockZ());
			int z2 = Util.getIntegerMax(lower.getBlockZ(), higher.getBlockZ());
			int y1 = lower.getBlockY();
			int y2 = higher.getBlockY();
			List<Player> players = world.getPlayers();
			for (int y = y1; y < y2; y++) {
				for (int x = x1; x < x2; x++) {
					for (int z = z1; z < z2; z++) {
						Shop shop = QuickShop.getInstance().getShopManager()
								.getShopIncludeAttached(new Location(world, x, y, z));
						if (shop != null) {
							shop.getDisplay().remove();
							shop.delete();
						}
						Block block = world.getBlockAt(x, y, z);
						// 移除所有牌子
						if (ItemStackUtil.isSign(block.getType())) {
							System.out.println(block.getType());
							plugin.getCoreProtectApi().logRemoval("摊位" + id, block.getLocation(), block.getType(),
									block.getBlockData());
							NMSUtil.breakBlock(block);
							for (Player player : players) {
								player.sendBlockChange(block.getLocation(), NMSUtil.newBlockData(Material.AIR));
							}
						}
						// 清空箱子
						if (block.getState() instanceof Chest) {
							plugin.getCoreProtectApi().logContainerTransaction("摊位" + id, block.getLocation());
							if (((Chest) block.getState()).getInventory() != null)
								((Chest) block.getState()).getInventory().clear();
							if (((Chest) block.getState()).getBlockInventory() != null)
								((Chest) block.getState()).getBlockInventory().clear();
							if (((Chest) block.getState()).getSnapshotInventory() != null)
								((Chest) block.getState()).getSnapshotInventory().clear();
						}
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	@Nullable
	public String getMarketPlayerIn(Player player) {
		if (player == null)
			return null;
		return this.getMarketByLoc(player.getLocation());
	}

	public boolean has(String id) {
		return markets.containsKey(id);
	}

	public void putMarketData(MarketData market) {
		if (market == null)
			return;
		if (has(market.getId())) {
			this.marketsData.remove(market.getId());
			this.marketsData.put(market.getId(), market);
			this.saveConfig();
		}
	}

	public static class MarketData {
		String id;
		String owner;
		int year;
		int month;
		int day;
		int hour;
		int minute;
		int second;
		Location signLoc;

		public MarketData(String id, String owner, LocalDateTime outdateTime, Location signLoc) {
			this(id, owner, outdateTime.getYear(), outdateTime.getMonthValue(), outdateTime.getDayOfMonth(),
					outdateTime.getHour(), outdateTime.getMinute(), outdateTime.getSecond(), signLoc);
		}
		
		public MarketData(String id, String owner, int year, int month, int day, int hour, int minute, int second, Location signLoc) {
			
			this.id = id;
			this.owner = owner;
			this.year = year;
			this.month = month;
			this.day = day;
			this.hour = hour;
			this.minute = minute;
			this.second = second;
			this.signLoc = signLoc;
		}

		public String getId() {
			return id;
		}

		public String getOwner() {
			return owner;
		}

		public LocalDateTime getOutdateTime() {
			if(year < 114) return null;
			return LocalDateTime.of(year, month, day, hour, minute, second);
		}

		public void addOutdateTimeDay(int day) {
			LocalDateTime time = getOutdateTime();
			if(time == null) { 
				System.out.println(id + " 号摊位的到期时间获取失败，自动使用今天");
				time = LocalDateTime.now();
			}
			setOutdateTime(TimeUtil.addDay(time, day));
		}
		
		public String getOutdateTimeChinese() {
			if(year < 114) return "";
			return TimeUtil.getChineseTime(getOutdateTime());
		}


		public void setId(String id) {
			this.id = id;
		}

		public void setOwner(String owner) {
			this.owner = owner;
		}

		public void setOutdateTime(int year, int month, int day, int hour, int minute, int second) {
			this.year = year;
			this.month = month;
			this.day = day;
			this.hour = hour;
			this.minute = minute;
			this.second = second;
		}
		public void setOutdateTime(LocalDateTime time) {
			setOutdateTime(time.getYear(), time.getMonthValue(), time.getDayOfMonth(),
					time.getHour(), time.getMinute(), time.getSecond());
		}

		public boolean isOutdate() {
			LocalDateTime time = getOutdateTime();
			return this.owner.length() > 0 && year > 114 && (time != null && LocalDateTime.now().isAfter(time));
		}

		public Location getSignLoc() {
			return signLoc;
		}

		public void setSignLoc(Location signLoc) {
			this.signLoc = signLoc;
		}

		public int getYear() {
			return year;
		}

		public int getMonth() {
			return month;
		}

		public int getDay() {
			return day;
		}

		public int getHour() {
			return hour;
		}

		public int getMinute() {
			return minute;
		}

		public int getSecond() {
			return second;
		}

		public void removeOutdateTime() {
			this.year = this.month = this.day = this.hour = this.minute = this.second = 0;
		}
	}

	public void reloadConfig() {
		try {
			if (!configFile.exists()) {
				this.plugin.saveResource("market.yml", true);
			}
			config = YamlConfiguration.loadConfiguration(configFile);
			data = dataFile.exists() ? YamlConfiguration.loadConfiguration(dataFile) : new YamlConfiguration();
			this.markets.clear();
			this.marketsData.clear();
			for (String id : config.getKeys(false)) {
				String res = config.getString(id);
				if (res != null) {
					this.markets.put(id, res);
				}
			}
			for (String id : data.getKeys(false)) {
				String owner = data.getString(id + ".owner");
				String world = data.getString(id + ".sign.world", "");
				double x = data.getInt(id + ".sign.x", 0);
				double y = data.getInt(id + ".sign.y", 0);
				double z = data.getInt(id + ".sign.z", 0);
				int year = data.getInt(id + ".outdate.year", 1970);
				int month = data.getInt(id + ".outdate.month", 1);
				int day = data.getInt(id + ".outdate.day", 1);
				int hour = data.getInt(id + ".outdate.hour", 8);
				int minute = data.getInt(id + ".outdate.minute", 0);
				int second = data.getInt(id + ".outdate.second", 0);
				Location loc = SpaceUtil.getLocation(world, x, y, z);
				this.marketsData.put(id, new MarketData(id, owner, year, month, day, hour, minute, second, loc));
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void saveConfig() {
		try {
			this.data = new YamlConfiguration();
			for (MarketData marketData : this.marketsData.values()) {
				String id = marketData.getId();
				this.data.set(id + ".owner", marketData.getOwner());
				if(marketData.getYear() > 114) {
				this.data.set(id + ".outdate.year", marketData.getYear());
				this.data.set(id + ".outdate.month", marketData.getMonth());
				this.data.set(id + ".outdate.day", marketData.getDay());
				this.data.set(id + ".outdate.hour", marketData.getHour());
				this.data.set(id + ".outdate.minute", marketData.getMinute());
				this.data.set(id + ".outdate.second", marketData.getSecond());
				}
				Location loc = marketData.getSignLoc();
				if (loc != null) {
					this.data.set(id + ".sign.world", loc.getWorld().getName());
					this.data.set(id + ".sign.x", loc.getBlockX());
					this.data.set(id + ".sign.y", loc.getBlockY());
					this.data.set(id + ".sign.z", loc.getBlockZ());
				}
			}
			this.data.save(this.dataFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<String> all() {
		return Lists.newArrayList(this.markets.keySet());
	}

	public List<String> allResidences() {
		return Lists.newArrayList(this.markets.values());
	}

	public List<MarketData> allData() {
		return Lists.newArrayList(this.marketsData.values());
	}
}
