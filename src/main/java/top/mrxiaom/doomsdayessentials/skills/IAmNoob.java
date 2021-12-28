package top.mrxiaom.doomsdayessentials.skills;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import top.mrxiaom.doomsdayessentials.Main;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class IAmNoob implements ISkill{
	final Main plugin;
	class NoobSkill implements Runnable{
		boolean enable = true;
		final Player player;
		final BukkitTask task;
		final BossBar bar;
		double damageProbaby = 0.05D;
		int ticks = 0;
		private NoobSkill(Player player) {
			this.player = player;
			if(player.hasPotionEffect(PotionEffectType.CONFUSION)) {
				player.removePotionEffect(PotionEffectType.CONFUSION);
			}
			player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 180, 5));
			this.bar = Bukkit.createBossBar("§b菜鸟附体 §c当前真伤几率: §e5.0%", BarColor.GREEN, BarStyle.SOLID);
			this.bar.setProgress(1.0);
			this.bar.addPlayer(player);
			this.task = Bukkit.getScheduler().runTaskTimer(plugin, this, 1, 1);
		}
		private double getDP() {
			double i = (ticks / 160.0D);
			if(i > 1.0D) i = 1.0;
			return 0.05D + 0.35 * i;
		}
		public void run() {
			if(!enable) return;
			this.damageProbaby = getDP();
			this.bar.setProgress(1.0D - ticks / 180.0D);
			this.bar.setTitle("§b菜鸟附体 §c当前真伤几率: §e" + String.format("%.1f", this.damageProbaby) + "%");
			ticks++;
			if(ticks >= 180) {
				this.enable = false;
				this.task.cancel();
				this.bar.removeAll();
				IAmNoob.this.players.remove(player.getUniqueId());
			}
		}
	}
	class NoobSkillAttackCooldown implements Runnable{
		boolean enable = true;
		final Player player;
		final BukkitTask task;
		final BossBar bar;
		int ticks = 0;
		int maxTicks = 40;
		private NoobSkillAttackCooldown(Player player, int maxTicks) {
			this.player = player;
			this.maxTicks = maxTicks;
			this.bar = Bukkit.createBossBar("§b菜鸟附体 §f真伤触发冷却中", BarColor.WHITE, BarStyle.SOLID);
			this.bar.setProgress(1.0);
			this.bar.addPlayer(player);
			this.task = Bukkit.getScheduler().runTaskTimer(plugin, this, 1, 1);
		}
		public void run() {
			if(!enable) return;
			this.bar.setProgress(1.0D - ticks / maxTicks);
			ticks++;
			if(ticks >= maxTicks) {
				this.enable = false;
				this.task.cancel();
				this.bar.removeAll();
				IAmNoob.this.attackCooldown.remove(player.getUniqueId());
			}
		}
	}
	class NoobSkillCooldown implements Runnable{
		final Player player;
		final BukkitTask task;
		private NoobSkillCooldown(Player player, int cooldownTicks) {
			this.player = player;
			this.task = Bukkit.getScheduler().runTaskLater(plugin, this, cooldownTicks);
		}
		public void run() {
			this.task.cancel();
			IAmNoob.this.cooldown.remove(player.getUniqueId());
		}
	}
	final Map<UUID, NoobSkill> players = new HashMap<>();
	final Map<UUID, NoobSkillCooldown> cooldown = new HashMap<>();
	final Map<UUID, NoobSkillAttackCooldown> attackCooldown = new HashMap<>();
	public IAmNoob(Main plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean canItemStackRunSkill(ItemStack item) {
		RPGItem rpg = ItemManager.toRPGItem(item).orElse(null);
		return rpg != null && rpg.getName().equalsIgnoreCase("salted_fish");
	}

	@Override
	public void runSkill(Object data) {
		Player player = (Player) data;
		if(cooldown.containsKey(player.getUniqueId()) || players.containsKey(player.getUniqueId())) {
			player.sendMessage("§7[§9末日社团§7] §c技能正在冷却中");
			return;
		}
		players.put(player.getUniqueId(), new NoobSkill(player));
	}
	
	public void onAttack(EntityDamageByEntityEvent event) {
		if(!(event.getEntity() instanceof LivingEntity)) return;
		Player player = (Player) event.getDamager();
		if(attackCooldown.containsKey(player.getUniqueId())) return;
		RPGItem rpg = ItemManager.toRPGItem(player.getInventory().getItemInMainHand()).orElse(null);
		if(rpg == null || !rpg.getName().equalsIgnoreCase("salted_fish")) return;
		
		LivingEntity entity = (LivingEntity) event.getEntity();
		if(players.containsKey(player.getUniqueId())) {
			NoobSkill skill = players.get(player.getUniqueId());
			if (new Random().nextInt(1000) > (1.0D - skill.damageProbaby) * 1000) {
				attackCooldown.put(player.getUniqueId(), new NoobSkillAttackCooldown(player, 40));
				event.setCancelled(true);
				entity.setLastDamageCause(new EntityDamageByEntityEvent(player, entity, event.getCause(), rpg.getDamageMin()));
				entity.setHealth(entity.getHealth() - rpg.getDamageMin());
			}
		}
	}
	
}
