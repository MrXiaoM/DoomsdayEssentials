package top.mrxiaom.doomsdayessentials.skills;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import net.minecraft.server.v1_15_R1.EntityArrow;
import net.minecraft.server.v1_15_R1.EntityArrow.PickupStatus;
import net.minecraft.server.v1_15_R1.EntityTypes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;
import top.mrxiaom.doomsdayessentials.utils.SpaceUtil;

import java.util.*;

public class SpiritualCrystallization implements ISkill {
	final Main plugin;
	final int maxtime = 5 * 20;
	final Map<UUID, SkillStatus> players = new HashMap<>();

	class SkillStatus implements Runnable {
		class SkillArrow {
			final EntityArrow arrow;
			final double rotate;
			final double yOffset;
			final double distance;

			public SkillArrow(double rotate, double distance) {
				net.minecraft.server.v1_15_R1.WorldServer world = ((CraftWorld) player.getWorld()).getHandle();
				this.arrow = (EntityArrow) EntityTypes.ARROW.a(world);
				this.distance = distance;
				this.rotate = rotate;
				this.yOffset = (6.0D + new Random().nextInt(10)) / 10.0D;
				this.updateLoc();
				arrow.setShooter(((CraftPlayer) player).getHandle());
				arrow.setDamage(1.0D);
				arrow.setNoGravity(true);
				arrow.fromPlayer = PickupStatus.DISALLOWED;
				arrow.addScoreboardTag("DoomsdaySkill");
				world.addEntity(arrow);
			}

			public void updateLoc() {
				Location loc = SpaceUtil.rotateLocation(player.getLocation(), 0, yOffset, 0, distance, rotate);
				this.arrow.setLocation(loc.getX(), loc.getY(), loc.getZ(), -player.getLocation().getYaw(),
						-player.getLocation().getPitch());
			}

			public double getDamage() {
				double result = ((double) time / (double) maxtime) * 3.6D;
				if (result < 1.0D)
					return 1.0D;
				return result;
			}

			@SuppressWarnings("deprecation")
			public void shoot() {
				arrow.setNoGravity(false);
				Location loc = new Location(player.getLocation().getWorld(), arrow.locX(), arrow.locY(), arrow.locZ());
				ClaimedResidence res = ResidenceApi.getResidenceManager().getByLoc(loc);
				if (res != null) {
					if (!res.getPermissions().playerHas(player, "power", true)) {
						((CraftWorld) player.getWorld()).getHandle().removeEntity(arrow);
						player.sendMessage("§c你没有 §6power §c权限.");
						return;
					}
					if (!res.getPermissions().playerHas(player, Flags.shoot, true)) {
						((CraftWorld) player.getWorld()).getHandle().removeEntity(arrow);
						player.sendMessage("§c你没有 §6shoot §c权限.");
						return;
					}
				}
				arrow.setDamage(getDamage());
				Location targetLoc = getPlayerTarget();
				arrow.shoot(targetLoc.getX() - loc.getX(), targetLoc.getY() + 0.1 - loc.getY(),
						targetLoc.getZ() - loc.getZ(), 3.0F, 0.002F);
			}
		}

		boolean working = true;
		final Player player;
		final BossBar bossBar;
		final List<SkillArrow> arrows = new ArrayList<>();
		final int taskId;
		int time = 0;

		public SkillStatus(Player player) {
			this.player = player;
			this.bossBar = Bukkit.createBossBar(getBarTitle(0), BarColor.WHITE, BarStyle.SEGMENTED_10);
			bossBar.addPlayer(player);
			bossBar.setVisible(true);
			bossBar.setProgress(0);
			this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 1L, 1L);
		}

		public Location getPlayerTarget() {
			// TODO 获取玩家指向目标
			return player.getTargetBlock(null, 200).getLocation();
		}

		public void cancel() {
			this.working = false;
			Bukkit.getScheduler().cancelTask(taskId);
			bossBar.setVisible(false);
			bossBar.removeAll();
			players.remove(player.getUniqueId());
		}

		public void excute() {
			this.cancel();
			for (SkillArrow arrow : arrows) {
				arrow.shoot();
			}
			arrows.clear();
		}

		public void run() {
			if (!working)
				return;
			this.time = this.time + 1;
			if (time > 0 && time % 12 == 0) {
				arrows.add(new SkillArrow((new Random().nextBoolean() ? 1 : -1) * 45,
						(5.4D + new Random().nextInt(3)) / 10.0D));
			}

			if (this.time >= maxtime) {
				this.excute();
				return;
			}
			this.updateProcess(time, maxtime);

			for (SkillArrow arrow : arrows) {
				arrow.updateLoc();
			}
		}

		public void updateProcess(double now, double max) {
			bossBar.setProgress(now / max);
			bossBar.setTitle(getBarTitle((int) (now / max * 100.0D)));
		}
	}

	public SpiritualCrystallization(Main plugin) {
		this.plugin = plugin;
	}

	@SuppressWarnings("deprecation")
	public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		if (!this.canItemStackRunSkill(player.getInventory().getItemInMainHand()))
			return;
		// 开始潜行 (推送进度条，准备释放技能)
		if (event.isSneaking()) {
			ClaimedResidence res = ResidenceApi.getResidenceManager().getByLoc(player.getLocation());
			if (res != null && !res.getPermissions().playerHas(player, "power", true)) {
				player.sendMessage("§c你没有 §6power §c权限.");
				return;
			}
			if (res != null && !res.getPermissions().playerHas(player, Flags.shoot, true)) {
				player.sendMessage("§c你没有 §6shoot §c权限.");
				return;
			}
			this.players.put(player.getUniqueId(), new SkillStatus(player));
			return;
		}
		// 结束潜行 (取消或释放技能)
		if (this.players.containsKey(player.getUniqueId())) {
			this.players.get(player.getUniqueId()).excute();
			// this.players.remove(player.getUniqueId());
		}
	}

	public static String getBarTitle(int process) {
		return "§7[§b心灵结晶§7] §a正在准备技能 " + process + "% §7[§f松开Shift或到100%释放技能§7]";
	}

	@Override
	public boolean canItemStackRunSkill(ItemStack item) {
		return ItemStackUtil.hasLore(item, "§3§8§c§6§a发出自狙蓄力箭");
	}

	@Override
	public void runSkill(Object data) {

	}
}
