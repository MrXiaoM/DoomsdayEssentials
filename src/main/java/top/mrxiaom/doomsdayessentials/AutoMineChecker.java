package top.mrxiaom.doomsdayessentials;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitTask;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class AutoMineChecker implements Listener{
	final Main plugin;
	private class Task implements Runnable{
		final BukkitTask task;
		final BossBar bar;
		final Player player;
		Task(Player player){
			this.player = player;
			this.bar = Bukkit.createBossBar(I18n.t("anti-automine.san-low-bossbar").replace("%percent%", "100"), BarColor.GREEN, BarStyle.SEGMENTED_10);
			this.bar.addPlayer(player);
			this.task = Bukkit.getScheduler().runTaskTimer(plugin, this, 4, 4);
		}
		private double getProgress() {
			return bar.getProgress();
		}
		public void run() {
			if(!tasks.containsKey(player.getUniqueId())) {
				if(task != null && !task.isCancelled()) {
					task.cancel();
				}
				return;
			}
			if(bar.getProgress() - 0.02D <= 0) {
				if(task != null && !task.isCancelled()) {
					task.cancel();
				}
				bar.removeAll();
				tasks.remove(player.getUniqueId());
				//player.kickPlayer("为避免出问题，暂时用踢出玩家代替杀死玩家\n如果你认为你遇到了bug请联系管理员");
				player.damage(1145141919810D);
				Util.alert(I18n.t("death.san-low").replace("%player%", player.getName()), true);
				player.sendMessage(I18n.tn("death.san-low-self-tips"));
				return;
			}
			bar.setProgress(bar.getProgress() - 0.02D);
			if(bar.getProgress() < 0.3D) 
				bar.setColor(BarColor.RED);
			else if(bar.getProgress() >= 0.3D && bar.getProgress() < 0.6D) 
				bar.setColor(BarColor.YELLOW);
			else bar.setColor(BarColor.GREEN);
			bar.setStyle(BarStyle.SEGMENTED_10);
			bar.setTitle(I18n.t("anti-automine.san-low-bossbar").replace("%percent%", String.valueOf((int)(bar.getProgress() * 100D))));
		}
		public void addProcess(double value) {
			bar.setProgress(bar.getProgress() + value);
		}
	}

	/**
	 * 反自带挖矿检查任务
	 * UUID: 玩家UUID
	 * Task: 任务实例
	 */
	private final Map<UUID, Task> tasks = new HashMap<>();
	public AutoMineChecker(Main plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		// 开放世界不触发
		if(!player.getWorld().getName().equalsIgnoreCase("openworld")
			&& !tasks.containsKey(player.getUniqueId()) && ItemStackUtil.isOre(event.getBlock().getType())) {
			if(new Random().nextInt(1000) < 950) return;
			tasks.put(player.getUniqueId(), new Task(player));
		}
	}

	@EventHandler
	public void onPlayerSneak(PlayerToggleSneakEvent event) {
		if(event.isSneaking()) {
			Player player = event.getPlayer();
			if(!tasks.containsKey(player.getUniqueId())) return;
			Task task = tasks.get(player.getUniqueId());
			if(task.getProgress() + 0.1D >= 1.0D) {
				task.bar.removeAll();
				task.task.cancel();
				player.sendTitle(I18n.t("anti-automine.san-recovered-title"), I18n.t("anti-automine.san-recovered-subtitle"), 10, 30, 10);
				tasks.remove(player.getUniqueId());
				return;
			}
			task.addProcess(0.1D);
			tasks.put(player.getUniqueId(), task);
		}
	}
	
	public float numBewteen(float a, float b) {
		return (a - b) * (a < b ? -1 : 1);
	}
}
