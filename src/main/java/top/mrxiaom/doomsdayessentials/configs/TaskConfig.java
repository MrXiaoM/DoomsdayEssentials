package top.mrxiaom.doomsdayessentials.configs;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.doomsdayessentials.Main;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskConfig {
	final File configFile;
	FileConfiguration config;
	final Main plugin;

	public static class Task {
		private boolean everyday;
		private String id;
		private String name;
		private List<String> description;

		private List<String> requests;
		private List<String> commands;

		public Task(boolean everyday, String name, List<String> description, List<String> requests,
                    List<String> commands) {
			this.everyday = everyday;
			this.name = name;
			this.description = description;
			this.requests = requests;
			this.commands = commands;
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

		public List<String> getDescription() {
			return description;
		}

		public void setDescription(List<String> description) {
			this.description = description;
		}

		public List<String> getRequests() {
			return requests;
		}

		public void setRequests(List<String> requests) {
			this.requests = requests;
		}

		public boolean isNameEqualIgnoreCase(String name) {
			return this.name.equalsIgnoreCase(name);
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}
	}

	private Map<String, Task> tasksMap = new HashMap<String, Task>();

	public TaskConfig(Main plugin) {
		this.plugin = plugin;
		this.configFile = new File(plugin.getDataFolder().getAbsolutePath() + "\\tasks.yml");
		this.reloadConfig();
	}

	public boolean contains(String warpName) {
		return this.tasksMap.containsKey(warpName);
	}

	public Map<String, Task> getAllTasks() {
		return tasksMap;
	}

	public Map<String, Task> getMainTasks() {
		Map<String, Task> result = new HashMap<String, Task>();
		for (String key : tasksMap.keySet()) {
			Task task = tasksMap.get(key);
			if (!task.isEveryday()) {
				result.put(key, task);
			}
		}
		return result;
	}

	public Map<String, Task> getDailyTasks() {
		Map<String, Task> result = new HashMap<String, Task>();
		for (String key : tasksMap.keySet()) {
			Task task = tasksMap.get(key);
			if (task.isEveryday()) {
				result.put(key, task);
			}
		}
		return result;
	}

	@Nullable
	public Task get(String taskName) {
		if (this.contains(taskName)) {
			return this.tasksMap.get(taskName);
		}
		return null;
	}

	public void set(String taskName, Task task) {
		this.tasksMap.put(taskName, task);
		this.saveConfig();
	}

	public void remove(String taskName) {
		if (this.contains(taskName)) {
			this.tasksMap.remove(taskName);
			this.saveConfig();
		}
	}

	public void reloadConfig() {
		if (!configFile.exists()) {
			this.saveConfig();
		}
		config = YamlConfiguration.loadConfiguration(configFile);
		this.tasksMap = new HashMap<String, Task>();
		this.tasksMap.clear();
		for (String key : config.getConfigurationSection("tasks").getKeys(false)) {
			try {
				ConfigurationSection taskConfig = config.getConfigurationSection("tasks," + key);
				String name = taskConfig.getString("name");
				boolean everyday = taskConfig.getBoolean("everyday");
				List<String> description = taskConfig.getStringList("description");
				List<String> requests = taskConfig.getStringList("requests");
				List<String> commands = taskConfig.getStringList("commands");
				this.tasksMap.put(key, new Task(everyday, name, description, requests, commands));
			} catch (IllegalArgumentException e) {
				continue;
			}
		}

	}

	public void saveConfig() {
		try {
			if (!configFile.exists()) {
				configFile.createNewFile();
			}

			FileConfiguration oldConfig = config;
			config = new YamlConfiguration();
			for (String key : oldConfig.getKeys(false)) {
				// 删除掉 tasks
				if (!key.equalsIgnoreCase("tasks")) {
					config.set(key, oldConfig.get(key));
				}
			}
			// 写入数据到配置文件
			for (String key : tasksMap.keySet()) {
				try {
					Task kit = tasksMap.get(key);
					config.set("tasks." + key + ".name", kit.getName());
					config.set("tasks." + key + ".everyday", kit.isEveryday());
					config.set("tasks." + key + ".description", kit.getDescription());
					config.set("tasks." + key + ".requests", kit.getRequests());
					config.set("tasks." + key + ".commands", kit.getCommands());
				} catch (IllegalArgumentException e) {
					continue;
				}
			}
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
