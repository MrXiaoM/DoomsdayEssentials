package top.mrxiaom.doomsdayessentials.configs;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GunConfig {
	final File configFile;
	final Main plugin;

	public static class Gun {
		private final String gunId;
		private final String name;
		private final float damage;
		private final float speed;
		private final float spread;
		private final Material material;
		private final List<String> lore;
		private final String bullet;
		private final int bulletsInt;
		private final int delay;
		private final String sound;
		private final float volume;
		private final float pitch;

		public Gun(String id, String name, Material material, List<String> lore, String bullet, int bulletsInt,
				int delay, float damage, float speed, float spread, String sound, float volume, float pitch) {
			this.gunId = id;
			this.name = name;
			this.material = material;
			this.damage = damage;
			this.speed = speed;
			this.spread = spread;
			this.lore = lore;
			this.bullet = bullet;
			this.bulletsInt = bulletsInt;
			this.delay = delay;
			this.sound = sound;
			this.volume = volume;
			this.pitch = pitch;
		}

		public String getName() {
			return this.name;
		}

		public float getSpeed() {
			return this.speed;
		}

		public float getDamage() {
			return this.damage;
		}

		public float getSpread() {
			return this.spread;
		}

		public String getGunId() {
			return this.gunId;
		}

		public String getSound() {
			return this.sound;
		}

		public float getVolume() {
			return this.volume;
		}

		public float getPitch() {
			return this.pitch;
		}

		public Material getMaterial() {
			return this.material;
		}

		public String getBullet() {
			return this.bullet;
		}

		public int getBulletsInt() {
			return this.bulletsInt;
		}

		public int getDelay() {
			return this.delay;
		}

		public ItemStack getItem() {
			ItemStack result = new ItemStack(material, 1);
			StringBuilder identifier = new StringBuilder("§g§u§n");
			for (char c : gunId.toCharArray()) {
				identifier.append("§").append(c);
			}
			List<String> itemLore = new ArrayList<>();
			for (String s : lore) {
				itemLore.add(ChatColor.translateAlternateColorCodes('&', s));
			}
			itemLore.add(identifier.toString());

			ItemStackUtil.setItemDisplayName(result, ChatColor.translateAlternateColorCodes('&', name));
			ItemStackUtil.setItemLore(result, itemLore);
			return result;
		}

		public List<String> getLore() {
			return lore;
		}
	}

	private Map<String, Gun> gunMap = new HashMap<>();

	public GunConfig(Main plugin) {
		this.plugin = plugin;
		this.configFile = new File(plugin.getDataFolder().getAbsolutePath() + "\\guns\\");
		this.reloadConfig();
	}

	public boolean contains(String gunName) {
		return this.gunMap.containsKey(gunName);
	}

	public Map<String, Gun> all() {
		return this.gunMap;
	}

	@Nullable
	public Gun get(String gunName) {
		if (this.contains(gunName)) {
			return this.gunMap.get(gunName);
		}
		return null;
	}

	public void set(String gunName, Gun warp) {
		this.gunMap.put(gunName, warp);
		this.saveConfig();
	}

	public void remove(String gunName) {
		if (this.contains(gunName)) {
			this.gunMap.remove(gunName);
			this.saveConfig();
		}
	}

	public void reloadConfig() {
		if (!configFile.exists()) {
			this.saveConfig();
		}

		this.gunMap = new HashMap<>();
		if (configFile.exists()) {
			File[] files = configFile.listFiles();
			if(files == null) return;
			for (File file : files) {
				try {
					YamlConfiguration gunConfig = YamlConfiguration.loadConfiguration(file);
					String id = file.getName().substring(0, file.getName().length() - 4);
					// System.out.println("正在载入 " + id + " : " + gunConfig.getString("material"));
					Material material = Material.getMaterial(gunConfig.getString("material").toUpperCase());
					if (material == null || material == Material.AIR) {
						System.out.println("枪械 " + id + " 配置中的物品ID载入失败");
						continue;
					}
					String name = ChatColor.translateAlternateColorCodes('&', gunConfig.getString("name"));
					List<String> lore = gunConfig.getStringList("lore");
					String bullet = gunConfig.getString("bullet");
					int bulletsInt = gunConfig.getInt("bulletsInt");
					int delay = gunConfig.getInt("delay");
					float damage = Util.getFloatFromConfig(gunConfig, "damage", 2.0F);
					float speed = Util.getFloatFromConfig(gunConfig, "speed", 1.0F);
					float spread = Util.getFloatFromConfig(gunConfig, "spread", 10.0F);
					String sound = null;
					float volume = 1.0F;
					float pitch = 1.0F;
					if (gunConfig.contains("sound")) {
						sound = gunConfig.getString("sound");
					}
					if (sound != null) {
						volume = Util.getFloatFromConfig(gunConfig, "volume", 1.0F);
						pitch = Util.getFloatFromConfig(gunConfig, "pitch", 1.0F);
					}
					this.gunMap.put(id, new Gun(id, name, material, lore, bullet, bulletsInt, delay, damage, speed,
							spread, sound, volume, pitch));
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
			for (String key : gunMap.keySet()) {
				try {
					Gun gun = gunMap.get(key);
					YamlConfiguration gunConfig = new YamlConfiguration();

					gunConfig.set("id", gun.getGunId());
					gunConfig.set("name", gun.getName());
					gunConfig.set("lore", gun.getLore());
					gunConfig.set("bullet", gun.getBullet());
					gunConfig.set("bulletsInt", gun.getBulletsInt());
					gunConfig.set("delay", gun.getDelay());
					gunConfig.set("material", gun.material.name());
					gunConfig.set("damage", gun.getDamage());
					gunConfig.set("speed", gun.getSpeed());
					gunConfig.set("spread", gun.getSpread());
					if (gun.getSound() != null) {
						gunConfig.set("sound", gun.getSound());
						gunConfig.set("volume", gun.getVolume());
						gunConfig.set("pitch", gun.getPitch());
					}
					gunConfig.save(new File(configFile, gun.getName() + ".yml"));
					files.add(gun.getName() + ".yml");
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
