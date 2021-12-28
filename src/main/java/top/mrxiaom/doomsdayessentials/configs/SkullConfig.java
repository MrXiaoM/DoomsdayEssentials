package top.mrxiaom.doomsdayessentials.configs;

import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import de.tr7zw.nbtapi.NBTListCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SkullConfig {
	final File configPath;
	FileConfiguration config;
	final Main plugin;

	public static class Skull {

		final String type;
		final String displayName;

		final String texturesValue;

		final double change;

		final List<String> lore;

		private final Date data = new Date();

		private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

		Skull(double change, String type, List<String> lore, String texturesValue) {

			lore.replaceAll(s -> s.replace("%time%", simpleDateFormat.format(this.data)));

			this.lore = lore;
			this.type = type;

			this.displayName = ChatColor.translateAlternateColorCodes('&', type + "的头");
			this.texturesValue = texturesValue;
			this.change = change;
		}

		public String getType() {
			return type;
		}

		public double getChange() {
			return this.change;
		}

		public ItemStack getItemStack() {

			ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
			NBTItem nbtItem = new NBTItem(head);

			NBTCompound skull = nbtItem.addCompound("SkullOwner");
			skull.setString("Id", String.valueOf(UUID.randomUUID()));

			NBTListCompound texture = skull.addCompound("Properties").getCompoundList("textures").addCompound();
			texture.setString("Value", texturesValue);

			ItemStack itemStack = nbtItem.getItem();

			return new ItemStackUtil(itemStack).name(displayName).lore(lore).build();
		}
	}

	public SkullConfig(Main plugin) {
		this.plugin = plugin;
		this.configPath = new File(plugin.getDataFolder(), "skull.yml");
		this.reloadConfig();
	}

	public FileConfiguration getConfig() {
		return this.config;
	}

	public String getEntityName(String value) {
		ConfigurationSection section = this.getConfig().getConfigurationSection("SkullType");
		if (section == null)
			return null;

		for (String entityName : section.getKeys(false)) {

			String configValue = section.getString(entityName.concat(".Value"));
			if (value.equals(configValue))
				return entityName;
		}
		return null;
	}
	public Skull getSkull(EntityType entityType) {

		ConfigurationSection skullType = this.config.getConfigurationSection("SkullType");

		String name = entityType.name();
		if (skullType.getString(name) == null) {
			Bukkit.getConsoleSender().sendMessage(
					ChatColor.translateAlternateColorCodes('&', "&9&lSakuraHead &6&l>> &c生物" + name + "不存在, 请检查配置文件"));
		}

		double change = skullType.getDouble(entityType + ".Change");
		String displayName = skullType.getString(entityType + ".DisplayName");
		List<String> lore = skullType.getStringList(entityType + ".Lore");
		String value = skullType.getString(entityType + ".Value");

		return new Skull(change, displayName, lore, value);
	}

	public void reloadConfig() {
		try {
			if (!configPath.exists()) {
				this.plugin.saveResource("skull.yml", true);
			}
			this.config = YamlConfiguration.loadConfiguration(this.configPath);

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public String getPlayerHeadDisplayName() {
		return this.config.getString("Player.DisplayName", "&d&l%killed%");
	}

	public double getPlayerHeadChance() {
		return this.config.getDouble("Player.Change", 0.0001D);
	}
}
