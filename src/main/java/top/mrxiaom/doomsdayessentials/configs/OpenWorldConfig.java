package top.mrxiaom.doomsdayessentials.configs;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenWorldConfig {
	final File configFile;
	final Main plugin;

	public static class OngoingTask {

		// TODO 这里好像会产生很大的性能浪费的说… 未来的我请帮我解决下

		public final String name;
		public String stringValue1 = "";
		public String stringValue2 = "";
		public Integer intValue1 = null;
		public Integer intValue2 = null;
		public Float floatValue1 = null;
		public Float floatValue2 = null;
		public Boolean booleanValue1 = null;
		public Boolean booleanValue2 = null;

		private OngoingTask(String name) {
			this.name = name;
		}

		public static OngoingTask transStringToOngoingTask(String string) {
			if (string.contains(";")) {
				List<String> result = new ArrayList<String>();
				for (String s : string.split(";")) {
					result.add(s);
				}
				return transStringListToOngoingTask(result);
			} else {
				return new OngoingTask(string);
			}
		}

		public static OngoingTask transStringListToOngoingTask(List<String> list) {
			OngoingTask task = new OngoingTask(list.get(0));
			for (int i = 1; i < list.size(); i++) {
				try {
					if (i == 1) {
						task.stringValue1 = list.get(1);
					}
					if (i == 2) {
						task.stringValue2 = list.get(2);
					}
					if (i == 3) {
						task.intValue1 = Integer.valueOf(list.get(3));
					}
					if (i == 4) {
						task.intValue2 = Integer.valueOf(list.get(4));
					}
					if (i == 5) {
						task.floatValue1 = Float.valueOf(list.get(5));
					}
					if (i == 6) {
						task.floatValue2 = Float.valueOf(list.get(6));
					}
					if (i == 7) {
						String s = list.get(7);
						task.booleanValue1 = s.equalsIgnoreCase("true") ? true
								: s.equalsIgnoreCase("false") ? false : null;
					}
					if (i == 8) {
						String s = list.get(8);
						task.booleanValue2 = s.equalsIgnoreCase("true") ? true
								: s.equalsIgnoreCase("false") ? false : null;
					}
				} catch (Throwable t) {
					continue;
				}
			}
			return task;
		}

		public String toString() {
			String string = name + ";" + stringValue1 + ";" + stringValue2 + ";";
			string += (intValue1 != null ? intValue1 : "") + ";" + (intValue2 != null ? intValue2 : "") + ";";
			string += (floatValue1 != null ? floatValue1 : "") + ";" + (floatValue2 != null ? floatValue2 : "") + ";";
			string += (booleanValue1 != null ? booleanValue1 : "") + ";" + (booleanValue2 != null ? booleanValue2 : "")
					+ ";";
			return string;
		}
	}

	public static class OpenWorldPlayer {
		private final String name;
		private String itemsLast;
		private String itemsOpenWorldLast;
		private List<String> tasks;
		private int level;

		public OpenWorldPlayer(String name, String itemsLast, String itemsOpenWorldLast, List<String> tasks,
				int level) {
			this.name = name;
			this.itemsLast = itemsLast;
			this.itemsOpenWorldLast = itemsOpenWorldLast;
			this.tasks = tasks;
			this.level = level;
		}

		public void setTasks(List<String> tasks) {
			this.tasks = tasks;
		}

		public void addTask(String task) {
			this.tasks.add(task);
		}

		public void removeTask(String task) {
			for (String t : tasks) {
				if (t.toLowerCase().startsWith(task.toLowerCase())) {
					tasks.remove(t);
				}
			}
		}

		public List<String> getTaskStatus(String task) {
			return this.getTaskStatus(task, false);
		}

		public List<String> getTaskStatus(String task, boolean noResult) {
			for (String t : tasks) {
				if (t.toLowerCase().startsWith(task.toLowerCase())) {
					List<String> result = new ArrayList<String>();
					if (noResult)
						return result;
					if (t.contains(";")) {
						for (String s : t.split(";")) {
							result.add(s);
						}
					} else {
						result.add(t);
					}
					return result;
				}
			}
			return null;
		}

		public void setTaskStatus(List<String> task) {
			if (task.size() >= 1) {
				String t = "";
				for (int i = 0; i < task.size(); i++) {
					t += task.get(i) + (i + 1 < task.size() ? ";" : "");
				}
				this.removeTask(task.get(0));
				this.addTask(t);
			}
		}

		public List<String> getTasks() {
			return this.tasks;
		}

		public String getName() {
			return this.name;
		}

		public void setLevel(int level) {
			this.level = level;
		}

		public int getLevel() {
			return this.level;
		}

		public ItemStack[] getItemsLast() {
			return ItemStackUtil.itemStackArrayFromBase64(itemsLast, true);
		}

		public ItemStack[] getItemsOpenWorldLast() {
			return ItemStackUtil.itemStackArrayFromBase64(itemsOpenWorldLast, true);
		}

		public String getItemsLastString() {
			return this.itemsLast;
		}

		public void setItemsLastString(String itemsLast) {
			this.itemsLast = itemsLast;
		}

		public void setItemsLast(ItemStack[] itemList) {
			this.itemsLast = ItemStackUtil.itemStackArrayToBase64(itemList);
		}

		public String getItemsOpenWorldLastString() {
			return this.itemsOpenWorldLast;
		}

		public void setItemsOpenWorldLastString(String itemsOpenWorldLast) {
			this.itemsOpenWorldLast = itemsOpenWorldLast;
		}

		public void setItemsOpenWorldLast(ItemStack[] itemList) {
			this.itemsOpenWorldLast = ItemStackUtil.itemStackArrayToBase64(itemList);
		}

	}

	private Map<String, OpenWorldPlayer> playersMap = new HashMap<String, OpenWorldPlayer>();

	public OpenWorldConfig(Main plugin) {
		this.plugin = plugin;
		this.configFile = new File(plugin.getDataFolder().getAbsolutePath() + "\\openworld\\players\\");
		this.reloadConfig();
	}

	public boolean contains(String player) {
		return this.playersMap.containsKey(player);
	}

	public Map<String, OpenWorldPlayer> getAllKits() {
		return playersMap;
	}

	public OpenWorldPlayer get(Player player, boolean isVanillaInv) {
		if (!this.contains(player.getName())) {
			String s = ItemStackUtil.itemStackArrayToBase64(player.getInventory().getContents(), true);
			OpenWorldPlayer owp = new OpenWorldPlayer(player.getName(), isVanillaInv ? s : "", isVanillaInv ? "" : s,
					new ArrayList<String>(), 0);
			this.set(player.getName(), owp);
		}
		return this.playersMap.get(player.getName());
	}

	public OpenWorldPlayer get(String player) {
		if (!this.contains(player)) {
			return null;
		}
		return this.playersMap.get(player);
	}

	public void set(String playerName, OpenWorldPlayer kit) {
		this.playersMap.put(playerName, kit);
		this.saveConfig();
	}

	public void reloadConfig() {
		if (!configFile.exists()) {
			this.saveConfig();
		}

		this.playersMap = new HashMap<String, OpenWorldPlayer>();
		this.playersMap.clear();
		if (configFile.exists()) {
			for (File file : configFile.listFiles()) {
				try {
					YamlConfiguration kitConfig = YamlConfiguration.loadConfiguration(file);

					String name = kitConfig.getString("name");
					int level = kitConfig.getInt("level");
					List<String> tasks = kitConfig.getStringList("tasks");
					String itemsLast = kitConfig.getString("items-last");
					String itemsOpenWorldLast = kitConfig.getString("items-openworld-last");
					this.playersMap.put(file.getName().substring(0, file.getName().length() - 4),
							new OpenWorldPlayer(name, itemsLast, itemsOpenWorldLast, tasks, level));
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
			for (String key : playersMap.keySet()) {
				try {
					OpenWorldPlayer playerData = playersMap.get(key);
					YamlConfiguration playerConfig = new YamlConfiguration();
					playerConfig.set("name", playerData.getName());
					playerConfig.set("level", playerData.getLevel());
					playerConfig.set("tasks", playerData.getTasks());
					playerConfig.set("items-last", playerData.getItemsLastString());
					playerConfig.set("items-openworld-last", playerData.getItemsOpenWorldLastString());
					playerConfig.save(new File(configFile, key + ".yml"));
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
