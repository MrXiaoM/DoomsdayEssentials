package top.mrxiaom.doomsdayessentials.listener;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.event.ResidenceCommandEvent;
import com.bekvon.bukkit.residence.event.ResidenceTPEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerExperienceEvent;
import com.google.common.eventbus.Subscribe;
import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.events.PlayerTeleportToPlotEvent;
import fr.xephi.authme.api.v3.AuthMeApi;
import net.Zrips.CMILib.Container.CMIWorld;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import think.rpgitems.power.RPGItemsPowersPreFireEvent;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.GunConfig;
import top.mrxiaom.doomsdayessentials.configs.GunConfig.Gun;
import top.mrxiaom.doomsdayessentials.configs.ParkourConfig.Parkour;
import top.mrxiaom.doomsdayessentials.gui.GuiBullet;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;
import top.mrxiaom.doomsdayessentials.utils.NMSUtil;
import top.mrxiaom.doomsdayessentials.utils.NMSUtil.NMSItemStack;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;

public class  PlayerListener implements Listener {
	final Main plugin;
	public boolean enable = true;

	public PlayerListener(Main plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	public void handleBannedItem(Player player, ItemStack... items) {
		if(items == null || items.length == 0 || player == null || player.isOp()) return;
		for(ItemStack item : items) {
			if(item == null) continue;
			RPGItem rpg = ItemManager.toRPGItem(item).orElse(null);
			if(rpg != null) {
				if(rpg.getDisplayName().contains("终焉")) {
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + player.getName() + " 要解封就去跟冷鸟鸟解释终焉之剑的来源");
					break;
				}
			}
		}
	}

	@EventHandler
	public void ban1(InventoryClickEvent event) {
		if(!(event.getWhoClicked() instanceof Player)) return;
		this.handleBannedItem((Player) event.getWhoClicked(), event.getCurrentItem());
	}
	
	@EventHandler
	public void ban2(PlayerInteractEvent event) {
		this.handleBannedItem(event.getPlayer(), event.getItem());
	}

	@EventHandler
	public void onResidenceCmd(ResidenceCommandEvent event) {
		if(event.getSender() instanceof Player) {
			Player player = (Player) event.getSender();
			String[] args = event.getArgs();
			if(args.length >= 2 && args[0].equalsIgnoreCase("tp") && ResidenceApi.getResidenceManager().getByName(args[1]) != null) {
				if(plugin.getPlayerConfig().getConfig().getBoolean(player.getName() + ".tips-while-teleport", false)) {
					player.sendMessage("§a你知道吗\n§r" + plugin.getRandomTips());
				}
			}
		}
	}
	
	@EventHandler
	public void onResidenceTP(ResidenceTPEvent event) {
		Player player = event.getPlayer();
		Location loc = player.getLocation();
		if (plugin.getParkoursConfig().getParkourByLoc(loc) == null) {
			plugin.getBackConfig().addBackPoint(player, loc);
		}
	}

	@EventHandler
	public void onPlayerSneak(PlayerToggleSneakEvent event) {
		plugin.getSkillSpiritualCrystallization().onPlayerToggleSneak(event);
	}

	@EventHandler
	public void onPlayerItemDamage(PlayerItemDamageEvent event) {
		// 在PVP场地武器不掉耐久
		if (plugin.getLifeListener().getPVPArea(event.getPlayer()) != null) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		if (event.getCause().equals(TeleportCause.ENDER_PEARL)) {
			Player player = event.getPlayer();
			if (plugin.getParkoursConfig().getParkourPlayerIn(player) != null) {
				player.sendMessage("§c你没有 §6enderpearl §c权限.");
				event.setCancelled(true);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onRPGItemsPrePower(RPGItemsPowersPreFireEvent<?, ?, ?, ?> event) {
		Location loc = event.getPlayer().getLocation();
		ClaimedResidence res = plugin.getResidenceApi().getByLoc(loc);
		if (res != null && !res.getPermissions().playerHas(event.getPlayer(), "power", true)) {
			event.getPlayer().sendMessage("§c你没有 §6power §c权限.");
			event.setCancelled(true);
			return;
		}
		if (plugin.getParkoursConfig().getParkourByLoc(loc) != null) {
			event.getPlayer().sendMessage("§c你没有 §6power §c权限.");
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
		Player player = event.getPlayer();
		int old = event.getOldLevel();
		int now = event.getNewLevel();
		double health = 40.0D;
		if (now < 40) {
			health = 20.0D + (20.0D * now / 40.0D);
		}
		// 保留一位小数
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(1);
		nf.setRoundingMode(RoundingMode.UP);
		health = Double.parseDouble(nf.format(health));
		AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
		if(maxHealth != null) maxHealth.setBaseValue(health);
		// 升级之前在四十级以下 或 降级之后在40级以下 时进行通知
		if ((now > old && old < 40) || (now < old && now < 40)) {
			player.sendMessage("§7[§9末日社团§7] §6你的等级" + (now > old ? "§a提升" : "§c下降") + "§6到 " + now + " 了， "
					+ "你的生命值上限已" + (now > old ? "§a提升" : "§c下降") + "§6至 §c" + health);
		}
	}

	@EventHandler
	public void onPlayerHeal(EntityRegainHealthEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (player.isOnline() && !player.isDead()) {
				if (player.getHealth() - event.getAmount() <= 4) {
					if (plugin.getPlayerConfig().getConfig().getBoolean(player.getName() + ".curse", false)) {
						player.sendTitle("§5诅咒", "§7§l§o但这需要付出代价…", 20, 60, 20);
						player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 0, true));
						player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 100, 2, true));
						player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 0, true));
					}
				}
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			if (player.isOnline() && !player.isDead()) {
				if (player.getHealth() + event.getFinalDamage() > 4) {
					if (plugin.getPlayerConfig().getConfig().getBoolean(player.getName() + ".curse", false)) {
						player.sendTitle("§5诅咒", "§c§l§o禁忌神正给予你力量", 20, 60, 20);
						player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 30, 4, true));
					}
				}
			}
		}
		if (event.getCause() == DamageCause.ENTITY_ATTACK || event.getCause() == DamageCause.ENTITY_SWEEP_ATTACK) {
			if (event.getDamager() instanceof Player) {
				Player damager = (Player) event.getDamager();
				if (plugin.getSkillIAmNoob().canItemStackRunSkill(
						damager.getInventory().getItemInMainHand())){
					plugin.getSkillIAmNoob().onAttack(event);
				}
				if (plugin.getSkillSelfAttack().canItemStackRunSkill(
						damager.getInventory().getItemInMainHand())) {
					plugin.getSkillSelfAttack().runSkill(event);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerHurt(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player
		    && AuthMeApi.getInstance().isAuthenticated((Player)event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		boolean isRightClicked = event.getAction().equals(Action.RIGHT_CLICK_AIR)
				|| event.getAction().equals(Action.RIGHT_CLICK_BLOCK);
		Player player = event.getPlayer();
		// 地狱基岩层上方禁止建筑
		if (player.getWorld().getEnvironment().equals(Environment.NETHER)) {
			if (event.getClickedBlock() != null) {
				if (event.getClickedBlock().getY() >= 126) {
					player.sendMessage("§c你没有 §6build §c权限.");
					event.setCancelled(true);
					return;
				}
			}
		}
		if (!event.hasItem()) {
			return;
		}
		ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
		if (ItemStackUtil.hasLore(itemInMainHand, "§d§f§c§l§a重置你的摔落高度")) {
			ItemStackUtil.reduceItemInMainHand(player);
			player.setFallDistance(0.0F);
			player.sendTitle("§a从天而降", "§e摔落高度已重置", 10, 30, 10);
			return;
		}
		if (plugin.getSkillSakuzyoBeam().canItemStackRunSkill(player.getInventory().getItemInMainHand())) {
			plugin.getSkillSakuzyoBeam().runSkill(player);
			return;
		}
		if (plugin.getSkillIAmNoob().canItemStackRunSkill(player.getInventory().getItemInMainHand())) {
			plugin.getSkillIAmNoob().runSkill(player);
			return;
		}
		ItemStack item = event.getItem();
		if(item == null) return;
		String displayName = ItemStackUtil.getItemDisplayName(item);
		List<String> lore = ItemStackUtil.getItemLore(item);
		//ItemMeta im = item.getItemMeta();
		if (lore.isEmpty()) {
			return;
		}
		String s = lore.get(lore.size() - 1).toLowerCase();
		// 塔罗牌
		if (s.toLowerCase().startsWith("§t§a§r§o§t")) {
			if (isRightClicked) {
				event.setCancelled(true);
				String card = ChatColor.translateAlternateColorCodes('&', s.substring(10));
				if (event.getHand() != null && event.getHand().equals(EquipmentSlot.HAND)) {
					player.getInventory().setItemInMainHand((item.getAmount() - 1 > 0) ? item : null);
				}
				if (event.getHand() != null && event.getHand().equals(EquipmentSlot.OFF_HAND)) {
					player.getInventory().setItemInOffHand((item.getAmount() - 1 > 0) ? item : null);
				}
				this.useTarotCard(player, card);
			}
		}
		// 枪械
		if (s.toLowerCase().startsWith("§g§u§n")) {
			if (event.getHand() != null && !event.getHand().equals(EquipmentSlot.HAND)) {
				return;
			}
			if ((event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK))
					&& player.isSneaking()) {
				event.setCancelled(true);
				plugin.getGuiManager().openGui(new GuiBullet(plugin, player));
				return;
			}
			if (event.getAction().equals(Action.RIGHT_CLICK_AIR)
					|| event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {

				event.setCancelled(true);
				String id = s.replace("§", "").substring(3);
				
				NMSItemStack nms = NMSItemStack.fromBukkitItemStack(item);
				if (nms == null) return;
				int bullets = nms.getNBTTagInt("bullets", 0);
				if (bullets < 1) {
					player.sendMessage("§7[§9末日社团§7]§c 你的枪里没有子弹了 §7(Shift+左键填充子弹)");
					return;
				}
				Gun gun = plugin.getGunConfig().get(id);
				if (gun == null) {
					player.sendMessage("§7[§9末日社团§7]§c 错误的枪械");
					return;
				}
				if (plugin.getPlayerCooldownManager().isGunCooldown(player.getName(), id)) return;
				plugin.getPlayerCooldownManager().setGunCooldown(player.getName(), id, gun.getDelay());
				bullets--;
				nms.setNBTTagInt("bullets", bullets);
				nms.setDamage(0);
				player.getInventory().setItemInMainHand(nms.toBukkitItemStack());
				this.shoot(player, gun.getDamage(), gun.getSpeed(), gun.getSpread(), gun.getSound(), gun.getVolume(),
						gun.getPitch());
				Main.showPlayerBullets(player, displayName, bullets);
			}
		}
	}

	public void shoot(Player player, float speed, float damage, float spread, String sound, float volume, float pitch) {
		Location loc = player.getEyeLocation();
		Vector velocity = loc.getDirection();
		Arrow arrow = player.getWorld().spawnArrow(player.getEyeLocation(), velocity, speed, spread);
		arrow.setDamage(damage);
		arrow.addAttachment(plugin);
		arrow.setShooter(player);
		arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
		arrow.setColor(Color.RED);
		if (sound != null) {
			player.getWorld().playSound(loc, Sound.valueOf(sound), volume, pitch);
		}
	}

	// 枪械射击 主要代码
	// Author: 懒怠的小猫
	// 已 NMS 化
	@Deprecated
	public void shoot_OLD(Player player, float speed, float damage, float spread, String sound, float volume, float pitch) {
		try {
			String nms = NMSUtil.getNMSVersion();
			Class<?> classCraftServer = Class.forName("org.bukkit.craftbukkit." + nms + ".CraftWorld");
			Class<?> classCraftPlayer = Class.forName("org.bukkit.craftbukkit." + nms + ".entity.CraftPlayer");
			Class<?> classWorld = Class.forName("net.minecraft.server." + nms + ".World");
			Class<?> classWorldServer = Class.forName("net.minecraft.server." + nms + ".WorldServer");
			Class<?> classEntity = Class.forName("net.minecraft.server." + nms + ".Entity");
			Class<?> classEntityTypes = Class.forName("net.minecraft.server." + nms + ".EntityTypes");
			Class<?> classEntityArrow = Class.forName("net.minecraft.server." + nms + ".EntityArrow");
			Class<?> classPickupStatus = classEntityArrow.getDeclaredClasses()[0];
			Field fieldArrow = classEntityTypes.getDeclaredField("ARROW");
			Method spawnEntity = classEntityTypes.getDeclaredMethod("a", classWorld);
			Method addEntity = classWorldServer.getDeclaredMethod("addEntity", classEntity);
			Method getHandleWorld = classCraftServer.getDeclaredMethod("getHandle");
			Method getHandlePlayer = classCraftPlayer.getDeclaredMethod("getHandle");
			Method setPositionRotation = classEntity.getDeclaredMethod("setPositionRotation", 
					double.class, double.class, double.class, float.class, float.class);
			Method setShooter = classEntityArrow.getDeclaredMethod("setShooter", classCraftPlayer);
			Method setDamage = classEntityArrow.getDeclaredMethod("setDamage", double.class);
			Field fieldFromPlayer = classEntityArrow.getDeclaredField("fromPlayer");
			Method addScoreboardTag = classEntity.getDeclaredMethod("addScoreboardTag", String.class);
			Method shoot = classEntityArrow.getDeclaredMethod("shoot", 
					double.class, double.class, double.class, float.class, float.class);
			// EntityTypes.ARROW
			Object typeArrow = fieldArrow.get(null);
			// ((CraftWorld) player.getWorld()).getHandle()
			Object nmsWorld = getHandleWorld.invoke(player.getWorld());
			// ((Player) player).getHandle()
			Object nmsPlayer = getHandlePlayer.invoke(player);
			// nmsWorld.a(EntityTypes.ARROW)
			Object arrow = spawnEntity.invoke(typeArrow, nmsWorld);
			// PickupStatus.DISALLOWED
			Object enumDisAllowed = Util.valueOfForce(classPickupStatus, "DISALLOWED");

			Location loc = player.getEyeLocation();
			Vector velocity = loc.getDirection();

			// arrow.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
			setPositionRotation.invoke(arrow, loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
			// arrow.setShooter(nmsPlayer);
			setShooter.invoke(arrow, nmsPlayer);
			// arrow.setDamage(damage);
			setDamage.invoke(arrow, damage);
			// arrow.fromPlayer = PickupStatus.DISALLOWED;
			fieldFromPlayer.set(arrow, enumDisAllowed);
			// arrow.addScoreboardTag("DoomsdayGuns");
			addScoreboardTag.invoke(arrow, "DoomsdayGuns");
			// arrow.shoot(velocity.getX, velocity.getY() + 0.1F, velocity.getZ(), seed, spread);
			shoot.invoke(arrow, velocity.getX(), velocity.getY() + 0.1F, velocity.getZ(), speed, spread);
			// 默认音效: ENTITY_ZOMBIE_BREAK_WOODEN_DOOR
			if (sound != null) {
				player.getWorld().playSound(loc, Sound.valueOf(sound), volume, pitch);
			}
			// nmsWorld.addEntity(arrow);
			addEntity.invoke(nmsWorld, arrow);
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	// TODO 完成全套塔罗牌
	public void useTarotCard(Player player, String card) {
		if (card.equalsIgnoreCase("0")) {
			// 瞬间回城
			plugin.getBackConfig().addBackPoint(player, player.getLocation());
			player.teleport(new Location(Bukkit.getWorld("spawn"), 192.5, 64.0, -64.5));
			player.sendTitle("§e0§7 - §e愚者", "§o回到旅途开始的地方", 10, 100, 10);
			return;
		}
		if (card.equalsIgnoreCase("1")) {
			// 发送多个伤害为1的雪球锁定最近的目标
			player.sendTitle("§eI§7 - §e魔术师", "§o锁定距离你最近的目标", 10, 100, 10);
		}
		if (card.equalsIgnoreCase("2")) {
			// 召唤怪物对附近8格目标攻击一次
			// 若附近无目标则攻击自己
			player.sendTitle("§eII§7 - §e女祭司", "§o对你最近的目标造成大量伤害", 10, 100, 10);
		}
		if (card.equalsIgnoreCase("3")) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15 * 20, 2));
			player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 15 * 20, 2));
			// 速度+力量药水效果
			player.sendTitle("§eIII§7 - §e女皇", "§o临时提升你的移速和力量", 10, 100, 10);
			return;
		}
		if (card.equalsIgnoreCase("4")) {
			// 传送到 BOSS 房间
			player.sendTitle("§eIV§7 - §e皇帝", "§o挑战我!", 10, 100, 10);
		}
		if (card.equalsIgnoreCase("5")) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 30 * 20, 4));
			// 获得伤害吸收效果
			player.sendTitle("§eV§7 - §e教皇", "§o愿你的心灵被洗涤", 10, 100, 10);
			return;
		}
		if (card.equalsIgnoreCase("6")) {
			AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
			// 治疗4生命
			player.setHealth((Math.min(player.getHealth() + 4, maxHealth != null ? maxHealth.getValue() : 20)));
			player.sendTitle("§eVI§7 - §e恋人", "§o有情人终成眷属", 10, 100, 10);
			return;
		}
		if (card.equalsIgnoreCase("7")) {
			// 无敌
			player.setNoDamageTicks(15 * 20);
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 15 * 20, 12));
			player.sendTitle("§eVII§7 - §e战车", "§o没有人可以拦得住你", 10, 100, 10);
			return;
		}
		if (card.equalsIgnoreCase("8")) {
			// 治疗4生命，+100新币
			AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
			// 治疗4生命
			player.setHealth((Math.min(player.getHealth() + 4, maxHealth != null ? maxHealth.getValue() : 20)));
			plugin.getEcoApi().depositPlayer(player, 100);
			player.sendTitle("§eVIII§7 - §e正义", "§o末日里还存在正义吗?", 10, 100, 10);
			return;
		}
		if (card.equalsIgnoreCase("9")) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 30 * 20, 1, false, false));
			// 隐身药水效果
			player.sendTitle("§eIX§7 - §e隐士", "§o隐藏与世，销声匿迹", 10, 100, 10);
			return;
		}
		if (card.equalsIgnoreCase("10")) {
			ItemStack im = player.getInventory().getItemInOffHand();
			if (im.getType() != Material.AIR) {
				player.getWorld().dropItem(player.getLocation(), im);
			}
			player.getInventory().setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING, 1));
			// 将副手替换成不死图腾
			player.sendTitle("§eX§7 - §e命运之轮", "§o再来一次", 10, 100, 10);
			return;
		}
		if (card.equalsIgnoreCase("11")) {
			AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
			player.setHealth((maxHealth != null ? maxHealth.getValue() : 20) / 3 * 2);
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 15 * 20, 2));
			player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 15 * 20, 3));
			player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 8 * 20, 1));
			// 血量回满，获得速度、力量和生命恢复效果
			player.sendTitle("§eXI§7 - §e力量", "§o不要迷失其中", 10, 100, 10);
			return;
		}
		if (card.equalsIgnoreCase("12")) {
			// 短暂飞行
			player.sendTitle("§eXII§7 - §e倒吊人", "§o这只是一个考验", 10, 100, 10);
		}
		if (card.equalsIgnoreCase("13")) {
			for (Entity e : player.getNearbyEntities(16, 16, 16)) {
				if (e instanceof Damageable) {
					((Damageable) e).damage(15, player);
				}
			}
			// 对附近16格距离所有生物造成大量伤害
			player.sendTitle("§eXIII§7 - §e死亡", "§o全部都毁灭吧", 10, 100, 10);
			return;
		}
		if (card.equalsIgnoreCase("14")) {
			player.damage(10);
			plugin.getEcoApi().depositPlayer(player, 500);
			// 扣除10血量换取 500 新币
			player.sendTitle("§eXIV§7 - §e节制", "§o愿你内心纯净", 10, 100, 10);
			return;
		}
		if (card.equalsIgnoreCase("15")) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 15 * 20, 4));
			// 获得力量V效果
			player.sendTitle("§eXV§7 - §e恶魔", "§o下界之力", 10, 100, 10);
			return;
		}
		if (card.equalsIgnoreCase("16")) {
			// 在玩家周围召唤点燃的TNT
			player.sendTitle("§eXVI§7 - §e塔", "§o爆炸就是艺术", 10, 100, 10);
		}
		if (card.equalsIgnoreCase("17")) {
			// 随机给予玩家一件道具
			player.sendTitle("§eXVII§7 - §e星星", "§o引导你向目标进发", 10, 100, 10);
		}
		if (card.equalsIgnoreCase("18")) {
			// 随机给予玩家一件道具
			player.sendTitle("§eXVIII§7 - §e月亮", "§o找回你失去的东西", 10, 100, 10);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		if (!enable || plugin == null)
			return;
		Player player = event.getPlayer();
		Location from = event.getFrom();
		Location to = event.getTo();
		if(to == null) return;
		// 虚空令牌
		if(!player.isFlying() && to.getY() < 0) {
			for(int i = 0; i < player.getInventory().getSize(); i++) {
				ItemStack item = player.getInventory().getItem(i);
				if(ItemStackUtil.hasLore(item, "§b§a§6§8§3§a掉入虚空时消耗一个道具将你传送回城")
					|| ItemStackUtil.hasLore(item, "§d§e§0§mvoid_token")) {
					if (item.getAmount() - 1 <= 0) {
						player.getInventory().setItem(i, null);
					} else {
						item.setAmount(item.getAmount() - 1);
						player.getInventory().setItem(i, item);
					}
					player.setNoDamageTicks(60);
					player.setFallDistance(0);
					AttributeInstance maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
					player.setHealth(maxHealth != null ? maxHealth.getValue() : 20);
					player.teleport(new Location(Bukkit.getWorld("spawn"), 192.5, 64, -64.5, 180, 0));
					player.sendTitle("§9§o虚无之境", "§d你已被送回主城", 10, 40, 10);
					break;
				}
			}
		}
		// 传送 3 秒等待，移动时的取消
		if (from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY()
				|| from.getBlockZ() != to.getBlockZ()) {
			if (plugin.getPlayerCooldownManager().isCooldown(player.getName())) {
				plugin.getPlayerCooldownManager().cancelPlayerCooldownTask(player.getName());
				player.sendMessage(I18n.t("teleport-move"));
				return;
			}
		}
		// 跑酷相关
		Parkour p = plugin.getParkoursConfig().getParkourPlayerIn(player);
		if (p != null) {
			if (to.getBlockY() < p.getMinY()) {
				Location home = p.getResidence().getTeleportLocation(event.getPlayer());
				if (home.getY() > p.getMinY()) {
					event.getPlayer().setFallDistance(0.0F);
					event.setTo(home);
					player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 60, 4));
					player.sendTitle("§a欢迎回家", "§e梦开始的地方", 10, 40, 10);
				}
				return;
			}
			if(!player.isOp() && (player.getAllowFlight() || player.isFlying())) {
				player.setFlying(false);
				player.setAllowFlight(false);
				player.sendTitle("§cDon't cheat", "§e§o跑酷场地禁止飞行", 10, 40, 10);
				event.setCancelled(true);
				return;
			}
			if (player.getInventory().getChestplate() != null
					&& player.getInventory().getChestplate().getType() == Material.ELYTRA) {
				player.sendTitle("§c不讲武德", "§e§o请脱下鞘翅", 5, 40, 5);
				// event.setTo(from);
				// 穿鞘翅直接送回家，不多bb
				event.getPlayer().setFallDistance(0.0F);
				event.setTo(p.getResidence().getTeleportLocation(event.getPlayer()));
				event.setCancelled(true);
				return;
			}
			// 进度记录
			int checkpoint = plugin.getPlayerConfig().getConfig().getInt(event.getPlayer().getName() + ".parkours." + p.getId() + ".checkpoint", -1);
			List<String> checkpoints = p.getCheckPoints();
			if (checkpoint + 1 < checkpoints.size()) {
				String next = checkpoints.get(checkpoint + 1);
				if (next.equals(to.getBlockX() + "," + to.getBlockY() + "," + to.getBlockZ())) {
					plugin.getPlayerConfig().getConfig().set(
							event.getPlayer().getName() + ".parkours." + p.getId() + ".checkpoint", checkpoint + 1);
					plugin.getPlayerConfig().saveConfig();
					if (checkpoint + 2 >= checkpoints.size()) {
						event.getPlayer().sendTitle("§aCongratulation!", "§d你到达了 §b" + p.getDisplayName() + " §d的终点",
								10, 40, 10);
						p.processWinCommands(event.getPlayer());
						return;
					}
					event.getPlayer().sendTitle("§a已记录", "§o你已触发记录点", 10, 40, 10);
					event.getPlayer().sendMessage("§7[§9末日社团§7] §a你已触发记录点");
				}
			}
		}
	}

	@EventHandler
	public void onMcmmoExp(McMMOPlayerExperienceEvent event) {
		// 不允许使用枪械提升箭术
		if (event.getSkill() != PrimarySkillType.ARCHERY)
			return;
		Player player = event.getPlayer();
		ItemStack item = player.getInventory().getItemInMainHand();
		ItemMeta im = item.getItemMeta();
		if (im == null || !im.hasLore() || im.getLore() == null)
			return;
		String s = im.getLore().get(im.getLore().size() - 1).toLowerCase();
		if (!s.toLowerCase().startsWith("§g§u§n"))
			return;
		String id = s.substring(6).contains(";") ? s.substring(6).split(";")[0] : s.substring(6);
		GunConfig config = plugin.getGunConfig();
		if (!config.contains(id))
			return;
		event.setCancelled(true);
	}
}
