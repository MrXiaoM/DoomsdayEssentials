package top.mrxiaom.doomsdayessentials.configs;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
				List<String> result = new ArrayList<>(Arrays.asList(string.split(";")));
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
						task.booleanValue1 = s.equalsIgnoreCase("true");
					}
					if (i == 8) {
						String s = list.get(8);
						task.booleanValue2 = s.equalsIgnoreCase("true");
					}
				} catch (Throwable t) {
					// 收声
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
		private List<String> tasks;
		private int level;

		public OpenWorldPlayer(String name, List<String> tasks,
				int level) {
			this.name = name;
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
			tasks.removeIf(t -> t.toLowerCase().startsWith(task.toLowerCase()));
		}

		public List<String> getTaskStatus(String task) {
			return this.getTaskStatus(task, false);
		}

		public List<String> getTaskStatus(String task, boolean noResult) {
			for (String t : tasks) {
				if (t.toLowerCase().startsWith(task.toLowerCase())) {
					List<String> result = new ArrayList<>();
					if (noResult)
						return result;
					if (t.contains(";")) {
						result.addAll(Arrays.asList(t.split(";")));
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
				StringBuilder t = new StringBuilder();
				for (int i = 0; i < task.size(); i++) {
					t.append(task.get(i)).append(i + 1 < task.size() ? ";" : "");
				}
				this.removeTask(task.get(0));
				this.addTask(t.toString());
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

	}

	private Map<String, OpenWorldPlayer> playersMap = new HashMap<>();

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

	public OpenWorldPlayer get(Player player) {
		if (!this.contains(player.getName())) {
			String s = ItemStackUtil.itemStackArrayToBase64(player.getInventory().getContents(), true);
			OpenWorldPlayer owp = new OpenWorldPlayer(player.getName(), new ArrayList<>(), 0);
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

		this.playersMap = new HashMap<>();
		if (configFile.exists()) {
			File[] files = configFile.listFiles();
			if(files == null) return;
			for (File file : files) {
				try {
					YamlConfiguration kitConfig = YamlConfiguration.loadConfiguration(file);

					String name = kitConfig.getString("name");
					int level = kitConfig.getInt("level");
					List<String> tasks = kitConfig.getStringList("tasks");
					this.playersMap.put(file.getName().substring(0, file.getName().length() - 4),
							new OpenWorldPlayer(name, tasks, level));
				} catch (Throwable t) {
					// 收声
				}
			}
		}
	}

	public void saveConfig() {
		try {
			if (!configFile.exists()) {
				configFile.mkdirs();
			}
			List<String> files = new ArrayList<>();
			for (String key : playersMap.keySet()) {
				try {
					OpenWorldPlayer playerData = playersMap.get(key);
					YamlConfiguration playerConfig = new YamlConfiguration();
					playerConfig.set("name", playerData.getName());
					playerConfig.set("level", playerData.getLevel());
					playerConfig.set("tasks", playerData.getTasks());
					playerConfig.save(new File(configFile, key + ".yml"));
					files.add(key + ".yml");
				} catch (Throwable t) {
					// 收声
				}
			}
			File[] filelist = configFile.listFiles();
			if(filelist == null) return;
			for (File file : filelist) {
				if (!files.contains(file.getName())) {
					file.delete();
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
