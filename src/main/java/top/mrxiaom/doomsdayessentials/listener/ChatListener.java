package top.mrxiaom.doomsdayessentials.listener;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.*;
import com.cyr1en.cp.PromptRegistry;
import com.gmail.nossr50.api.ChatAPI;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lenis0012.bukkit.marriage2.MPlayer;
import com.lenis0012.bukkit.marriage2.MarriageAPI;
import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import fr.xephi.authme.api.v3.AuthMeApi;
import me.clip.placeholderapi.PlaceholderAPI;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.maxgamer.quickshop.QuickShop;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.ParkourConfig.Parkour;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.NMSUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.logging.Logger;

public class ChatListener implements Listener {
	final Main plugin;
	private String switchSymbol;
	private String format;
	private String formatLocal;
	private Boolean isEnableLocalMode;
	private Boolean isDefaultLocal;
	private final Logger logger;
	private Double localRange;
	public ChatListener(Main plugin) {
		this.plugin = plugin;
		logger = Logger.getLogger("CHAT");
		Bukkit.getPluginManager().registerEvents(this, plugin);
		plugin.getProtocolManager().addPacketListener(new ChatPacketAdapter());
	}

	public class ChatPacketAdapter extends PacketAdapter {
		ChatPacketAdapter(){
			super(ChatListener.this.plugin, ListenerPriority.NORMAL, PacketType.Play.Client.CHAT);
		}
		@Override
		public void onPacketReceiving(PacketEvent event) {
			if (event.getPacketType() != PacketType.Play.Client.CHAT)
				return;
			PacketContainer packet = event.getPacket();
			String msg = packet.getStrings().read(0);
			if (Util.removeColor(msg).contains("${")){
				event.setCancelled(true);
				return;
			}
			// 命令补全，重定向 /cancel 到 cancel
			if (checkCommandPromptChat(event.getPlayer()) && msg.toLowerCase().startsWith("/cancel")){
				packet.getStrings().write(0, "cancel");
				return;
			}
			// 忽略命令
			if(msg.startsWith("/")) return;
			if(onPlayerChat(event.getPlayer(), msg)) event.setCancelled(true);
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

	@EventHandler
	public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event) {
		String msg = (event.getMessage().startsWith("/")
				? event.getMessage().substring(event.getMessage().indexOf("/") + 1)
				: event.getMessage()).toLowerCase();
		if (msg.contains(" ") && msg.substring(0, msg.indexOf(" ")).contains(":")) {
			msg = msg.substring(msg.indexOf(":") + 1);
		}
		if (msg.startsWith("marry sethome")) {
			Player player = event.getPlayer();
			Parkour p = plugin.getParkoursConfig().getParkourPlayerIn(player);
			if (p != null) {
				player.sendMessage("§7[§9末日社团§7] §c你不能在跑酷场地设置家");
				event.setCancelled(true);
			}
		}
		if (msg.startsWith("marry home")) {
			Player player = event.getPlayer();
			MPlayer mPlayer = MarriageAPI.getInstance().getMPlayer(player);
			if (mPlayer.isMarried()) {
				Location loc = mPlayer.getMarriage().getHome();
				if (plugin.getParkoursConfig().getParkourByLoc(loc) != null) {
					player.sendMessage("§7[§9末日社团§7] §c你设置的家在跑酷场地中， 无法传送");
					event.setCancelled(true);
				}
			}
		}
		if (msg.startsWith("marry tp")) {
			Player player = event.getPlayer();
			MPlayer mPlayer = MarriageAPI.getInstance().getMPlayer(player);
			if (mPlayer.isMarried()) {
				Player partner = Bukkit.getPlayer(mPlayer.getPartner().getUniqueId());
				if (partner != null && partner.isOnline()) {
					player.sendMessage("§7[§9末日社团§7] §c你的伴侣在跑酷场地，你不能传送到TA那里");
					event.setCancelled(true);
				}
			}
		}
	}

	private boolean onPlayerChat(Player player, String msg) {
		// 兼容各个插件
		if (this.checkMute(player) || this.checkAuthmeChat(player) || this.checkQuickShopChat(player)
				|| this.checkCommandPromptChat(player) || this.checkResidenceChat(player) 
				|| this.checkMcMMOChat(player) || this.checkMarriageChat(player)
				|| this.checkShopKeepersChat(player)) {
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
					this.handleChat(msg, player, -1);
			} else {
				// 否则全局聊天
				if (!isDefaultLocal)
					this.handleChat(msg, player, -1);
				else
					this.handleChat(msg.substring(1), player, localRange);
			}
		}
		// 不可附近聊天模式
		else {
			this.handleChat(msg, player, -1);
		}
		return true;
	}

	public JsonObject genChatObj(String str) {
		JsonObject result = new JsonObject();
		result.addProperty("text", str);
		return result;
	}

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
				//String json = "[{\"text\":\"" + msgs[0].replace("\\", "\\\\").replace("\"", "\\\"") + "\"},";
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

	public void handleChat(String rawMessage, Player sendPlayer, double range) {
		// 替换围在外面，避免全部&都替换成§
		String message = PlaceholderAPI.setPlaceholders(sendPlayer, range < 0 ? this.format : this.formatLocal).replace("{message}",
				Util.replaceColor(rawMessage, sendPlayer));
		Object chatText = this.handleItemDisplayString(sendPlayer, message);
		Location location = sendPlayer.getLocation();
		Bukkit.getScheduler().runTask(this.plugin, () -> {
			logger.info(message);
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (range < 0 || (location.getWorld() != null
						&& player.getWorld().getName().equals(location.getWorld().getName())
						&& location.distance(player.getLocation()) <= range)) {
					NMSUtil.sendChatPacket(player, chatText);
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
		}catch(Throwable t) {
			t.printStackTrace();
		}
		return false;
	}
}
