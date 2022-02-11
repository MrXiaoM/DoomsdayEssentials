package top.mrxiaom.doomsdayessentials.listener;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.cyr1en.cp.PromptRegistry;
import com.gmail.nossr50.api.ChatAPI;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lenis0012.bukkit.marriage2.MPlayer;
import com.lenis0012.bukkit.marriage2.MarriageAPI;
import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import fr.xephi.authme.api.v3.AuthMeApi;
import io.github.divios.dependencies.Core_lib.misc.ChatPrompt;
import me.clip.placeholderapi.PlaceholderAPI;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.maxgamer.quickshop.QuickShop;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.ParkourConfig.Parkour;
import top.mrxiaom.doomsdayessentials.modules.reviveme.ReviveMeApi;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.NMSUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ChatListener {
	final Main plugin;
	private String switchSymbol;
	private String format;
	private String formatLocal;
	private boolean isEnableLocalMode;
	private boolean isDefaultLocal;
	private final Logger logger;
	private double localRange;
	private static final double UNLIMITED_RANGE = -1;
	public ChatListener(Main plugin) {
		this.plugin = plugin;
		logger = Logger.getLogger("CHAT");
		plugin.getProtocolManager().addPacketListener(new ChatPacketAdapter());
	}

	public class ChatPacketAdapter extends PacketAdapter {
		ChatPacketAdapter(){
			super(ChatListener.this.plugin, ListenerPriority.NORMAL, PacketType.Play.Client.CHAT);
		}
		@Override
		public void onPacketReceiving(PacketEvent event) {
			if (event.getPacketType() != PacketType.Play.Client.CHAT || event.isCancelled())
				return;
			Player player = event.getPlayer();
			PacketContainer packet = event.getPacket();
			String msg = packet.getStrings().read(0);
			if (Util.removeColor(msg).contains("${")){
				event.setCancelled(true);
				return;
			}
			// 重定向命令前缀
			if (msg.startsWith("、")) {
				msg = "/" + msg.substring(1);
				packet.getStrings().write(0, msg);
			}
			// 命令补全，重定向 /cancel 到 cancel
			if (checkCommandPromptChat(event.getPlayer()) && msg.toLowerCase().startsWith("/cancel")){
				packet.getStrings().write(0, "cancel");
				return;
			}
			// 已倒地的玩家不得执行除私聊外的命令
			if(!player.isOp() && ReviveMeApi.isPlayerDowned(player) && msg.startsWith("/")
					&& !msg.toLowerCase().startsWith("/msg ") && !msg.toLowerCase().startsWith("/m ") && !msg.toLowerCase().startsWith("/tell ")
					&& !msg.equalsIgnoreCase("/msg") && !msg.equalsIgnoreCase("/m") && !msg.equalsIgnoreCase("/tell")){
				event.setCancelled(true);
				player.sendMessage(I18n.t("reiveme.no-command", true));
				return;
			}
			// 封禁部分命令
			if(msg.trim().startsWith("/")) {
				msg = msg.substring(msg.indexOf("/") + 1);
				if (msg.contains(" ") && msg.substring(0, msg.indexOf(" ")).contains(":")) {
					msg = msg.substring(msg.indexOf(":") + 1);
				}
				String cmd = msg.contains(" ") ? msg.substring(0, msg.indexOf(" ")) : msg;
				msg = msg.substring(cmd.length());
				if(msg.startsWith(" ")) msg = msg.substring(1);
				String[] args = msg.length() > 0 ? (msg.contains(" ") ? msg.split(" ")
						: new String[] { msg }) : new String[0];
				if(onPlayerCommandPreProcess(player, msg, cmd, args)) event.setCancelled(true);
				return;
			}
			if(onPlayerChat(player, msg)) event.setCancelled(true);
		}

		@Override
		public Plugin getPlugin() {
			return plugin;
		}
	}

	public void reloadConfig() {
		FileConfiguration config = plugin.getConfig();
		this.format = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNullElse(config.getString("chat.format"), "<%player_name%> {message}"));
		this.formatLocal = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNullElse(config.getString("chat.format-local"), "(local)<%player_name%> {message}"));
		this.isEnableLocalMode = config.getBoolean("chat.enable-local");
		this.isDefaultLocal = config.getBoolean("chat.default-local");
		this.switchSymbol = config.getString("chat.switch-symbol");
		this.localRange = config.getDouble("chat.local-range");
	}

	/**
	 * 预处理命令，比几乎任何插件都快，还能拦截，适合作拦截命令
	 * @param player 执行命令的玩家
	 * @param raw 原命令
	 * @param cmd 命令跟
	 * @param args 命令参数
	 * @return 是否阻止命令执行
	 */
	public boolean onPlayerCommandPreProcess(Player player, String raw, String cmd, String[] args) {
		if (cmd.equalsIgnoreCase("marry") && args.length > 0) {
			if(args[0].equalsIgnoreCase("sethome")) {
				Parkour p = plugin.getParkoursConfig().getParkourPlayerIn(player);
				if (p != null) {
					player.sendMessage("§7[§9末日社团§7] §c你不能在跑酷场地设置家");
					return true;
				}
			}
			if(args[0].equalsIgnoreCase("home")){
				MPlayer mPlayer = MarriageAPI.getInstance().getMPlayer(player);
				if (mPlayer.isMarried()) {
					Location loc = mPlayer.getMarriage().getHome();
					if (plugin.getParkoursConfig().getParkourByLoc(loc) != null) {
						player.sendMessage("§7[§9末日社团§7] §c你设置的家在跑酷场地中， 无法传送");
						return true;
					}
				}
			}
			if(args[0].equalsIgnoreCase("tp")){
				MPlayer mPlayer = MarriageAPI.getInstance().getMPlayer(player);
				if (mPlayer.isMarried()) {
					Player partner = Bukkit.getPlayer(mPlayer.getPartner().getUniqueId());
					if (partner != null && partner.isOnline()) {
						player.sendMessage("§7[§9末日社团§7] §c你的伴侣在跑酷场地，你不能传送到TA那里");
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * 处理玩家发送的聊天包，拥有最高优先级
	 * @param player 发送者
	 * @param msg 原消息
	 * @return 是否取消事件，使其他插件不处理消息
	 */
	private boolean onPlayerChat(Player player, String msg) {
		// 兼容各个插件
		if (this.checkMute(player) || this.checkAuthmeChat(player) || this.checkQuickShopChat(player)
				|| this.checkCommandPromptChat(player) || this.checkResidenceChat(player) 
				|| this.checkMcMMOChat(player) || this.checkMarriageChat(player)
				|| this.checkShopKeepersChat(player) || this.checkDailyShop(player)) {
			return false;
		}
		if (msg.length() > 128) {
			player.sendMessage(I18n.t("chat.too-long", true));
			return true;
		}
		// 可附近聊天模式
		if (this.isEnableLocalMode) {

			// 如果有聊天检测符号
			if (msg.startsWith(this.switchSymbol)) {
				// 附近聊天
				if (!isDefaultLocal)
					this.handleChat(msg.substring(1), player, localRange);
				else
					this.handleChat(msg, player, UNLIMITED_RANGE);
			} else {
				// 否则全局聊天
				if (!isDefaultLocal)
					this.handleChat(msg, player, UNLIMITED_RANGE);
				else
					this.handleChat(msg.substring(1), player, localRange);
			}
		}
		// 不可附近聊天模式
		else {
			this.handleChat(msg, player, UNLIMITED_RANGE);
		}
		return true;
	}

	public JsonObject genChatObj(String str) {
		JsonObject result = new JsonObject();
		result.addProperty("text", str);
		return result;
	}

	/**
	 * 将物品转化为 tellraw 所需格式
	 *
	 * @param item 将要转化的物品
	 */
	public JsonObject genChatObj(ItemStack item) {
		if(item == null) return new JsonObject();
		try {
			String nmsVersion = NMSUtil.getNMSVersion();
			Class<?> classItemStack = Class.forName("net.minecraft.server." + nmsVersion + ".ItemStack");
			Class<?> classIChatBase = Class.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent");
			Class<?> classChatSerializer = classIChatBase.getDeclaredClasses()[0];
			Class<?> classCraftItemStack = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".inventory.CraftItemStack");
			Method asNMSCopy = classCraftItemStack.getDeclaredMethod("asNMSCopy", ItemStack.class);
			Method toJson = classChatSerializer.getDeclaredMethod("b", classIChatBase);
			Method toChatMsg = null;
			for(Method m : classItemStack.getDeclaredMethods()) {
				if(m.getParameterTypes().length == 0 && m.getName().length() < 3 
						&& m.getReturnType().equals(classIChatBase))	
				{
					toChatMsg = m;
				}
			}
			if(toChatMsg == null) throw new NoSuchMethodException("net.minecraft.server." + nmsVersion + ".ItemStack.<unknown>() IChatBaseComponent");
			Object nmsItem = asNMSCopy.invoke(null, item);
			Object chatMsg = toChatMsg.invoke(nmsItem);
			return ((JsonElement) toJson.invoke(null, chatMsg)).getAsJsonObject();
		}catch(Throwable t) {
			t.printStackTrace();
		}
		return new JsonObject();
	}

	/**
	 * 处理物品展示 %1-9
	 *
	 * @param player 执行的玩家，物品从他身上获取
	 * @param message 需要处理的原消息
	 * @return 返回 JsonArray(有物品，包括空气) 或者 String(无物品)
	 **/
	public Object handleItemDisplayString(Player player, String message) {
		if (message.contains("%")) {
			String[] msgs = message.split("%");
			if (msgs.length < 2) return message;
			String a = msgs[1].toLowerCase();
			if (a.length() > 1) {
				a = a.substring(0, 1);
			}
			if (a.equalsIgnoreCase("i") || a.equalsIgnoreCase("1") || a.equalsIgnoreCase("2") || a.equalsIgnoreCase("3")
					|| a.equalsIgnoreCase("4") || a.equalsIgnoreCase("5") || a.equalsIgnoreCase("6")
					|| a.equalsIgnoreCase("7") || a.equalsIgnoreCase("8") || a.equalsIgnoreCase("9")) {
				JsonArray json = new JsonArray();
				int slot = a.equalsIgnoreCase("i") ? player.getInventory().getHeldItemSlot()
						: (Integer.parseInt(a) - 1);

				json.add(genChatObj(msgs[0]));
				ItemStack item = player.getInventory().getItem(slot);
				if (item == null) {
					json.add(genChatObj("[空气]"));
				}
				else {
					json.add(genChatObj(item));
				}
				json.add(genChatObj(msgs[1].substring(1)));
				return json;
			}
		}
		return message;
	}

	public static String chatToString(Object obj){
		if (obj instanceof JsonArray){
			JsonArray array = (JsonArray) obj;
			StringBuilder s = new StringBuilder();
			for(int i = 0; i < array.size(); i++){
				JsonObject one = array.get(i).getAsJsonObject();
				if(one.has("text")) s.append(one.get("text").getAsString());
			}
			return s.toString().replace("§", "&");
		}
		return ((String) obj).replace("§", "&");
	}

	/**
	 * 检查是否屏蔽
	 * @param sender 消息发送者
	 * @param receiver 消息接收者
	 * @param chatString 发送的内容
	 * @return 是否已屏蔽
	 */
	public boolean isIgnored(@NotNull Player sender, @NotNull Player receiver, @Nullable String chatString) {
		for (String s : plugin.getPlayerConfig().getIgnorePlayers(receiver.getName(), false))
			if (sender.getName().equalsIgnoreCase(s)) return true;
		if(chatString != null) for (String s : plugin.getPlayerConfig().getIgnoreMsgs(receiver.getName(), false))
			if (chatString.toLowerCase().contains(s.toLowerCase())) return true;
		for (String s : plugin.getPlayerConfig().getIgnorePlayers(receiver.getName(), true)){
			try {
				if (Pattern.matches(s, chatString)) return true;
			} catch(Throwable t){
				receiver.sendMessage(I18n.prefix() + " §6匹配屏蔽玩家名正则表达式 §c" + s +" §6时出现一个异常");
				receiver.sendMessage(Util.getThrowableMessage(t, "§c"));
			}
		}
		if(chatString != null) for (String s : plugin.getPlayerConfig().getIgnoreMsgs(receiver.getName(), true)){
			try {
				if (Pattern.matches(s, chatString)) return true;
			} catch(Throwable t){
				receiver.sendMessage(I18n.prefix() + " §6匹配屏蔽消息正则表达式 §c" + s +" §6时出现一个异常");
				receiver.sendMessage(Util.getThrowableMessage(t, "§c"));
			}
		}
		return false;
	}

	public void handleChat(String rawMessage, Player sender, double range) {
		// 替换围在外面，避免全部&都替换成§
		String message = PlaceholderAPI.setPlaceholders(sender, range < 0 ? this.format : this.formatLocal)
				.replace("{message}", Util.replaceColor(rawMessage, sender));
		Object chatText = this.handleItemDisplayString(sender, message);
		Location location = sender.getLocation();
		Bukkit.getScheduler().runTask(this.plugin, () -> {
			logger.info(message);
			for (Player receiver : Bukkit.getOnlinePlayers()) {
				if (range < 0 || (location.getWorld() != null
						&& receiver.getWorld().getName().equals(location.getWorld().getName())
						&& location.distance(receiver.getLocation()) <= range)) {
					String chatString = chatToString(chatText);
					if(isIgnored(sender, receiver, chatString)) continue;
					NMSUtil.sendChatPacket(receiver, chatText);
				}
			}
		});
	}

	public boolean checkAuthmeChat(Player player) {
		try {
			// 兼容登录插件，未登录禁止发消息
			if (!AuthMeApi.getInstance().isAuthenticated(player)) {
				player.sendMessage(I18n.t("not-login", true));
				return true;
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return false;
	}

	public boolean checkQuickShopChat(Player player) {
		try {
			return QuickShop.getInstance().getShopManager().getActions().containsKey(player.getUniqueId());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return false;
	}

	public boolean checkCommandPromptChat(Player player) {
		try {
			return PromptRegistry.inCommandProcess(player);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return false;
	}

	public boolean checkResidenceChat(Player player) {
		try {
			ClaimedResidence res = ResidenceApi.getResidenceManager().getByLoc(player.getLocation());
			return res != null && res.getChatChannel() != null && res.getChatChannel().hasMember(player.getName());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return false;
	}

	public boolean checkMcMMOChat(Player player) {
		try {
			return ChatAPI.isUsingPartyChat(player) || ChatAPI.isUsingAdminChat(player);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return false;
	}

	public boolean checkMarriageChat(Player player) {
		try {
			return MarriageAPI.getInstance().getMPlayer(player).isInChat();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return false;
	}

	public boolean checkShopKeepersChat(Player player) {
		try {
			return SKShopkeepersPlugin.getInstance().getShopkeeperNaming().isNaming(player);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return false;
	}

	public boolean checkMute(Player player) {
		try {
			return PunishmentManager.get().isMuted(UUIDManager.get().getUUID(player.getName()));
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return false;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public boolean checkDailyShop(Player player) {
		try {
			Class<ChatPrompt> cls = ChatPrompt.class;
			Field field = cls.getDeclaredField("prompts");
			field.setAccessible(true);
			Set<Player> players = (Set<Player>) (((Map) field.get(null)).keySet());
			for (Player p : players) {
				if (p!= null && p.getName().equalsIgnoreCase(player.getName())) return true;
			}
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return false;
	}
}
