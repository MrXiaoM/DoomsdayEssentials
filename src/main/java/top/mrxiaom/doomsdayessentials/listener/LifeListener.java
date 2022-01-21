package top.mrxiaom.doomsdayessentials.listener;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.gmail.nossr50.events.hardcore.McMMOPlayerPreDeathPenaltyEvent;
import com.hm.achievement.category.NormalAchievements;
import com.ranull.graves.api.events.GravePlayerDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.WarpConfig.Warp;
import top.mrxiaom.doomsdayessentials.listener.OpenWorldListener.InventoryType;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;
import top.mrxiaom.doomsdayessentials.utils.NMSUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LifeListener implements Listener {
	final Main olugin;
	private final Map<String, DeathDetail> forceRespawn = new HashMap<>();
	private final List<String> banKick = new ArrayList<>();
	private final List<String> respawnTokenTemp = new ArrayList<>();
	public LifeListener(Main plugin) {
		this.olugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public String getPVPAreaRespawnWarp(ClaimedResidence res) {
		if (res == null)
			return "";
		for (String s : olugin.getConfig().getStringList("pvp-res")) {
			if (s.startsWith(res.getName() + ":")) {
				return s.substring(s.indexOf(":") + 1);
			}
		}
		return "";
	}

	public ClaimedResidence getPVPArea(Player player) {
		ClaimedResidence res = ResidenceApi.getResidenceManager().getByLoc(player.getLocation());
		if (res == null)
			return null;
		for (String s : olugin.getConfig().getStringList("pvp-res")) {
			if (s.startsWith(res.getName() + ":")) {
				return res;
			}
		}
		return null;
	}

	public boolean hasRespawnToken(Player player) {
		for (int i = 0; i < player.getInventory().getSize(); i++){
			ItemStack item = player.getInventory().getItem(i);
			if(ItemStackUtil.containsLore(item, "§5§b§e§f§a死亡时不掉落物品、技能等级和复活针")) {
				return true;
			}
		}
		return false;
	}
	public boolean removeRespawnTokenFromPlayer(Player player) {
		for (int i = 0; i < player.getInventory().getSize(); i++){
			ItemStack item = player.getInventory().getItem(i);
			if(ItemStackUtil.containsLore(item, "§5§b§e§f§a死亡时不掉落物品、技能等级和复活针")) {
				if(item.getAmount() - 1 > 0) {
					item.setAmount(item.getAmount() - 1);
					player.getInventory().setItem(i, item);
				}
				else {
					player.getInventory().setItem(i, null);
				}
				return true;
			}
		}
		return false;
	}
	
	@EventHandler
	public void onGravePlayerDeath(GravePlayerDeathEvent event) {
		if (event.getEntity() == null || !event.getEntity().getType().equals(EntityType.PLAYER))
			return;
		Player player = (Player) event.getEntity();
		if (getPVPArea(player) != null || this.hasRespawnToken(player)) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onMcMMOPlayerPreDeathPenalty(McMMOPlayerPreDeathPenaltyEvent event) {
		if (getPVPArea(event.getPlayer()) != null) {
			event.setCancelled(true);
		}
		if(respawnTokenTemp.contains(event.getPlayer().getName())) {
			event.setCancelled(true);
			respawnTokenTemp.remove(event.getPlayer().getName());
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		String worldName = p.getWorld().getName();
		// 在pvp场地死亡不扣复活针
		ClaimedResidence pvp = getPVPArea(p);
		boolean isInPvP = (pvp != null);
		if (!isInPvP) {
			olugin.getBackConfig().addBackPoint(p, p.getLocation(), true);
			
			// 判断是否需要封禁玩家
			if (olugin.getPlayerConfig().getNeedle(p.getName()) - 1 < 0) {
				Util.alert(p.getName() + "的脉搏停止了", true);
				banKick.add(p.getName());
			}
			olugin.getPlayerConfig().removeNeedle(p.getName(), 1);
			p.sendTitle("§a等你复活", "§o你怎么又死啦?", 10, 35, 10);
		} else if(this.removeRespawnTokenFromPlayer(p)){
			// 重生令牌
			olugin.getAachCacheApi().getAndIncrementStatisticAmount(NormalAchievements.DEATHS, p.getUniqueId(), -1);
			respawnTokenTemp.add(p.getName());
			// 保留背包物品和经验值
			e.setKeepInventory(true);
			e.setKeepLevel(true);
		}
		else {
			// 在pvp场地死亡，不增加成就死亡次数
			olugin.getAachCacheApi().getAndIncrementStatisticAmount(NormalAchievements.DEATHS, p.getUniqueId(), -1);
			// 保留背包物品和经验值
			e.setKeepInventory(true);
			e.setKeepLevel(true);
			p.sendTitle("§a等你复活", "§o你怎么又死啦?", 10, 35, 10);
		}
		this.forceRespawn.put(p.getName(),
				new DeathDetail(Bukkit.getScheduler().scheduleSyncDelayedTask(olugin,
						new ForceRespawn(this.olugin, p.getName()), isInPvP ? 5 : 3 * 20L), worldName,
						isInPvP ? getPVPAreaRespawnWarp(pvp) : ""));
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		event.setRespawnLocation(new Location(Bukkit.getWorld("spawn"), 192.5, 64, -64.5, 180, 0));
		
		World fromWorld = player.getLocation().getWorld();
		World toWorld = event.getRespawnLocation().getWorld();
		if(fromWorld != null && toWorld != null) {
			// 如果来自开放世界
			if (fromWorld.getName().equalsIgnoreCase(olugin.getOpenWorldListener().openWorldName)
					&& !toWorld.getName().equalsIgnoreCase(olugin.getOpenWorldListener().openWorldName)) {
				// 切换为原版背包
				olugin.getOpenWorldListener().switchInventory(event.getPlayer(), InventoryType.vanilla);
			}
			// 如果来自原版世界
			if (toWorld.getName().equalsIgnoreCase(olugin.getOpenWorldListener().openWorldName)
					&& !fromWorld.getName().equalsIgnoreCase(olugin.getOpenWorldListener().openWorldName)) {
				// 切换为开放世界背包
				olugin.getOpenWorldListener().switchInventory(event.getPlayer(), InventoryType.openworld);
			}
		}
		
		if (this.forceRespawn.containsKey(playerName)) {
			DeathDetail detail = this.forceRespawn.get(playerName);
			Bukkit.getScheduler().cancelTask(detail.taskId);
			this.forceRespawn.remove(playerName);
			if (detail.pvp.length() > 0) {
				Warp warp = olugin.getWarpConfig().get(detail.pvp);
				if (warp != null)
					event.setRespawnLocation(warp.getLocation());
			} else {
				int amount = olugin.getPlayerConfig().getNeedle(playerName);
				player.sendMessage(I18n.t("respawnneedle.death.message").replace("%amount%", String.valueOf(amount)));
				player.sendTitle(I18n.t("respawnneedle.death.title").replace("%amount%", String.valueOf(amount)),
						I18n.t("respawnneedle.death.subtitle").replace("%amount%", String.valueOf(amount)), 10, 100,
						10);
			}
		}
		if (banKick.contains(playerName)) {
			player.kickPlayer(I18n.tn("respawnneedle.death.ban-message"));
			banKick.remove(playerName);
		}
	}

	static class DeathDetail {
		final Integer taskId;
		final String worldName;
		final String pvp;

		public DeathDetail(Integer taskId, String worldName, String pvp) {
			this.taskId = taskId;
			this.worldName = worldName;
			this.pvp = pvp;
		}
	}

	static class ForceRespawn implements Runnable {
		private final Main plugin;
		private final String playerName;

		public ForceRespawn(Main plugin, String playerName) {
			this.playerName = playerName;
			this.plugin = plugin;
		}

		@Override
		public void run() {
			Player player = this.plugin.getServer().getPlayer(this.playerName);
			NMSUtil.respawnPlayer(player);
		}
	}
}
