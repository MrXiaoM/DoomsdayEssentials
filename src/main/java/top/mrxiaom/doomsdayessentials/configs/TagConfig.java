package top.mrxiaom.doomsdayessentials.configs;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagConfig {
	private final Main plugin;
	private final File configFile;
	private FileConfiguration config;
	private final File dataFile;
	private FileConfiguration data;
	private String cmd;
	private double cost;
	private String allTagGuiTitle;
	private String tagGuiTitle;
	private boolean isUseCommand;
	private int defaultTitleId = 1001;
	private Map<Integer, String> titleMap;

	public TagConfig(Main plugin) {
		this.plugin = plugin;
		dataFile = new File(plugin.getDataFolder(), "tagsData.yml");
		this.configFile = new File(this.plugin.getDataFolder(), "tags.yml");
	}

	public TagConfig reloadConfig() {
		if (!configFile.exists()) {
			this.plugin.saveResource("tags.yml", true);
		}
		config = YamlConfiguration.loadConfiguration(configFile);
		if (config == null) {
			plugin.getLogger().info(I18n.t("title.LoadFail"));
			
		}
		else{
			tagGuiTitle = ChatColor.translateAlternateColorCodes('&', config.getString("title", "&b称号列表"));
			allTagGuiTitle = ChatColor.translateAlternateColorCodes('&', config.getString("listtitle", "&a所有称号展示"));
			defaultTitleId = config.getInt("defaultTitleId");
			isUseCommand = config.getBoolean("usecommand");
			cmd = ChatColor.translateAlternateColorCodes('&', config.getString("cmd"));
			cost = config.getDouble("cost");
		}
		titleMap = new HashMap<>();
		List<String> tagKeys = Lists.newArrayList(config.getConfigurationSection("titles").getKeys(false));
		Collections.sort(tagKeys);
		for (String id : tagKeys) {
			try {
				Integer intId = Util.strToInt(id, -1);
				if(intId < 0) continue;
				String tag = config.getString("titles." + id);
				titleMap.put(intId, tag);
			} catch (Throwable t) {
				plugin.getLogger().warning("载入称号(id=" + id + ")时出现异常: " + t.getLocalizedMessage());
				continue;
			}
		}
		try {
			if (!dataFile.exists())
				dataFile.createNewFile();
			this.data = YamlConfiguration.loadConfiguration(dataFile);
		} catch (Throwable t) {
			t.printStackTrace();
			this.data = new YamlConfiguration();
		}
		return this;
	}

	public TagConfig saveConfig() {
		try {
			this.data.save(dataFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public String getGuiTagListAllTitle() {
		return this.allTagGuiTitle;
	}

	public String getGuiTagListPlayerTitle() {
		return this.tagGuiTitle;
	}

	public Map<Integer, String> getTagMap() {
		return this.titleMap;
	}

	public FileConfiguration getConfig() {
		return this.config;
	}

	public boolean isTagExist(int id) {
		return this.titleMap.keySet().contains(id);
	}

	public Map<Integer, String> getPlayerTags(OfflinePlayer player) {
		return this.getPlayerTags(player.getName());
	}
	
	public Map<Integer, String> getPlayerTags(Player player) {
		return this.getPlayerTags(player.getName());
	}

	public Map<Integer, String> getPlayerTags(String player) {
		Map<Integer, String> listTitle = new HashMap<Integer, String>();
		for (Integer titleId : titleMap.keySet()) {
			if (hasTag(player, titleId)) {
				listTitle.put(titleId, titleMap.get(titleId));
			}
		}
		return listTitle;
	}

	public String getTagFromID(int id) {
		if (titleMap.keySet().contains(id)) {
			return titleMap.get(id);
		}
		return "";
	}

	public List<String> getTagDisplayLore(int id) {
		List<String> L = config.getStringList("lore." + id);
		for (int i = 0; i < L.size(); i++) {
			L.set(i, L.get(i).replace("&", "§"));
		}
		return L;
	}

	public Material getTagDisplayItemMaterial(int id) {
		return Util.valueOf(Material.class, config.getString("itemid." + id + ".Material", "NAME_TAG"), Material.NAME_TAG);
	}

	public static String packId(int i) {
		StringBuilder result = new StringBuilder();
		for (char c2 : String.valueOf(i).toCharArray()) {
			result.append("§").append(c2);
		}
		return result.toString();
	}

	public static int extractId(String s) {
		int resetIndex = s.indexOf("§r");
		if (resetIndex <= 0) {
			return -1;
		}
		String codes = s.substring(0, resetIndex);
		return Util.strToInt(codes.replace("§", ""), -1);
	}
	
	public Main getPlugin() {
		return plugin;
	}
	
	public double getCost() {
		return cost;
	}

	public boolean hasTag(Player player, int id) {
		return plugin.getPermsApi().has(player, "udtitle.t." + String.valueOf(id));
	}

	public boolean hasTag(OfflinePlayer player, int id) {
		return plugin.getPermsApi().playerHas("", player, "udtitle.t." + String.valueOf(id));
	}
	
	@SuppressWarnings("deprecation")
	public boolean hasTag(String player, int id) {
		return plugin.getPermsApi().playerHas("", player, "udtitle.t." + String.valueOf(id));
	}

	public String getPlayerTag(String player) {
		if (!this.data.contains(player)) {
			return setDefaultTag(player);
		}
		return this.data.getString(player);
	}

	public void setPlayerTag(Player player, String prefix) {
		setPlayerTag(player.getName(), prefix);
	}

	public boolean setPlayerTag(String player, String prefix) {
		this.data.set(player, prefix);
		this.saveConfig();
		if (isUseCommand) {
			return Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
					cmd.replace("%player%", player).replace("%prefix%", prefix).replace("§", "&"));
		}
		return true;
	}

	public boolean setPlayerTag(Player player, int id) {
		return setPlayerTag(player.getName(), id);
	}

	public boolean setPlayerTagUseMoney(Player player, int id) {
		if (plugin.getEcoApi().getBalance(player) < cost || !takeMoney(player, cost)) {
			return false;
		}
		if (setPlayerTag(player, id)) {
			return true;
		}
		plugin.getEcoApi().depositPlayer(player, cost);
		player.sendMessage(I18n.t("title.NotEnoughMoney"));
		return false;
	}

	public boolean takeMoney(Player player, double db) {
		return plugin.getEcoApi().withdrawPlayer(player, db).transactionSuccess();
	}

	public boolean setPlayerTag(String player, int id) {
		for (Integer titleId : titleMap.keySet()) {
			if (titleId == id) {
				return setPlayerTag(player, titleMap.get(titleId));
			}
		}
		return false;
	}

	public String setDefaultTag(Player player) {
		return this.setDefaultTag(player.getName());
	}

	public String setDefaultTag(String player) {
		String defaultTag = "&7[&a玩家&7]&e";
		if (this.titleMap.keySet().contains(this.defaultTitleId)) {
			defaultTag = this.titleMap.get(this.defaultTitleId);
		}
		this.setPlayerTag(player, defaultTag);
		return defaultTag;
	}
}
