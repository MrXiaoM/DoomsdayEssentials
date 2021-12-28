package top.mrxiaom.doomsdayessentials;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdayessentials.utils.I18n;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerCooldownManager {
	class GunCooldownData {
		public final String gunId;
		public final int taskId;

		public GunCooldownData(String gunId, int taskId) {
			this.gunId = gunId;
			this.taskId = taskId;
		}

		public void cancelTask() {
			Bukkit.getServer().getScheduler().cancelTask(taskId);
		}
	}

	public class TPRequest implements Runnable {
		final Player sender;
		final Player receiver;
		final boolean tpahere;
		int time;
		final int taskId;

		private TPRequest(Player sender, Player receiver, int time, boolean tpahere) {
			this.sender = sender;
			this.receiver = receiver;
			this.time = time;
			this.tpahere = tpahere;
			this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 20L, 20L);
		}

		public void accept() {
			System.out.println(sender.getName() + " 的请求被接受了");
			this.cancel();
			sender.sendMessage(I18n.t("teleport.accept", true).replace("%player%", receiver.getName()));
			receiver.sendMessage(I18n.t("teleport.accept-self", true).replace("%player%", sender.getName()));
			Player targetPlayer = (tpahere ? receiver : sender);
			Player anotherPlayer = (tpahere ? sender : receiver);
			Location target = anotherPlayer.getLocation();
			if (plugin.getParkoursConfig().getParkourByLoc(target) != null) {
				targetPlayer.sendMessage(I18n.t("teleport.parkour-self", true));
				anotherPlayer.sendMessage(I18n.t("teleport.parkour", true));
				return;
			}
			if (targetPlayer.hasPermission("doomteam.teleport.cooldown.bypass")) {
				plugin.getBackConfig().addBackPoint(targetPlayer, targetPlayer.getLocation());
				targetPlayer.teleport(target);
				targetPlayer.sendMessage(I18n.t("teleport.to", true).replace("%player%", anotherPlayer.getName()));
				return;
			}
			if (plugin.getPlayerCooldownManager().isCooldown(targetPlayer.getName())) {
				plugin.getPlayerCooldownManager().cancelPlayerCooldownTask(targetPlayer.getName());
			}
			targetPlayer.sendMessage(I18n.t("teleport-intime", true).replace("%time%", String.valueOf(3)));
			plugin.getPlayerCooldownManager().put(targetPlayer.getName(),
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
						@Override
						public void run() {
							if (plugin.getPlayerCooldownManager().isCooldown(targetPlayer.getName())) {
								plugin.getPlayerCooldownManager().cancelPlayerCooldownTask(targetPlayer.getName());
							}
							plugin.getBackConfig().addBackPoint(targetPlayer, targetPlayer.getLocation());
							targetPlayer.teleport(target);
							targetPlayer.sendMessage(I18n.t("teleport.to", true).replace("%player%", anotherPlayer.getName()));
						}
					}, 3 * 20));

		}

		public void reject() {
			this.cancel();
			sender.sendMessage(I18n.t("teleport.reject", true).replace("%player%", receiver.getName()));
			receiver.sendMessage(I18n.t("teleport.reject-self", true).replace("%player%", sender.getName()));
		}

		public void cancel() {
			Bukkit.getScheduler().cancelTask(taskId);
			tpList.remove(this);
		}

		public void cancelBySender() {
			this.cancel();
			sender.sendMessage(I18n.t("teleport.cancel-self", true).replace("%player%", receiver.getName()));
			receiver.sendMessage(I18n.t("teleport.cancel", true).replace("%player%", sender.getName()));
		}

		@Override
		public void run() {
			if (time <= 0) {
				sender.sendMessage(I18n.t("teleport-move", true));
				this.cancel();
			} else
				time--;
		}

		public Player getSender() {
			return sender;
		}

		public Player getReceiver() {
			return receiver;
		}

		public Location getTargetLoc() {
			return (tpahere ? sender : receiver).getLocation();
		}

		public int getTime() {
			return time;
		}

		public boolean isTpahere() {
			return tpahere;
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof TPRequest))
				return false;
			return this.taskId == ((TPRequest) other).taskId;
		}
	}

	public final Map<String, Integer> playerCooldown = new HashMap<String, Integer>();
	public final Map<String, Integer> randomTPCooldown = new HashMap<String, Integer>();
	public final Map<String, Integer> playerBeamCooldown = new HashMap<String, Integer>();
	public final Map<String, GunCooldownData> playerGunCooldown = new HashMap<String, GunCooldownData>();
	public final Map<String, Integer> playerRedstoneCooldown = new HashMap<String, Integer>();
	public final List<TPRequest> tpList = new ArrayList<>();
	final Main plugin;

	public PlayerCooldownManager(Main plugin) {
		this.plugin = plugin;
	}

	public boolean isInTpRequest(Player player) {
		for (TPRequest request : tpList) {
			if (request.getSender().getName().equalsIgnoreCase(player.getName())
					|| request.getReceiver().getName().equalsIgnoreCase(player.getName())) {
				return true;
			}
		}
		return false;
	}

	public List<TPRequest> getTpRequestsSender(Player sender) {
		List<TPRequest> requests = new ArrayList<>();
		for (TPRequest request : tpList) {
			if (request.getSender().getUniqueId().equals(sender.getUniqueId())) {
				requests.add(request);
			}
		}
		return requests;
	}

	public List<TPRequest> getTpRequests(Player receiver) {
		List<TPRequest> requests = new ArrayList<>();
		for (TPRequest request : tpList) {
			if (request.getReceiver().getUniqueId().equals(receiver.getUniqueId())) {
				requests.add(request);
			}
		}
		return requests;
	}

	public boolean hasTpRequest(Player sender, Player receiver) {
		return this.getTpRequest(sender, receiver) != null;
	}

	@Nullable
	public TPRequest getTpRequest(Player sender, Player receiver) {
		for (TPRequest request : tpList) {
			if (request.getSender().getName().equals(sender.getName())
					&& request.getReceiver().getName().equals(receiver.getName())) {
				return request;
			}
		}
		return null;
	}

	public void putTpRequest(Player sender, Player receiver, boolean tpahere) {
		tpList.add(new TPRequest(sender, receiver, plugin.getTpTimeout(), tpahere));
	}

	public void removeTpRequest(Player sender) {
		for (TPRequest request : tpList) {
			if (request.getSender().getUniqueId().equals(sender.getUniqueId())) {
				request.cancel();
				break;
			}
		}
	}
	public void putRandomTP(String player, int cooldown) {
		int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
		@Override
		public void run() {
			cancelRandomTPCooldownTask(player);
		}
		}, cooldown);
		this.randomTPCooldown.put(player, taskId);
	}

	public void cancelRandomTPCooldownTask(String player) {
		if (randomTPCooldown.containsKey(player)) {
			int id = randomTPCooldown.get(player);
			Bukkit.getScheduler().cancelTask(id);
			this.randomTPCooldown.remove(player);
		}
	}

	public void put(String player, Integer taskId) {
		this.playerCooldown.put(player, taskId);
	}

	public void putBeam(String player, int cooldown) {
		int taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				cancelPlayerBeamCooldownTask(player);
			}
		}, cooldown);
		this.playerBeamCooldown.put(player, taskId);
	}

	public void cancelPlayerCooldownTask(String player) {
		if (playerCooldown.containsKey(player)) {
			int id = playerCooldown.get(player);
			Bukkit.getScheduler().cancelTask(id);
			this.playerCooldown.remove(player);
		}
	}

	public void cancelPlayerBeamCooldownTask(String player) {
		if (playerBeamCooldown.containsKey(player)) {
			int id = playerBeamCooldown.get(player);
			Bukkit.getScheduler().cancelTask(id);
			this.playerBeamCooldown.remove(player);
		}
	}

	public boolean isCooldown(String player) {
		return playerCooldown.containsKey(player);
	}

	public boolean isBeamCooldown(String player) {
		return playerBeamCooldown.containsKey(player);
	}
	
	public boolean isRandomTPCooldown(String player) {
		return randomTPCooldown.containsKey(player);
	}

	public boolean isGunCooldown(String player, String gunId) {
		if (playerGunCooldown.containsKey(player)) {
			if (gunId == null)
				return false;
			return playerGunCooldown.get(player).gunId.equals(gunId);
		}
		return false;
	}

	public boolean isRedstoneCooldown(String player) {
		return playerRedstoneCooldown.containsKey(player);
	}

	public void setRedstoneCooldown(String player, int ticks) {
		if (this.isRedstoneCooldown(player)) {
			return;
		}
		playerRedstoneCooldown.put(player, Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				if(playerRedstoneCooldown.containsKey(player)) {
					Bukkit.getScheduler().cancelTask(playerRedstoneCooldown.get(player));
					playerRedstoneCooldown.remove(player);
				}
			}
		}, ticks).getTaskId());
	}
	
	public void setGunCooldown(String player, String gunId, int ticks) {
		if (this.isGunCooldown(player, gunId)) {
			return;
		}
		if (this.playerGunCooldown.containsKey(player)) {
			this.playerGunCooldown.get(player).cancelTask();
		}
		this.playerGunCooldown.put(player,
				new GunCooldownData(gunId, Bukkit.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
					@Override
					public void run() {
						playerGunCooldown.remove(player);
					}
				}, ticks).getTaskId()));
	}
}
