package top.mrxiaom.doomsdayessentials.skills;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.api.ResidenceInterface;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.api.BeamBreakBlockEvent;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;
import top.mrxiaom.doomsdayessentials.utils.SpaceUtil;

import java.util.ArrayList;
import java.util.List;

public class SakuzyoBeam implements ISkill {
	final Main plugin;

	public SakuzyoBeam(Main plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean canItemStackRunSkill(ItemStack item) {
		return ItemStackUtil.hasLore(item, "§5§2§o§e§a发射毁灭性光束");
	}

	@Override
	public void runSkill(Object data) {
		Player player = (Player) data;
		if (plugin.getPlayerCooldownManager().isBeamCooldown(player.getName())) {
			player.sendMessage("§7[§9末日社团§7] §e削除射线每 30 秒只能使用一次");
			return;
		}
		plugin.getPlayerCooldownManager().putBeam(player.getName(), 30 * 20);
		ItemStackUtil.reduceItemInMainHand(player);
		player.sendTitle("§e§o削  除  射  线", "§c§l----*SAKUZYO BEAM*---->", 5, 60, 5);
		World world = player.getWorld();
		world.playSound(player.getLocation(), Sound.BLOCK_SNOW_BREAK, 1.0F, 1.0F);
		ItemStack tool = new ItemStack(Material.IRON_PICKAXE);
		ResidenceInterface manager = ResidenceApi.getResidenceManager();
		List<String> blocks = new ArrayList<>();
		for (Block b : SpaceUtil.getSightLineBlocks(player, 16)) {
			for (int x = b.getX() - 1; x <= b.getX() + 1; x++) {
				for (int y = b.getY() - 1; y <= b.getY() + 1; y++) {
					for (int z = b.getZ() - 1; z <= b.getZ() + 1; z++) {
						Block block = world.getBlockAt(x, y, z);
						Location loc = block.getLocation();
						String locString = loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
						if (blocks.contains(locString)) {
							continue;
						}
						blocks.add(locString);
						world.spawnParticle(Particle.REDSTONE, loc, 2, new Particle.DustOptions(Color.WHITE, 200));

						// 获取领地权限状态
						ClaimedResidence res = manager.getByLoc(loc);
						boolean canAttackAnimal = res == null
								|| res.getPermissions().playerHas(player, Flags.animalkilling, false);
						boolean canAttackMonster = res == null
								|| res.getPermissions().playerHas(player, Flags.mobkilling, false);
						for (Entity e : world.getNearbyEntities(loc, 0.5D, 0.5D, 0.5D)) {
							// 忽视自己、公民 NPC 和 Shopkeepers 商店
							if (e instanceof Damageable && !e.getUniqueId().equals(player.getUniqueId())
									&& !e.hasMetadata("NPC") && !e.hasMetadata("shopkeeper")
									&& !(((e instanceof Animals) && !canAttackAnimal)
											|| ((e instanceof Monster) && !canAttackMonster))) {
								// 10 秒发光
								if (e instanceof LivingEntity) {
									((LivingEntity) e).addPotionEffect(
											new PotionEffect(PotionEffectType.GLOWING, 600, 1, true, true, true));
								}
								((Damageable) e).damage(10, player);
							}
						}
						BeamBreakBlockEvent event = new BeamBreakBlockEvent(block, player);
						Bukkit.getPluginManager().callEvent(event);
						if (!event.isCancelled()) {
							plugin.getCoreProtectApi().logRemoval(player.getName(), loc, b.getType(), b.getBlockData());
							block.breakNaturally(tool);
						}
					}
				}
			}
		}
	}

}
