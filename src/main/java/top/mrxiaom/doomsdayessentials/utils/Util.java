package top.mrxiaom.doomsdayessentials.utils;

import com.blank038.servermarket.ServerMarket;
import com.blank038.servermarket.data.gui.SaleItem;
import com.blank038.servermarket.enums.PayType;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.WorldBorderAction;
import com.mcrmb.PayApi;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class Util {
	public static final Logger logger = Logger.getLogger("ALERT");

	public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name) {
		return valueOf(enumType, name, null);
	}

	public static <T extends Enum<T>> T valueOf(Class<T> enumType, String name, T nullValue) {
		for (T t : enumType.getEnumConstants()) {
			if (t.name().equalsIgnoreCase(name))
				return t;
		}
		return nullValue;
	}

	public static Object valueOfForce(Class<?> enumType, String name) {
		return valueOfForce(enumType, name, null);
	}

	public static Object valueOfForce(Class<?> enumType, String name, Object nullValue) {
		for (Object t : enumType.getEnumConstants()) {
			if (((Enum<?>) t).name().equalsIgnoreCase(name))
				return t;
		}
		return nullValue;
	}

	public static void sendItemToMail(String player, String sender, ItemStack item) {
		SaleItem si = new SaleItem(UUID.randomUUID().toString(), "xxxxxxxx-xxxx-xxxx-xxxxxxxxxxxxxxxx", sender, item, PayType.VAULT, 0, System.currentTimeMillis());
		ServerMarket.getInstance().getApi().getPlayerData(player).addItem(si);
		Player p = Util.getOnlinePlayer(player);
		if(p != null) {
			p.sendMessage(I18n.t("mail-receive", true).replace("%item%", ItemStackUtil.getItemDisplayName(item)));
		}
	}

	public static int getIntegerMin(int... list) {
		int result = list[0];
		for (int i : list) {
			if (i < result)
				result = i;
		}
		return result;
	}

	public static int getIntegerMax(int... list) {
		int result = list[0];
		for (int i : list) {
			if (i > result)
				result = i;
		}
		return result;
	}

	public static int randomIntegerBetween(int a, int b) {
		return (a > b ? b : a) + new Random().nextInt((a > b ? a : b) - (a > b ? b : a));
	}

	public static double randomDoubleBetween(double a, double b, int digit) {
		if (digit < 1)
			return a;
		double c = Math.pow(10, digit);
		return (a > b ? b : a) + new Random().nextInt((int) ((a > b ? a : b) * c) - (int) ((a > b ? b : a) * c)) / c;
	}

	public static void clearPlayerEffects(Player player) {
		if (player == null)
			return;
		for (PotionEffectType type : PotionEffectType.values()) {
			if (type == null)
				continue;
			if (player.hasPotionEffect(type)) {
				player.removePotionEffect(type);
			}
		}
	}

	public static String replaceColor(String msg, CommandSender sender) {
		String result = msg;
		if (sender.hasPermission("doomsdaychat.color")) {
			result = result.replace("&a", "§a").replace("&b", "§b").replace("&c", "§c").replace("&d", "§d")
					.replace("&e", "§e").replace("&f", "§f").replace("&0", "§0").replace("&1", "§1").replace("&2", "§2")
					.replace("&3", "§3").replace("&4", "§4").replace("&5", "§5").replace("&6", "§6").replace("&7", "§7")
					.replace("&8", "§8").replace("&9", "§9");
		}
		if (sender.hasPermission("doomsdaychat.color.format")) {
			result = result.replace("&r", "§r").replace("&l", "§l").replace("&m", "§m").replace("&n", "§n")
					.replace("&o", "§o");
		}
		if (sender.hasPermission("doomsdaychat.color.magic")) {
			result = result.replace("&k", "§k");
		}
		return result;
	}

	public static int look_MCRMB(Player player) {
		return look_MCRMB(player.getName());
	}

	public static int look_MCRMB(String player) {
		return PayApi.look(player);
	}

	public static boolean buy_MCRMB(Player player, int money, String reason) {
		return buy_MCRMB(player.getName(), money, reason);
	}

	public static boolean buy_MCRMB(String player, int money, String reason) {
		return PayApi.Pay(player, String.valueOf(money), reason, false);
	}

	public static boolean noPerm(CommandSender sender) {
		sender.sendMessage("§7[§9末日社团§7] §c你没有执行该命令的权限");
		return true;
	}

	public static boolean noPlayer(CommandSender sender) {
		sender.sendMessage("§c我一眼就看出你不是人");
		return true;
	}

	public static boolean noEcoApi(CommandSender sender) {
		sender.sendMessage("§7[§9末日社团§7]§c 找不到经济API，这是个错误，请联系管理员");
		return true;
	}

	public static boolean isNoClearEntities(Entity e) {
		return e instanceof org.bukkit.entity.Endermite
				// boss 相关
				|| e instanceof org.bukkit.entity.EnderDragon || e instanceof org.bukkit.entity.ElderGuardian
				|| e instanceof org.bukkit.entity.WitherSkeleton
				// 袭击相关
				|| e instanceof org.bukkit.entity.Vex || e instanceof org.bukkit.entity.Evoker
				|| e instanceof org.bukkit.entity.Illusioner || e instanceof org.bukkit.entity.Vindicator
				|| e instanceof org.bukkit.entity.Raider || e instanceof org.bukkit.entity.Pillager
				|| e instanceof org.bukkit.entity.Ravager

				|| e instanceof org.bukkit.entity.Wither;
	}

	public static Calendar getDoomsdayEssentialsUpdateTime() {
		try {
			String path = URLDecoder.decode(Util.class.getClassLoader()
					.getResource(Util.class.getName().replace('.', '/') + ".class").getPath(), "UTF-8");
			JarFile jf = new JarFile(path.substring(6, path.indexOf("!")));
			Calendar date = Calendar.getInstance();
			date.setTimeInMillis(jf.getEntry("META-INF/MANIFEST.MF").getLastModifiedTime().toMillis());
			jf.close();
			return date;
		} catch (Throwable t) {
			System.gc();
			t.printStackTrace();
		}
		return null;
	}

	public static String getDoomsdayEssneitialsUpdateTimeString() {
		Calendar date = Util.getDoomsdayEssentialsUpdateTime();
		if (date == null)
			return "2077/12/10 --:--:--";
		int year = date.get(Calendar.YEAR);
		int month = date.get(Calendar.MONTH) + 1;
		int day = date.get(Calendar.DAY_OF_MONTH);
		int hour = date.get(Calendar.HOUR_OF_DAY);
		int minute = date.get(Calendar.MINUTE);
		int second = date.get(Calendar.SECOND);
		return year + "/" + (month < 10 ? "0" : "") + month + "/" + (day < 10 ? "0" : "") + day + " "
				+ (hour < 10 ? "0" : "") + hour + ":" + (minute < 10 ? "0" : "") + minute + ":"
				+ (second < 10 ? "0" : "") + second;
	}

	protected static String getThrowableMessage(Throwable e) {
		String result = "" + e.getClass().getName() + " : " + e.getMessage();
		for (int i = 0; i < 10; i++) {
			if (i < e.getStackTrace().length) {
				result = result + "\n" + e.getStackTrace()[i];
			} else
				break;
		}
		return result;
	}

	public static boolean checkCustomNpc(Entity entity) {
		return entity == null || entity.hasMetadata("NPC") || entity.hasMetadata("MyPet")
				|| entity.hasMetadata("MythicMobs") || entity.hasMetadata("shopkeeper");
	}

	public static String readFile(File file){
		return readFile(file, "GBK");
	}

	public static String readFile(File file, String encode) {
		String result = "";
		InputStreamReader read = null;
		try {
			read = new InputStreamReader(new FileInputStream(file), encode);

			BufferedReader bufferedReader = new BufferedReader(read);

			String lineTxt = null;
			while ((lineTxt = bufferedReader.readLine()) != null) {
				result += lineTxt + "\n";
			}
			read.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return result;
	}

	public static Throwable writeFile(File file, String content) {
		Throwable t = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}

			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "GBK"));
			writer.write(content);
			writer.close();
		} catch (Throwable e) {
			t = e;
		}
		return t;
	}

	public static boolean startsWithIgnoreCase(String text, String prefix) {
		if (text.length() >= prefix.length())
			return text.substring(0, prefix.length()).equalsIgnoreCase(prefix);
		else
			return false;
	}

	public static long strToLong(String s, Long nullReturnValue) {
		try {
			return Long.valueOf(s);
		} catch (NumberFormatException ex) {
			return nullReturnValue;
		}
	}

	public static int strToInt(String s, Integer nullReturnValue) {
		try {
			return Integer.valueOf(s);
		} catch (NumberFormatException ex) {
			return nullReturnValue;
		}
	}

	public static float strToFloat(String s, Float nullReturnValue) {
		try {
			return Float.valueOf(s);
		} catch (NumberFormatException ex) {
			return nullReturnValue;
		}
	}

	public static double strToDouble(String s, Double nullReturnValue) {
		try {
			return Double.valueOf(s);
		} catch (NumberFormatException ex) {
			return nullReturnValue;
		}
	}

	public static Float getFloatFromConfig(ConfigurationSection config, String key) {
		return Util.getFloatFromConfig(config, key, 0.0F);
	}

	public static Float getFloatFromConfig(ConfigurationSection config, String key, Float nullValue) {
		try {
			if (config.contains(key)) {
				if (config.isDouble(key)) {
					return Util.convertToFloat(config.getDouble(key));
				} else {
					return (float) config.get(key);
				}
			}
		} catch (Throwable t) {
		}
		return nullValue;
	}

	public static Float convertToFloat(Double doubleValue) {
		return doubleValue == null ? null : doubleValue.floatValue();
	}

	public static String limitLength(String str, int length) {
		if (str.length() < length)
			return str;
		return str.substring(0, length - 1) + "…";
	}

	public static String removeColor(String str) {
		String result = "";
		char[] a = str.toCharArray();
		for (int i = 0; i < a.length; i++) {
			if (a[i] == ChatColor.COLOR_CHAR) {
				i++;
				continue;
			}
			result += String.valueOf(a[i]);
		}
		return result;
	}
	
	@Nullable
	public static OfflinePlayer getOfflinePlayer(String name) {
		for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			if (player.getName() != null && player.getName().equalsIgnoreCase(name)) {
				return player;
			}
		}
		return null;
	}

	@Nullable
	public static Player getOnlinePlayer(String name) {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.getName() != null && player.getName().equalsIgnoreCase(name)) {
				return player;
			}
		}
		return null;
	}

	public static void alert(String msg) {
		alert(msg, true);
	}

	public static void alert(String msg, boolean log) {
		String alert = ChatColor.translateAlternateColorCodes('&', msg);
		if (log)
			logger.info(alert);
		for (Player p : Bukkit.getOnlinePlayers()) {
			p.sendMessage(alert);
		}
	}

	public static boolean registerPlaceholder(Logger logger, PlaceholderExpansion placeholder, String name) {
		boolean result = placeholder.register();
		if (!result) {
			logger.info("无法注册 " + name + " PAPI 变量");
		}
		return result;
	}
	
	public static void fakeDeath(Player player) {
		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(player, ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.UPDATE_HEALTH));
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	public static void updateHealth(Player player) {
		updateHealth(player, player.getHealth(), player.getFoodLevel(), player.getSaturation());
	}
	
	public static void updateHealth(Player player, double health, int foodLevel, float saturation) {
		try {
			PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.UPDATE_HEALTH);
			packet.getFloat().write(0, Util.convertToFloat(health));
			packet.getIntegers().write(0, foodLevel);
			packet.getFloat().write(1, saturation);
			ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	public static void updateWarningDistance(Player player) {
		updateWarningDistance(player, player.getWorld().getWorldBorder().getWarningDistance());
	}
	public static void updateWarningDistance(Player player, int distance) {
		try {
			PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.WORLD_BORDER);
			packet.getEnumModifier(WorldBorderAction.class, 0).write(0, WorldBorderAction.SET_WARNING_BLOCKS);
			packet.getIntegers().write(2, distance);
			ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	public static void updateWarningTime(Player player) {
		updateWarningTime(player, player.getWorld().getWorldBorder().getWarningTime());
	}
	public static void updateWarningTime(Player player, int time) {
		try {
			PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.WORLD_BORDER);
			packet.getEnumModifier(WorldBorderAction.class, 0).write(0, WorldBorderAction.SET_WARNING_BLOCKS);
			packet.getIntegers().write(1, time);
			ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	// NeverLag 清理密集实体
	public static void checkAndCleanMobFarm() {
		for (World world : Bukkit.getWorlds()) {
			for (LivingEntity entity : world.getLivingEntities()) {
				if (entity instanceof Monster || entity instanceof Animals || entity.getType() == EntityType.SQUID) {
					if (Util.checkCustomNpc(entity)) {
						continue;
					}
					if (entity.getCustomName() == null) {
						if (getNearbyEntityCount(entity, false) >= 30 || getNearbyEntityCount(entity, true) >= 5) {
							System.out.println("[实体农场] 已清理 " + entity.getName() + " (" + entity.getWorld().getName() + ": " +
									entity.getLocation().getBlockX() + ", " + entity.getLocation().getBlockY() +
									", " + entity.getLocation().getBlockZ() + ")");
							// CoreProtect.getInstance().getAPI().logRemoval("清理 " +
							// entity.getType().name(), entity.getLocation(), Material.NETHER_STAR,
							// (byte)0);
							entity.remove();
						}
					}
				}
			}
		}
	}

	public static int getNearbyEntityCount(LivingEntity entity, boolean isTiny) {
		List<Entity> entityList = new ArrayList<>();
		if (isTiny) {
			entityList = entity.getNearbyEntities(0.50D, 3.5D, 0.5D);
		} else {
			entityList = entity.getNearbyEntities(2.25D, 4.5D, 2.25D);
		}
		int count = 0;
		for (Entity ent : entityList) {
			if (ent instanceof Monster || ent instanceof Animals || ent instanceof Villager
					|| ent.getType() == EntityType.SQUID) {
				count++;
			}
		}
		return count;
	}

}
