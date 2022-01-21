package top.mrxiaom.doomsdayessentials.listener;

import de.Keyle.MyPet.api.event.MyPetActiveSkillEvent;
import de.Keyle.MyPet.api.event.MyPetActiveTargetSkillEvent;
import de.Keyle.MyPet.api.event.MyPetCallEvent;
import de.tr7zw.nbtapi.NBTCompound;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.ParkourConfig.Parkour;
import top.mrxiaom.doomsdayessentials.configs.SkullConfig.Skull;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.Collection;
import java.util.Objects;
import java.util.Random;

public class EntityListener implements Listener {
	final Main plugin;

	public EntityListener(Main plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onEntityDead(EntityDeathEvent event) {
		LivingEntity entity = event.getEntity();
		if (entity.hasMetadata("MyPet") || entity.hasMetadata("NPC") || entity.hasMetadata("MythicMobs")
				|| entity.hasMetadata("shopkeeper"))
			return;
		EntityType entityType = entity.getType();
		Player killer = entity.getKiller();
		// 盔甲架 & 原版能掉头的生物不处理
		if (killer == null || entityType == EntityType.ARMOR_STAND
				|| entityType == EntityType.ZOMBIE || entityType == EntityType.SKELETON
				|| entityType == EntityType.WITHER_SKELETON || entityType == EntityType.CREEPER)
			return;
		double playerChange = plugin.getSkullConfig().getPlayerHeadChance();
		RPGItem rpg = ItemManager.toRPGItem(killer.getInventory().getItemInMainHand()).orElse(null);
		// 斩首剑额外掉头概率
		if(rpg != null && rpg.getName().equalsIgnoreCase("beheading_sword")) {
			playerChange += 0.003;
		}
		if (entityType.equals(EntityType.PLAYER)) {
			// 斩首剑击杀玩家恢复耐久
			if(rpg != null && rpg.getName().equals("beheading_sword")) {
				ItemStack item = killer.getInventory().getItemInMainHand();
				int d = rpg.getItemStackDurability(item).orElse(0);
				int max = rpg.getMaxDurability();
				if(d + 60 > max) d = max; else d += 60;
				rpg.setItemStackDurability(item, d);
				killer.getInventory().setItemInMainHand(item);
			}
			// 随机掉头
			if(playerChange > Math.random()) {
				givePlayerSkull(killer, (Player) entity);
				return;
			}
		}
		Skull entitySkull = this.plugin.getSkullConfig().getSkull(entityType);
		if (entitySkull != null && entitySkull.getChange() >= Math.random()) {
			ItemStack skull = entitySkull.getItemStack();
			event.getDrops().add(skull);
			String dropMobHead = I18n.t("head.drop-mob", true);
			plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&', dropMobHead
					.replace("%player%", killer.getName())
					.replace("%target%", Util.removeColor(entitySkull.getType()))));
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBreakBlock(BlockBreakEvent event) {
		if (event.isCancelled() || event.getBlock().getLocation().getWorld() == null)
			return;
		Collection<ItemStack> drops = event.getBlock().getDrops();
		for (ItemStack itemStack : drops) {
			if (!itemStack.getType().equals(Material.PLAYER_HEAD))
				continue;
			NBTItem nbtItem = new NBTItem(itemStack);
			NBTCompound skullOwner = nbtItem.getCompound("SkullOwner");
			String value;
			try {
				value = skullOwner.getCompound("Properties").getCompoundList("textures").get(0).getString("Value");
			} catch (NullPointerException e) {
				continue;
			}
			String entityName = plugin.getSkullConfig().getEntityName(value);
			if (entityName == null) continue;
			Skull skull = this.plugin.getSkullConfig().getSkull(EntityType.valueOf(entityName));
			if(skull == null) continue;
			event.setDropItems(false);
			Location location = event.getBlock().getLocation();
			ItemStack skullItemStack = skull.getItemStack();
			location.getWorld().dropItem(location, skullItemStack);
			break;
		}
	}

	public void givePlayerSkull(Player killer, Player killed) {
		ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta itemMeta = (SkullMeta) Objects.requireNonNullElse(itemStack.getItemMeta(), ItemStackUtil.getItemMeta(itemStack.getType()));
		itemMeta.setDisplayName(
				Objects.requireNonNullElse(plugin.getSkullConfig().getConfig().getString("Player.DisplayName"),"%killed% 的头")
						.replace("%killed%", killed.getName()));
		itemMeta.setOwningPlayer(killed);
		itemStack.setItemMeta(itemMeta);
		killer.getInventory().addItem(itemStack);
		String dropPlayerHead = I18n.t("head.drop-player", true).replace("%player%", killer.getName())
				.replace("%target%", killed.getName());
		Util.alert(dropPlayerHead, true);
	}

	@EventHandler
	public void onMyPetCall(MyPetCallEvent event) {
		Player player = event.getOwner().getPlayer();
		Parkour p = plugin.getParkoursConfig().getParkourPlayerIn(player);
		if (p != null) {
			// player.sendMessage("§7[§9末日社团§7] §c你不能在跑酷场地召唤宠物");
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onMyPetActiveSkill(MyPetActiveSkillEvent event) {
		Player player = event.getOwner().getPlayer();
		Parkour p = plugin.getParkoursConfig().getParkourPlayerIn(player);
		if (p != null) {
			// player.sendMessage("§7[§9末日社团§7] §c你不能在跑酷场地释放宠物技能");
			Bukkit.dispatchCommand(player, "petsendaway");
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onMyPetActiveTargetSkill(MyPetActiveTargetSkillEvent event) {
		Player player = event.getOwner().getPlayer();
		Parkour p = plugin.getParkoursConfig().getParkourPlayerIn(player);
		if (p != null) {
			// player.sendMessage("§7[§9末日社团§7] §c你不能在跑酷场地释放宠物技能");
			Bukkit.dispatchCommand(player, "petsendaway");
			event.setCancelled(true);
		}
	}
	
	// NeverLag 防止 TNT 链式反应
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onTNTExplode(EntityExplodeEvent e) {
		// 防爆
		e.blockList().clear();
		Entity entity = e.getEntity();
		if (entity instanceof TNTPrimed) {
			for (Entity primedTNT : entity.getNearbyEntities(4.5, 4.5, 4.5)) {
				if (primedTNT instanceof TNTPrimed) {
					primedTNT.remove();
				}
			}
		}
	}

	// DoomsdayEssentials 禁止生物传送
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityTeleport(EntityTeleportEvent event) {
		Location from = event.getFrom();
		Location to = event.getTo();
		EntityType type = event.getEntityType();
		if (type.equals(EntityType.MINECART) || type.equals(EntityType.MINECART_CHEST)
				|| type.equals(EntityType.MINECART_COMMAND) || type.equals(EntityType.MINECART_FURNACE)
				|| type.equals(EntityType.MINECART_HOPPER) || type.equals(EntityType.MINECART_MOB_SPAWNER)
				|| type.equals(EntityType.MINECART_TNT) || type.equals(EntityType.DONKEY)
				|| type.equals(EntityType.HORSE) || type.equals(EntityType.DROPPED_ITEM)
				|| type.equals(EntityType.ZOMBIE_HORSE) || type.equals(EntityType.SKELETON_HORSE)
				|| type.equals(EntityType.ARMOR_STAND)
				|| type.equals(EntityType.ITEM_FRAME) || type.equals(EntityType.IRON_GOLEM)) {
			if (this.isTeleportDisallow(from, to)) {
				event.setCancelled(true);
			}
		}
	}

	private boolean isTeleportDisallow(Location from, Location to) {
		if(from.getWorld() == null || to.getWorld() == null) return false;
		if (from.getWorld().getName().contains("nether") || to.getWorld().getName().contains("nether"))
			return true;
		return from.getWorld().getName().contains("end") || to.getWorld().getName().contains("end");
	}

	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event) {
		if(event.getLocation().getWorld() == null) return;
		if (event.getEntityType().equals(EntityType.PHANTOM)) {
			if (event.getLocation().getWorld().getName().equals("world")) {
				event.setCancelled(true);
				event.getEntity().remove();
			}
		}
		Boolean doMobSpawning = event.getLocation().getWorld().getGameRuleValue(GameRule.DO_MOB_SPAWNING);
		if (doMobSpawning != null && !doMobSpawning) {
			return;
		}
		if (event.getEntityType().equals(EntityType.IRON_GOLEM)) {
			if (new Random().nextInt(1000) < 500) {
				event.setCancelled(true);
				event.getEntity().remove();
				return;
			}
			int count = 0;
			for (Entity e : event.getLocation().getChunk().getEntities()) {
				if (e.getType().equals(EntityType.IRON_GOLEM)) {
					count++;
				}
			}
			if (count > 4) {
				event.setCancelled(true);
				event.getEntity().remove();
			}
		}
	}

	public boolean isRenameable(ItemStack item) {
		if (plugin.getSkillSelfAttack().canItemStackRunSkill(item)
				|| ItemManager.toRPGItem(item).orElse(null) != null)
			return true;
		if (item == null || item.getType() == Material.AIR)
			return false;
		if (item.getItemMeta() == null || item.getItemMeta().getLore() == null)
			return false;
		return item.getItemMeta().getLore().contains("§e§b§5§6§a丢弃该物品，使用已改名命名牌左键物品下面的方块以改名");
	}

	// 神器改名
	@EventHandler
	public void onPlayerClickItem(PlayerInteractEvent event) {
		Block clickedBlock = event.getClickedBlock();
		if (event.getAction() != Action.LEFT_CLICK_BLOCK || clickedBlock == null)
			return;
		Location loc = new Location(clickedBlock.getWorld(), clickedBlock.getX(),
				clickedBlock.getY() + 1, clickedBlock.getZ());
		Entity entity = null;
		for (Entity e : event.getPlayer().getNearbyEntities(5, 5, 5)) {
			if (loc.getWorld() != null && e instanceof Item && e.getWorld().getName().equals(loc.getWorld().getName())
					&& e.getLocation().getBlockX() == loc.getBlockX() && e.getLocation().getBlockY() == loc.getBlockY()
					&& e.getLocation().getBlockZ() == loc.getBlockZ()) {
				entity = e;
				break;
			}
		}
		if (entity == null)
			return;
		if (event.getHand() == EquipmentSlot.HAND) {
			ItemStack itemClicked = ((Item) entity).getItemStack();
			if (this.isRenameable(itemClicked)) {
				ItemStack itemHand = event.getPlayer().getInventory().getItemInMainHand();
				if (itemHand.getType() == Material.NAME_TAG && itemHand.getItemMeta() != null) {
					itemHand.getItemMeta().getDisplayName();
					if (itemHand.getItemMeta().getDisplayName().length() > 0) {
						ItemStackUtil.setItemDisplayName(itemClicked,
								ChatColor.translateAlternateColorCodes('&', itemHand.getItemMeta().getDisplayName()));
						if (itemHand.getAmount() - 1 <= 0) {
							event.getPlayer().getInventory().setItemInMainHand(null);
						} else {
							itemHand.setAmount(itemHand.getAmount() - 1);
							event.getPlayer().getInventory().setItemInMainHand(itemHand);
						}
						((Item) entity).setItemStack(itemClicked);
						event.getPlayer().sendMessage("§7[§9末日社团§7] §a你已成功修改该物品的名称");
					}
				}
			}
		}
	}

	@EventHandler
	public void onEntityDropItem(EntityDropItemEvent event) {
		if (event.getEntityType() == EntityType.EVOKER) {
			if (event.getItemDrop().getItemStack().getType() == Material.TOTEM_OF_UNDYING) {
				event.getItemDrop().setItemStack(null);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event) {
		if ((event.getEntity().getShooter() instanceof Player) && event.getHitEntity() != null
				&& (event.getHitEntity() instanceof LivingEntity)) {
			Player player = (Player) event.getEntity().getShooter();
			player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
		}
	}
}
