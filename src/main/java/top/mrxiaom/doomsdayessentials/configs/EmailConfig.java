package top.mrxiaom.doomsdayessentials.configs;

import com.google.common.collect.Lists;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.doomsdayessentials.Main;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

public class EmailConfig {
	final File configPath;
	Map<String, YamlConfiguration> playerDatas;
	final Main plugin;
	public class Email{
		String sender;
		String receiver;
		List<String> content;
		ItemStack item;
		public Email(String sender, String receiver, List<String> content, ItemStack item) {
			this.sender = sender;
			this.receiver = receiver;
			this.content = content;
			this.item = item;
		}
		public String getSender() {
			return sender;
		}
		public String getReceiver() {
			return receiver;
		}
		public List<String> getContent() {
			return content;
		}
		public ItemStack getItem() {
			return item;
		}
		public void setSender(String sender) {
			this.sender = sender;
		}
		public void setReceiver(String receiver) {
			this.receiver = receiver;
		}
		public void setContent(List<String> content) {
			this.content = content;
		}
		public void setItem(ItemStack item) {
			this.item = item;
		}
		// TODO
		public ItemStack genIcon() {
			return null;
		}
		public ItemStack genBook() {
			return null;
		}
	}
	public EmailConfig(Main plugin) {
		this.plugin = plugin;
		this.configPath = new File(plugin.getDataFolder(), "email");
		this.reloadConfig();
	}

	public boolean isNeedToNotice(String player) {
		if(!playerDatas.containsKey(player)) return false;
		YamlConfiguration config = playerDatas.get(player);
		if(config.contains("unread")) {
			return config.getStringList("unread").size() > 0;
			
		}
		return false;
	}
	
	public YamlConfiguration getPlayerConfig(String player) {
		if (!playerDatas.keySet().contains(player)) {
			return new YamlConfiguration();
		}
		return playerDatas.get(player);
	}

	public int getEmailCount(String player) {
		if (!playerDatas.keySet().contains(player) || !playerDatas.get(player).contains("email")) {
			return 0;
		}
		return playerDatas.get(player).getConfigurationSection("email").getKeys(false).size();
	}
	public int getUnreadEmailCount(String player) {
		if (!playerDatas.keySet().contains(player) || !playerDatas.get(player).contains("unread")) {
			return 0;
		}
		return playerDatas.get(player).getStringList("unread").size();
	}

	public boolean hasEmail(String player, String id) {
		if (!playerDatas.keySet().contains(player) || !playerDatas.get(player).contains("email")) {
			return false;
		}
		return playerDatas.get(player).contains(id);
	}
	public EmailConfig sendEmail(String sender, String receiver, String... content) {
		return sendEmail(sender, receiver, Lists.newArrayList(content));
	}
	public EmailConfig sendEmail(String sender, String receiver, List<String> content) {
		return sendEmail(sender, receiver, (ItemStack) null, content);
	}
	public EmailConfig sendEmail(String sender, String receiver, ItemStack item, String... content) {
		return sendEmail(sender, receiver, item, Lists.newArrayList(content));
	}
	public EmailConfig sendEmail(String sender, String receiver, ItemStack item, List<String> content) {
		return sendEmail(sender, receiver, item, content);
	}

	public EmailConfig sendEmail(String id, String sender, String receiver, List<String> content) {
		return sendEmail(id, sender, receiver, (ItemStack) null, content);
	}
	public EmailConfig sendEmail(String id, String sender, String receiver, String... content) {
		return sendEmail(id, sender, receiver, (ItemStack) null, Lists.newArrayList(content));
	}
	public EmailConfig sendEmail(String id, String sender, String receiver, ItemStack item, List<String> content) {
		Email email = new Email(sender, receiver, content, item);
		// TODO 保存到双方的配置文件中
		return this;
	}
	
	public String genRandomID(String sender, String receiver) {
		String random = UUID.randomUUID().toString().substring(0, 8);
		if((!playerDatas.containsKey(sender) || !playerDatas.get(sender).contains("email." + random))
				&& (!playerDatas.containsKey(receiver) || !playerDatas.get(receiver).contains("email." + random))) {
			return random;
		}
		return genRandomID(sender, receiver);
	}
	
	@Nullable
	public Email getEmail(String player, String id) {
		if (!hasEmail(player, id))
			return null;

		YamlConfiguration config = playerDatas.get(player);
		ConfigurationSection email = config.getConfigurationSection("email." + id);

		String sender = email.getString("sender");
		String receiver = email.getString("receiver");
		List<String> content = email.getStringList("content");
		ItemStack item = email.getItemStack("item", null);
		return new Email(sender,receiver,content,item);
	}


	public EmailConfig delEmail(String player, String id) {
		YamlConfiguration config = playerDatas.containsKey(player) ? playerDatas.get(player) : new YamlConfiguration();
		config.set("playerName", player);
		if (config.contains("email" + id)) {
			playerDatas.remove(player);
			YamlConfiguration newCfg = removeKey(config, "email");
			for(String key : config.getConfigurationSection("email").getKeys(false)) {
				if(!key.equals(id)) {
					newCfg.set("email." + key, config.getConfigurationSection("email." + key));
				}
			}
			playerDatas.put(player, newCfg);
		}
		return this;
	}

	public YamlConfiguration removeKey(YamlConfiguration con, String key) {
		YamlConfiguration result = new YamlConfiguration();
		for (String k : con.getKeys(false)) {
			if (!k.equalsIgnoreCase(key)) {
				result.set(k, con.get(k));
			}
		}
		return result;
	}

	public EmailConfig reloadConfig() {
		try {
			if (!configPath.exists()) {
				configPath.mkdirs();
			}
			if (this.playerDatas == null) {
				this.playerDatas = new HashMap<String, YamlConfiguration>();
			}
			this.playerDatas.clear();
			for (File file : configPath.listFiles()) {
				try {
					if (file.getName().toLowerCase().endsWith(".yml")) {
						String playerName = file.getName().substring(0, file.getName().lastIndexOf(".yml"));
						if (!playerName.isEmpty()) {
							this.playerDatas.put(playerName, YamlConfiguration.loadConfiguration(file));
						}
					}
				} catch (Throwable t) {
					t.printStackTrace();
					continue;
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return this;
	}

	public EmailConfig saveConfig() {
		try {
			Set<String> players = this.playerDatas.keySet();
			for (String player : players) {
				playerDatas.get(player).save(new File(configPath, player + ".yml"));
			}

			for (File file : configPath.listFiles()) {
				String playerName = file.getName().substring(0, file.getName().lastIndexOf(".yml"));
				if (!players.contains(playerName)) {
					file.delete();
				}
			}

		} catch (Throwable t) {
			t.printStackTrace();
		}
		return this;
	}
}
