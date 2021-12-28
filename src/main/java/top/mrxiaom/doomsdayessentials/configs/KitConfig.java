package top.mrxiaom.doomsdayessentials.configs;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitConfig {
	final File configFile;
	final Main plugin;

	public static class Kit {
		private boolean everyday;
		private String id;
		private String name;
		private String items;
		private List<String> commands;
		private int maxTime;

		public Kit(String id, String name, String items, boolean everyday, List<String> commands, int maxTime) {
			this.id = id;
			this.name = name;
			this.items = items;
			this.everyday = everyday;
			this.commands = commands;
			this.maxTime = maxTime;
		}

		public void setCommands(List<String> commands) {
			this.commands = commands;
		}

		public void addCommand(String command) {
			this.commands.add(command);
		}

		public void removeCommand(int index) {
			this.commands.remove(index);
		}

		public List<String> getCommands() {
			return this.commands;
		}

		public void setIsEveryday(boolean everyday) {
			this.everyday = everyday;
		}

		public boolean isEveryday() {
			return this.everyday;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		public void setMaxTime(int maxTime) {
			this.maxTime = maxTime;
		}

		public int getMaxTime() {
			return this.maxTime;
		}

		public boolean isNameEqualIgnoreCase(String name) {
			return this.name.equalsIgnoreCase(name);
		}

		public ItemStack[] getItems() {
			return ItemStackUtil.itemStackArrayFromBase64(items);
		}

		public String getItemsString() {
			return this.items;
		}

		public void setItemsString(String items) {
			this.items = items;
		}

		public void setItems(ItemStack[] itemList) {
			this.items = ItemStackUtil.itemStackArrayToBase64(itemList);
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

	}

	private Map<String, Kit> kitsMap = new HashMap<String, Kit>();

	public KitConfig(Main plugin) {
		this.plugin = plugin;
		this.configFile = new File(plugin.getDataFolder().getAbsolutePath() + "\\kits\\");
		this.reloadConfig();
	}

	public boolean contains(String warpName) {
		return this.kitsMap.containsKey(warpName);
	}

	public Map<String, Kit> getAllKits() {
		return kitsMap;
	}

	@Nullable
	public Kit get(String kitName) {
		if (this.contains(kitName)) {
			return this.kitsMap.get(kitName);
		}
		return null;
	}

	public void set(Kit kit) {
		this.kitsMap.put(kit.getId(), kit);
		this.saveConfig();
	}

	public void remove(String kitName) {
		if (this.contains(kitName)) {
			this.kitsMap.remove(kitName);
			this.saveConfig();
		}
	}

	public void reloadConfig() {
		if (!configFile.exists()) {
			this.saveConfig();
		}

		this.kitsMap = new HashMap<String, Kit>();
		this.kitsMap.clear();
		if (configFile.exists()) {
			for (File file : configFile.listFiles()) {
				try {
					YamlConfiguration kitConfig = YamlConfiguration.loadConfiguration(file);
					String id = file.getName().substring(0, file.getName().length() - 4);
					String name = kitConfig.getString("name");
					boolean everyday = kitConfig.getBoolean("everyday");
					int maxTime = kitConfig.getInt("maxTime");
					List<String> commands = kitConfig.getStringList("commands");
					String items = kitConfig.getString("items");
					this.kitsMap.put(id,
							new Kit(id, name, items, everyday, commands, maxTime));
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
			for (String key : kitsMap.keySet()) {
				try {
					Kit kit = kitsMap.get(key);
					YamlConfiguration kitConfig = new YamlConfiguration();
					kitConfig.set("name", kit.getName());
					kitConfig.set("everyday", kit.isEveryday());
					kitConfig.set("maxTime", kit.getMaxTime());
					kitConfig.set("commands", kit.getCommands());
					kitConfig.set("items", kit.getItemsString());
					kitConfig.save(new File(configFile, key + ".yml"));
					files.add(key + ".yml");
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
