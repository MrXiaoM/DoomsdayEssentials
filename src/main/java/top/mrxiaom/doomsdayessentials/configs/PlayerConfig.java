package top.mrxiaom.doomsdayessentials.configs;

import com.google.common.collect.Lists;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.doomsdayessentials.Main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PlayerConfig {

	FileConfiguration config;
	final File configFile;
	final Main plugin;

	public PlayerConfig(Main plugin) {
		this.plugin = plugin;
		configFile = new File(plugin.getDataFolder().getAbsolutePath() + "\\data.yml");
		this.reloadConfig();
	}

	public FileConfiguration getConfig() {
		return this.config;
	}

	public List<String> getPlayers(){
		return Lists.newArrayList(this.config.getKeys(false));
	}

	public PlayerConfig addNeedle(String player, int amount) {
		int a = this.getNeedle(player);
		return this.setNeedle(player, a + amount);
	}

	/**
	 * 屏蔽某玩家
	 * @param player 请求屏蔽的玩家
	 * @param target 屏蔽目标
	 * @param isRegex 是否为正则表达式
	 * @return 是否已屏蔽成功
	 */
	public boolean ignoreAddPlayer(String player, String target, boolean isRegex){
		List<String> keys = new ArrayList<>();
		if (config.contains(player + ".ignore.players" + (isRegex ? "-regex" : "")))
			keys = config.getStringList(player + ".ignore.players" + (isRegex ? "-regex" : ""));
		if(keys.contains(target)) return false;
		keys.add(target);
		config.set(player + ".ignore.players" + (isRegex ? "-regex" : ""), keys);
		return true;
	}

	/**
	 * 获取已屏蔽的玩家列表
	 * @param player 请求获取的玩家
	 * @param isRegex 是否为正则表达式
	 * @return 玩家列表
	 */
	public List<String> getIgnorePlayers(String player, boolean isRegex){
		List<String> keys = new ArrayList<>();
		if (config.contains(player + ".ignore.players" + (isRegex ? "-regex" : "")))
			keys = config.getStringList(player + ".ignore.players" + (isRegex ? "-regex" : ""));
		return keys;
	}

	/**
	 * 取消屏蔽的玩家
	 * @param player 请求屏蔽的玩家
	 * @param target 屏蔽目标
	 * @param isRegex 是否为正则表达式
	 * @return 是否已取消成功
	 */
	public boolean ignoreRemovePlayer(String player, String target, boolean isRegex){
		List<String> keys = new ArrayList<>();
		if (config.contains(player + ".ignore.players" + (isRegex ? "-regex" : "")))
			keys = config.getStringList(player + ".ignore.players" + (isRegex ? "-regex" : ""));
		if(!keys.contains(target)) return false;
		keys.remove(target);
		config.set(player + ".ignore.players" + (isRegex ? "-regex" : ""), keys);
		return true;
	}

	/**
	 * 屏蔽某聊天关键词
	 * @param player 请求屏蔽的玩家
	 * @param target 要屏蔽的消息
	 * @param isRegex 是否为正则表达式
	 * @return 是否屏蔽成功
	 */
	public boolean ignoreAddMsg(String player, String target, boolean isRegex){
		List<String> keys = new ArrayList<>();
		if (config.contains(player + ".ignore.messages" + (isRegex ? "-regex" : "")))
			keys = config.getStringList(player + ".ignore.messages" + (isRegex ? "-regex" : ""));
		if(keys.contains(target)) return false;
		keys.add(target);
		config.set(player + ".ignore.messages" + (isRegex ? "-regex" : ""), keys);
		return true;
	}

	/**
	 * 获取某人的聊天屏蔽关键词
	 * @param player 请求获取的玩家
	 * @param isRegex 是否为正则表达式
	 * @return 关键词列表
	 */
	public List<String> getIgnoreMsgs(String player, boolean isRegex){
		List<String> keys = new ArrayList<>();
		if (config.contains(player + ".ignore.messages" + (isRegex ? "-regex" : "")))
			keys = config.getStringList(player + ".ignore.messages" + (isRegex ? "-regex" : ""));
		return keys;
	}

	/**
	 * 取消屏蔽某聊天关键词
	 * @param player 请求屏蔽的玩家
	 * @param target 屏蔽的消息
	 * @param isRegex 是否为正则表达式
	 * @return 是否取消成功
	 */
	public boolean ignoreRemoveMsg(String player, String target, boolean isRegex){
		List<String> keys = new ArrayList<>();
		if (config.contains(player + ".ignore.messages" + (isRegex ? "-regex" : "")))
			keys = config.getStringList(player + ".ignore.messages" + (isRegex ? "-regex" : ""));
		if(!keys.contains(target)) return false;
		keys.remove(target);
		config.set(player + ".ignore.messages" + (isRegex ? "-regex" : ""), keys);
		return true;
	}
	public PlayerConfig removeNeedle(String player, int amount) {
		int a = this.getNeedle(player);
		return this.setNeedle(player, a - amount);
	}

	public int getNeedle(String player) {
		if (!this.config.contains(player + ".needle")) return 3;
		return this.config.getInt(player + ".needle");
	}

	public PlayerConfig setNeedle(String player, int amount) {
		this.config.set(player + ".needle", amount);
		return this;
	}

	public void setIsShowBullets(String player, boolean value) {
		this.config.set(player + ".is-show-bullets", value);
		this.saveConfig();
	}

	public boolean isShowBullets(String player) {
		if (!this.config.contains(player + ".is-show-bullets")) return true;
		return this.config.getBoolean(player + ".is-show-bullets");
	}

	public PlayerConfig setSign(String player, String kit, SignTime time) {
		String kitRoot = player + ".kits." + kit + ".";
		this.config.set(kitRoot + "year", time.year);
		this.config.set(kitRoot + "month", time.month);
		this.config.set(kitRoot + "day", time.day);
		this.config.set(kitRoot + "hour", time.hour);
		this.config.set(kitRoot + "minute", time.minute);
		this.config.set(kitRoot + "second", time.second);
		int times = 0;
		if (this.config.contains(kitRoot + "times")) {
			times = this.config.getInt(kitRoot + "times");
		}
		times++;
		this.config.set(kitRoot + "times", times);
		return this;
	}

	public LastSignInfo getLastSignInfo(String player, String kit) {
		String kitRoot = player + ".kits." + kit + ".";
		if (!this.config.contains(player + ".kits." + kit)) {
			return new LastSignInfo(player, null, 0);
		}
		SignTime signTime = new SignTime(this.config.getInt(kitRoot + "year"), this.config.getInt(kitRoot + "month"),
				this.config.getInt(kitRoot + "day"), this.config.getInt(kitRoot + "hour"),
				this.config.getInt(kitRoot + "minute"), this.config.getInt(kitRoot + "second"));
		return new LastSignInfo(player, signTime, this.config.getInt(kitRoot + "times"));
	}

	public static class LastSignInfo {
		public final String player;
		public final SignTime signTime;
		public final int times;

		public LastSignInfo(String player, SignTime signTime, int times) {
			this.player = player;
			this.signTime = signTime;
			this.times = times;
		}
	}

	public static class SignTime {
		public final int year;
		public final int month;
		public final int day;
		public final int hour;
		public final int minute;
		public final int second;

		public SignTime(int year, int month, int day, int hour, int minute, int second) {
			this.year = year;
			this.month = month;
			this.day = day;
			this.hour = hour;
			this.minute = minute;
			this.second = second;
		}

		public static SignTime getNowTime() {
			Calendar now = Calendar.getInstance();
			return new SignTime(now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH),
					now.get(Calendar.HOUR), now.get(Calendar.MINUTE), now.get(Calendar.SECOND));
		}

		public boolean isTimeNotUp() {
			SignTime now = SignTime.getNowTime();
			return now.year <= year && now.month <= month && now.day <= day;
		}
	}

	public PlayerConfig reloadConfig() {
		if (configFile.exists()) {
			config = YamlConfiguration.loadConfiguration(configFile);
		} else {
			config = new YamlConfiguration();
			this.saveConfig();
		}
		return this;
	}

	public PlayerConfig saveConfig() {
		try {
			this.config.save(this.configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return this;
	}

	public PlayerConfig set(String key, String value) {
		this.config.set(key, value);
		return this;
	}
}
