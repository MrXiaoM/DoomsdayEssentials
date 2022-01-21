package top.mrxiaom.doomsdayessentials.utils;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.doomsdayessentials.Main;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class I18n {
	private static final boolean useCustomPrefix = false;
	private static YamlConfiguration config = new YamlConfiguration();

	public static void loadDefaultConfig(Main m) {
		InputStream is = m.getResource("lang-zh.yml");
		if (is == null) {
			config = new YamlConfiguration();
			new NullPointerException("无法读取默认语言文件为").printStackTrace();
			return;
		}
		config = YamlConfiguration.loadConfiguration(new InputStreamReader(is));
	}

	public static void loadConfig(Main m, YamlConfiguration c) {
		if (c == null)
			loadDefaultConfig(m);
		else
			config = c;
	}

	public static boolean contains(String key) {
		return config.contains("messages." + key);
	}

	public static String prefix() {
		if(!useCustomPrefix) return ChatColor.translateAlternateColorCodes('&', "&7[&9末日社团&7] &6");
		if (config == null)
			return "[notloaded]";
		if (!config.contains("prefix"))
			return "[notfound:prefix]";
		if (!config.isString("prefix"))
			return "[notstring:prefix]";
		return ChatColor.translateAlternateColorCodes('&', config.getString("prefix", "[notfound:prefix]"));
	}

	public static String t(String key) {
		return t(key, false);
	}

	public static String t(String key, boolean prefix) {
		if (config == null)
			return "[notloaded]";
		String prefixStr = "";
		if (prefix) prefixStr = prefix();

		if (!config.contains("messages." + key))
			return "[notfound:messages." + key + "]";
		if (!config.isString("messages." + key))
			return "[notstring:messages." + key + "]";
		return prefixStr
				+ ChatColor.translateAlternateColorCodes('&', config.getString("messages." + key).replace("\\n", "\n"));
	}

	public static String tn(String key) {
		return tn(key, false);
	}

	public static String tn(String key, boolean prefix) {
		if (config == null)
			return "[notloaded]";

		String prefixStr = "";
		if (prefix) prefixStr = prefix();

		if (!config.contains("messages." + key))
			return "[notfound:messages." + key + "]";
		if (!config.isList("messages." + key))
			return "[notlist:messages." + key + "]";
		List<String> list = config.getStringList("messages." + key);
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			result.append(prefixStr).append(ChatColor.translateAlternateColorCodes('&', list.get(i))).append(i < list.size() - 1 ? "\n" : "");
		}
		return result.toString();
	}

	public static List<String> l(String key) {
		return l(key, false);
	}
	public static List<String> l(String key, boolean prefix) {
		if (config == null)
			return Lists.newArrayList("[notloaded]");
		String prefixStr = "";
		if (prefix) prefixStr = prefix();
		if (!config.contains("messages." + key))
			return Lists.newArrayList("[notfound:messages." + key + "]");
		if (!config.isList("messages." + key))
			return Lists.newArrayList("[notlist:messages." + key + "]");
		List<String> result = new ArrayList<>();
		for (String s : config.getStringList("messages." + key)) {
			result.add((prefix ? prefixStr : "") + ChatColor.translateAlternateColorCodes('&', s));
		}
		return result;
	}

	public static String[] array(String key) {
		return array(key, false);
	}
	
	public static String[] array(String key, boolean prefix) {
		return I18n.l(key, prefix).parallelStream().toArray(String[]::new);
	}
}
