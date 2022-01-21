package top.mrxiaom.doomsdayessentials.configs;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

public class BindConfig {
	final Map<String, FileConfiguration> config = new HashMap<>();
	final File configFile;
	final Main plugin;
	public BindConfig(Main plugin) {
		this.plugin = plugin;
		configFile = new File(plugin.getDataFolder().getAbsolutePath() + "\\binds");
		this.reloadConfig();
	}
	
	public static final String PREFIX = "§b§i§n§d§0"; 
	
	public String genRandomID() {
		return genRandomID(null);
	}
	
	public String genRandomID(FileConfiguration playerCfg) {
		String num = UUID.randomUUID().toString().substring(0, 6);
		if(playerCfg != null) {
			while(playerCfg.contains(num)) {
				num = UUID.randomUUID().toString().substring(0, 6);
			}
		}
		return num;
	}
	
	public boolean needToRecall(ItemStack item) {
		if(item == null) return false;
		String code = plugin.getBindConfig().getCodeFromItemStack(item);
		return plugin.getBindConfig().needToRecall(code);
	}
	
	public boolean needToRecall(String code) {
		if(code == null) return false;
		String num = code.substring(0, 6);
		String player = code.substring(6);
		return !this.config.containsKey(player) || !this.config.get(player).contains(num);
	}
	
	public String getOwner(String code) {
		return code.substring(6);
	}
	
	public Map<String, ItemStack> getPlayerBindList(String player) {
		return getPlayerBindList(player, false);
	}
	
	public Map<String, ItemStack> getPlayerBindList(String player, boolean withGuiLore){
		Map<String, ItemStack> map = new HashMap<>();
		if(this.config.containsKey(player)) {
			for(String num : this.config.get(player).getKeys(false)) {
				if(num.length() != 6) continue;
				ItemStack item = getItemStackFromCode(num + player);
				if(item == null) continue;
				if(withGuiLore) {
					List<String> lore = ItemStackUtil.getItemLore(item);
					lore.add("§0" + num + player);
					lore.add("                   §b§o点击召回");
					ItemStackUtil.setItemLore(item, lore);
				}
				map.put(num, item);
			}
		}
		return map;
	}
	
	@Nullable
	public String getCodeFromItemStack(ItemStack item) {
		List<String> lore =  ItemStackUtil.getItemLore(item);
		if(lore.size() - 2 < 0) return null;
		String s =  lore.get(lore.size() - 2);
		if(!s.startsWith(PREFIX)) return null;
		return s.substring(PREFIX.length());
	}
	
	@Nullable
	public ItemStack getItemStackFromCode(String code) {
		String num = code.substring(0, 6);
		String player = code.substring(6);
		if(!this.config.containsKey(player)) return null;
		String itemStr = this.config.get(player).getString(num + ".save-item", "");
		if(itemStr != null && itemStr.length() > 0) {
			ItemStack[] items = ItemStackUtil.itemStackArrayFromBase64(itemStr);
			if(items.length > 0) {
				return items[0];
			}
		}
		return null;
	}

	public String putBind(String player, ItemStack item) {
		String num = genRandomID(this.config.getOrDefault(player, null));
		String base64 = ItemStackUtil.itemStackArrayToBase64(new ItemStack[] { item });
		LocalDateTime now = LocalDateTime.now();
		this.set(player, num + ".save-item", base64)
			.set(player, num + ".time.year", now.getYear())
			.set(player, num + ".time.month", now.getMonthValue())
			.set(player, num + ".time.day", now.getDayOfMonth())
			.set(player, num + ".time.hour", now.getHour())
			.set(player, num + ".time.minute", now.getMinute())
			.set(player, num + ".time.second", now.getSecond())
			.saveConfig();
		return num + player;
	}
	
	public BindConfig removeBind(String code) {
		String num = code.substring(0, 6);
		String player = code.substring(6);
		return remove(player, num);
	}
	
	public BindConfig remove(String player, String num) {
		if(this.config.containsKey(player)) {
			YamlConfiguration newCfg = new YamlConfiguration();
			for(String key : this.config.get(player).getKeys(false)) {
				if(!key.equalsIgnoreCase(num)) {
					newCfg.set(key, this.config.get(player).get(key));
				}
			}
			this.config.remove(player);
			this.config.put(player, newCfg);
		}
		return this;
	}
	
	public int getPlayerBindCount(String player) {
		if(this.config.containsKey(player)) {
			return this.config.get(player).getKeys(false).size();
		}
		return 0;
	}
	
	public Object get(String player, String key, Object nullValue) {
		return this.getConfig(player).get(key, nullValue);
	}
	
	public FileConfiguration getConfig(String player) {
		if(this.config.containsKey(player)) {
			return this.config.get(player);
		}
		return new YamlConfiguration();
	}

	public BindConfig set(String player, String key, Object value) {
		FileConfiguration cfg = getConfig(player);
		cfg.set(key, value);
		return set(player, cfg);
	}
	
	public BindConfig set(String player, FileConfiguration value) {
		this.config.remove(player);
		this.config.put(player, value);
		return this;
	}
	
	public BindConfig reloadConfig() {
		if (!configFile.exists()) {
			configFile.mkdirs();
		}
		this.config.clear();
		File[] files = configFile.listFiles();
		if(files == null) return this;
		for(File file : files) {
			try {
				String name = file.getName().replace(".yml", "");
				this.config.put(name, YamlConfiguration.loadConfiguration(file));
			}catch(Throwable t) {
				t.printStackTrace();
			}
		}
		return this;
	}

	public BindConfig saveConfig() {
		try {
			List<String> files = new ArrayList<>();
			for(String key : this.config.keySet()) {
				try {
					this.config.get(key).save(new File(this.configFile, key + ".yml"));
					files.add(key + ".yml");
				}catch(Throwable t) {
					t.printStackTrace();
				}
			}
			File[] filelist = configFile.listFiles();
			if(filelist == null) return this;
			for (File file : filelist) {
				if (!files.contains(file.getName())) {
					file.delete();
				}
			}
		}catch(Throwable t) {
			t.printStackTrace();
		}
		return this;
	}
}
