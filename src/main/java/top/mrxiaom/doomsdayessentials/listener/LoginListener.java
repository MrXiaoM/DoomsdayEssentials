package top.mrxiaom.doomsdayessentials.listener;

import fr.xephi.authme.api.v3.AuthMeApi;
import fr.xephi.authme.events.LoginEvent;
import me.albert.amazingbot.AmazingBot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.KitConfig.Kit;
import top.mrxiaom.doomsdayessentials.configs.PlayerConfig.LastSignInfo;
import top.mrxiaom.doomsdayessentials.configs.PlayerConfig.SignTime;
import top.mrxiaom.doomsdayessentials.configs.TagConfig;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;
import top.mrxiaom.pluginupdater.PlayerOfflineEvent;

public class LoginListener implements Listener {
	final Main plugin;
	public LoginListener(Main plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String prefix = this.plugin.getTagConfig().getPlayerTag(player.getName()).replace("&", "§");
		int id = TagConfig.extractId(prefix);
		if (id >= 1) {
			if (this.plugin.getTagConfig().hasTag(player, id)) {
				String a = this.plugin.getTagConfig().getTagFromID(id);
				if (!a.equals(prefix)) {
					this.plugin.getTagConfig().setPlayerTag(player, a);
					return;
				}
				return;
			}
			this.plugin.getTagConfig().setDefaultTag(player);
		}
		if (!AuthMeApi.getInstance().isRegistered(player.getName())) {
			try {
				LastSignInfo info = plugin.getPlayerConfig().getLastSignInfo(player.getName(), "xs");
				if (info.signTime == null || info.times < 1) {
					Kit kit = plugin.getKitConfig().get("xs");
					if(kit == null) throw new NullPointerException("礼包不存在");
					plugin.getPlayerConfig().setSign(player.getName(), "xs", SignTime.getNowTime()).saveConfig();
					player.getInventory().addItem(kit.getItems());
				}
			} catch (Throwable t) {
				t.printStackTrace();
				player.sendMessage("§c" + t.getMessage());
				player.sendMessage("§c§l末日社团 §7>> 在给予你新手礼包时出错，以上是错误信息，请联系服务器管理员");
			}
		}
		plugin.checkPoints(player);
	}

	@EventHandler
	public void onAuthmeLogin(LoginEvent e) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			String qq = plugin.getWhitelistConfig().getPlayerBindQQ(e.getPlayer().getName());
			String uuid = e.getPlayer().getUniqueId().toString();
			if (qq.length() > 0) {
				AmazingBot.getData().getConfig().set(qq, uuid);
				AmazingBot.getData().save();
			}
		});
		Util.updateHealth(e.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerJoin(PlayerLoginEvent e) {
		Player p = e.getPlayer();
		System.out.println(p.getName() + " 正在进入服务器");
		if (AuthMeApi.getInstance().isRegistered(p.getName()) || plugin.getWhitelistConfig().isSavedPlayer(p.getName())) {
			return;
		}
		System.out.println(p.getName() + " 无白名单，正在踢出");
		final StringBuilder sb = new StringBuilder();
		for (final String s : plugin.getConfig().getStringList("join_tip")) {
			sb.append(s).append("\n");
		}
		e.setKickMessage(sb.toString().replace("&", "§"));
		p.kickPlayer(sb.toString().replace("&", "§"));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		String player = event.getName();
		System.out.println(event.getName() + " 正在预进入服务器");
		// 已注册或已绑定
		if (AuthMeApi.getInstance().isRegistered(player) || plugin.getWhitelistConfig().isSavedPlayer(player)) {
			// 无复活针踢出
			if (plugin.getPlayerConfig().getNeedle(player) < 0) {
				event.setLoginResult(Result.KICK_BANNED);
				event.disallow(Result.KICK_BANNED, I18n.tn("respawnneedle.death.ban-message"));
			}
			return;
		}
		// 无白名单踢出
		System.out.println(player + " 无白名单，正在踢出");
		final StringBuilder sb = new StringBuilder();
		for (final String s : plugin.getConfig().getStringList("join_tip")) {
			sb.append(s).append("\n");
		}
		event.setKickMessage(sb.toString().replace("&", "§"));
		event.setLoginResult(Result.KICK_WHITELIST);
		event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, sb.toString().replace("&", "§"));
	}
	@EventHandler
	public void onPlayerOffline(PlayerOfflineEvent event) {
		// 保存玩家IP地址
		this.plugin.getPlayerConfig().set(event.getPlayer().getName() + ".ip-address", event.getIp()).saveConfig();
	}
}
