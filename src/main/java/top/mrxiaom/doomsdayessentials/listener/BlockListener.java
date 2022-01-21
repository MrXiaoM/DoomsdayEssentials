package top.mrxiaom.doomsdayessentials.listener;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.PortalCreateEvent.CreateReason;
import org.bukkit.inventory.ItemStack;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.MarketConfig.MarketData;
import top.mrxiaom.doomsdayessentials.gui.GuiBlockSmitch;
import top.mrxiaom.doomsdayessentials.gui.GuiMarket;
import top.mrxiaom.doomsdayessentials.utils.*;

import java.time.LocalDateTime;
import java.util.List;

public class BlockListener implements Listener {
	final Main plugin;

	public BlockListener(Main plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public boolean isFirePickaxe(ItemStack item) {
		if (item == null || item.getType() == Material.AIR)
			return false;
		if (item.getItemMeta() == null || item.getItemMeta().getLore() == null)
			return false;
		return item.getItemMeta().getLore().contains("§o§a§5§b§a挖掘矿物自动熔炼");
	}

	public ItemStack getItemNoSilk(ItemStack item) {
		if (item == null || item.getType().equals(Material.AIR))
			return item;
		ItemStack i = item.clone();
		if (i.containsEnchantment(Enchantment.SILK_TOUCH)) {
			i.removeEnchantment(Enchantment.SILK_TOUCH);
		}
		return i;
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		// 炽热的鹤嘴锄
		if (isFirePickaxe(player.getInventory().getItemInMainHand())) {
			Material m = block.getType();
			if (m.equals(Material.COAL_ORE) || m.equals(Material.IRON_ORE) || m.equals(Material.GOLD_ORE)
					|| m.equals(Material.LAPIS_ORE) || m.equals(Material.REDSTONE_ORE) || m.equals(Material.DIAMOND_ORE)
					|| m.equals(Material.EMERALD_ORE) || m.equals(Material.NETHER_QUARTZ_ORE)
					|| m.equals(Material.WET_SPONGE) || m.equals(Material.SAND) || m.equals(Material.RED_SAND)) {
				for (ItemStack i : event.getBlock().getDrops(getItemNoSilk(player.getInventory().getItemInMainHand()))) {
					if (i.getType().equals(Material.IRON_ORE)) {
						i.setType(Material.IRON_INGOT);
					}
					if (i.getType().equals(Material.GOLD_ORE)) {
						i.setType(Material.GOLD_INGOT);
					}
					if (i.getType().equals(Material.SAND) || i.getType().equals(Material.RED_SAND)) {
						i.setType(Material.GLASS);
					}
					if (i.getType().equals(Material.WET_SPONGE)) {
						i.setType(Material.SPONGE);
					}
					block.getWorld().dropItem(block.getLocation(), i);
				}
				event.setDropItems(false);
			}
		}
		if(SpaceUtil.signLineEquals(block, 0, "[集市]")) {
			event.setCancelled(true);
			player.sendMessage(I18n.t("market.sign-cannot-break", true));
			if(player.isOp()) {
				player.sendMessage(I18n.t("market.sign-cannot-break-op", true));
			}
		}
		String id = plugin.getMarketConfig().getMarketByLoc(event.getBlock().getLocation());
		if(id != null && !player.isOp() && !event.getClass().getName().contains("quickshop")) {
			if(!ItemStackUtil.isSign(block.getType())) {
				event.setCancelled(true);
				player.sendMessage(I18n.t("market.build-only-sign", true));
			}
		}
	}
	@EventHandler
	public void onPlayerBucketFill(PlayerBucketFillEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		String id = plugin.getMarketConfig().getMarketByLoc(event.getBlock().getLocation());
		if(id != null && !player.isOp()) {
			if(!ItemStackUtil.isSign(block.getType())) {
				event.setCancelled(true);
				player.sendMessage(I18n.t("market.build-only-sign", true));
			}
		}
	}
	@EventHandler
	public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		String id = plugin.getMarketConfig().getMarketByLoc(event.getBlock().getLocation());
		if(id != null && !player.isOp()) {
			if(!ItemStackUtil.isSign(block.getType())) {
				event.setCancelled(true);
				player.sendMessage(I18n.t("market.build-only-sign", true));
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		String id = plugin.getMarketConfig().getMarketByLoc(event.getBlock().getLocation());
		if(id != null && !player.isOp()) {
			if(!ItemStackUtil.isSign(block.getType())) {
				event.setCancelled(true);
				player.sendMessage(I18n.t("market.build-only-sign", true));
			}
		}
	}
	
	
	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		// 此部分来自 NeverLag
		for (int i = 0; i < 4; i++) {
			String line = event.getLine(i);
			if (line != null && line.length() > 50) {
				event.setCancelled(true);
				break;
			}
		}
		// 此部分为末日社团服务器功能
		Player player = event.getPlayer();
		if (event.getBlock().getState() instanceof Sign) {
			//Sign sign = (Sign) event.getBlock().getState();
			String line0 = event.getLine(0);
			if (line0 != null) {

				if (line0.equals("[集市]") && !player.isOp()) {
					event.getBlock().breakNaturally();
					player.closeInventory();
					player.sendMessage(I18n.t("market.sign-cannot-create", true));
					return;
				}
				if (line0.equals("$redstone")) {
					double money = Util.strToDouble(event.getLine(1), 0.0D);
					if (money <= 0 || money > 100000) {
						event.getBlock().breakNaturally();
						player.sendMessage(I18n.t("redstone-sign.money-invalid", true));
						return;
					}
					event.setLine(0, "§0[§c收费红石§0]");
					event.setLine(1, "花费: " + money);
					event.setLine(3, player.getName());
					player.sendMessage(I18n.t("redstone-sign.success", true));
					return;
				}
				if (line0.equals("$lock")) {
					double money = Util.strToDouble(event.getLine(1), 0.0D);
					if (money < 0 || money > 100000) {
						event.getBlock().breakNaturally();
						player.sendMessage(I18n.t("redstone-sign.money-invalid", true));
						return;
					}
					String line2 = event.getLine(2);
					boolean flagIn = line2 != null && line2.contains("i");
					boolean flagOut = line2 != null && line2.contains("o");
					boolean flagEmptyInv = line2 != null && line2.contains("e");
					boolean flagEmptyPotion = line2 != null && line2.contains("p");
					event.setLine(0, "§0[§d收费门§0]");
					event.setLine(1, "花费: " + money);
					event.setLine(2, (flagIn ? "§a进" : "§c进") + (flagOut ? "§a出" : "§c出") +
							(flagEmptyInv || flagEmptyPotion ? " " : "") +
							(flagEmptyInv ? "§2空" : "") + (flagEmptyPotion ? "§2效" : ""));
					event.setLine(3, player.getName());
					player.sendMessage(I18n.t("lcoks.success", true));
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockClick(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Player player = event.getPlayer();
			Block block = event.getClickedBlock();
			if (block == null)
				return;
			// 右键点击锻造台
			if (block.getType() == Material.SMITHING_TABLE && !player.isSneaking()) {
				plugin.getGuiManager().openGui(new GuiBlockSmitch(plugin, player));
				event.setCancelled(true);
				return;
			}
			// 右键点击集市牌子
			if (SpaceUtil.signLineEquals(block, 0, "[集市]")) {
				String marketName = ((Sign) block.getState()).getLine(1);
				ClaimedResidence res = ResidenceApi.getResidenceManager().getByLoc(block.getLocation());
				if(res == null || !res.getName().equals("主城.market")) {
					plugin.getCoreProtectApi().logRemoval("服务器", block.getLocation(), block.getType(), block.getBlockData());
					NMSUtil.breakBlock(block);
					player.sendMessage(I18n.t("market.sign-wrong-position", true));
					return;
				}
					MarketData data = plugin.getMarketConfig().getMarketDataById(marketName);
					// 摊位无主
					if (data == null || data.getOwner().length() == 0) {
						if (!player.isSneaking()) {
							player.sendMessage(I18n.t("market.rentable", true));
							return;
						}
						if (plugin.getEcoApi().getBalance(player) < 1000) {
							player.sendMessage(I18n.t("market.no-money", true));
							return;
						}
						plugin.getEcoApi().withdrawPlayer(player, 1000);
						((Sign) event.getClickedBlock().getState()).setLine(2, "§b摊主:");
						((Sign) event.getClickedBlock().getState()).setLine(3, player.getName());
						LocalDateTime outdateTime = TimeUtil.addDay(1);
						if(data == null) {
							data = new MarketData(marketName, player.getName(), outdateTime, block.getLocation());
						}
						else {
							data.setOwner(player.getName());
							data.setOutdateTime(outdateTime);
							data.setSignLoc(block.getLocation());
						}
						ClaimedResidence marketRes = this.plugin.getMarketConfig().getMarketResidence(marketName);
						if(marketRes != null) {
							marketRes.getPermissions().setPlayerFlag(Bukkit.getConsoleSender(), player.getName(), "container", "true", true, false);
							marketRes.getPermissions().setPlayerFlag(Bukkit.getConsoleSender(), player.getName(), "build", "true", true, false);
							marketRes.getPermissions().setPlayerFlag(Bukkit.getConsoleSender(), player.getName(), "destroy", "true", true, false);
						}
						plugin.getMarketConfig().putMarketData(data);
						player.sendMessage(I18n.tn("market.rent-success", true).replace("%time%", TimeUtil.getChineseTime(outdateTime, "§e", "§a")));
						return;
					}
					// 摊位有主
					String owner = data.getOwner();
					((Sign) event.getClickedBlock().getState()).setLine(2, "§b摊主:");
					((Sign) event.getClickedBlock().getState()).setLine(3, player.getName());
					// 玩家是摊主
					if (owner.equals(player.getName()) && player.isSneaking()) {
						plugin.getGuiManager().openGui(new GuiMarket(plugin, player, data));
						return;
					}
					// 玩家不是摊主
					player.sendMessage(I18n.tn("market.rent-info", true).replace("%id%", data.getId()).replace("%owner%", owner).replace("%time%", TimeUtil.getChineseTime(data.getOutdateTime(), "§e", "§a")));
					if(owner.equals(player.getName())) {
						player.sendMessage(I18n.tn("market.rent-info-owner", true));
					}
				return;
			}
			if(SpaceUtil.signLineEquals(block, 0, "§0[§c收费红石§0]")) {
				if(!player.isSneaking()) {
					event.setCancelled(true);
					player.sendMessage(I18n.t("redstone-sign.need-sneak", true));
					return;
				}
				if(plugin.getPlayerCooldownManager().isRedstoneCooldown(player.getName())) {
					return;
				}
				BlockFace facing = null;
				if(block.getBlockData() instanceof org.bukkit.block.data.type.WallSign) {
					facing = ((org.bukkit.block.data.type.WallSign) block.getBlockData()).getFacing();
				}
				else if(block.getBlockData() instanceof org.bukkit.block.data.type.Sign) {
					facing = BlockFace.UP;
				}
				if(facing == null) return;
				Sign sign = (Sign) block.getState();
				double money = Util.strToDouble(sign.getLine(1).contains(" ") ? sign.getLine(1).substring(sign.getLine(1).lastIndexOf(" ") + 1) : sign.getLine(1), 0.0D);
				if(money <= 0) {
					event.setCancelled(true);
					player.sendMessage(I18n.t("redstone-sign.money-invalid-user", true));
					return;
				}
				if(plugin.getEcoApi().getBalance(player) < money) {
					event.setCancelled(true);
					player.sendMessage(I18n.t("redstone-sign.no-money", true));
					return;
				}
				OfflinePlayer owner = Util.getOfflinePlayer(sign.getLine(3));
				if(owner == null) {
					event.setCancelled(true);
					player.sendMessage(I18n.t("redstone-sign.no-owner", true));
					return;
				}
				Block redstone = block.getRelative(facing.getOppositeFace(), 2);
				player.sendMessage(redstone.getType() + " : " + redstone.getState().getClass().getName());
				
				if(!redstone.getType().equals(Material.REDSTONE_WIRE)) {
					player.sendMessage(I18n.t("redstone-sign.not-powerable", true));
					return;
				}
				plugin.getPlayerCooldownManager().setRedstoneCooldown(player.getName(), 10);
				RedstoneWire data = ((RedstoneWire) redstone.getBlockData());
				data.setPower(15);
				redstone.setBlockData(data);
				plugin.getEcoApi().withdrawPlayer(player, money);
				plugin.getEcoApi().depositPlayer(owner, money);
				Bukkit.getScheduler().runTaskLater(plugin, () -> {
					data.setPower(0);
					redstone.setBlockData(data);
				}, 2);
				player.sendMessage(I18n.t("redstone-sign.used", true).replace("%money%", String.valueOf(money)));
				if(owner.isOnline() && owner.getPlayer() != null) {
					owner.getPlayer().sendMessage(I18n.t("redstone-sign.used-owner")
							.replace("%player%", player.getName())
							.replace("%world%", block.getWorld().getName())
							.replace("%x%", String.valueOf(block.getX()))
							.replace("%y%", String.valueOf(block.getY()))
							.replace("%z%", String.valueOf(block.getZ()))
							.replace("%money%", String.valueOf(money)));
				}
				return;
			}
			if(SpaceUtil.signLineEquals(block, 0, "§0[§d收费门§0]")) {
				Sign sign = (Sign) block.getState();
				double money = Util.strToDouble(sign.getLine(1).substring(4), -1.0D);
				if(money < 0) {
					block.breakNaturally();
					player.sendMessage("§7[§9末日社团§7]§c 此收费门存在问题，已拆除牌子");
					return;
				}
				String line2 = sign.getLine(2);
				boolean flagIn = line2.contains("§a进");
				boolean flagOut = line2.contains("§a出");
				boolean flagEmptyInv = line2.contains("§2空");
				boolean flagEmptyPotion = line2.contains("§2效");
				String rules = (flagIn ? "§e可以进入" : "§c不能进入") + "§a， " + (flagOut ? "§e可以出去" : "§c不能出去") 
						+ (flagEmptyInv || flagEmptyPotion ? "§a， " : "")
						+ (flagEmptyInv ? ("§e需要背包为空" + (flagEmptyPotion ? "§a， " : "")) : "")
						+ (flagEmptyPotion ? "§e需要无药水效果" : "");
				String owner = sign.getLine(3);
				player.sendMessage(I18n.tn("locks.details", true)
						.replace("%owner%", owner)
						.replace("%money%", String.format("%.2f", money))
						.replace("%rules%", rules));
				return;
			}
			if(block.getType().equals(Material.IRON_DOOR)) {
				Door door = (Door) block.getBlockData();
				Block up = block.getRelative(BlockFace.UP, (door.getHalf().equals(Half.BOTTOM) ? 2 : 1));
				if(up.getType().isAir()) return;
				BlockFace doorFace = door.getFacing();
				Block signBlock = up.getRelative(doorFace);
				if(!ItemStackUtil.isWallSign(signBlock.getType())) {
					signBlock = up.getRelative(doorFace = doorFace.getOppositeFace());
					if(!ItemStackUtil.isWallSign(signBlock.getType())) return;
				}
				Sign sign = (Sign) signBlock.getState();
				double money = Util.strToDouble(sign.getLine(1).substring(4), -1.0D);
				if(money < 0) {
					block.breakNaturally();
					player.sendMessage("§7[§9末日社团§7]§c 此收费门存在问题，已拆除牌子");
					return;
				}
				boolean action = event.getBlockFace().equals(doorFace);
				String line2 = sign.getLine(2);
				boolean flagIn = line2.contains("§a进");
				boolean flagOut = line2.contains("§a出");
				boolean flagEmptyInv = line2.contains("§2空");
				boolean flagEmptyPotion = line2.contains("§2效");
				OfflinePlayer owner = Util.getOfflinePlayer(sign.getLine(3));
				if(owner == null) {
					player.sendMessage(I18n.t("locks.no-owner", true));
					return;
				}
				if(!player.isSneaking()) {
					String rules = (flagIn ? "§e可以进入" : "§c不能进入") + "§a， " + (flagOut ? "§e可以出去" : "§c不能出去") 
							+ (flagEmptyInv || flagEmptyPotion ? "§a， " : "")
							+ (flagEmptyInv ? ("§e需要背包为空" + (flagEmptyPotion ? "§a， " : "")) : "")
							+ (flagEmptyPotion ? "§e需要无药水效果" : "");
					player.sendMessage(I18n.tn("locks.details", true)
							.replace("%owner%", owner.getName() != null ? owner.getName(): "???")
							.replace("%money%", String.format("%.2f", money))
							.replace("%rules%", rules));
					return;
				}
				if(action && !flagIn) {
					player.sendMessage(I18n.t("locks.no-entry", true));
					return;
				}
				if(!action && !flagOut) {
					player.sendMessage(I18n.t("locks.no-leaving", true));
					return;
				}
				
				if(action && flagEmptyInv && player.getInventory().all(Material.AIR).size() < 36) {
					player.sendMessage(I18n.t("locks.no-empty-inventory", true));
					return;
				}
				if(action && flagEmptyPotion && player.getActivePotionEffects().size() > 0) {
					player.sendMessage(I18n.t("locks.no-empty-potion", true));
					return;
				}
				if(action && money > 0 ) {
					if(plugin.getEcoApi().getBalance(player) < money) {
						player.sendMessage(I18n.t("locks.no-money", true).replace("%money%", String.format("%.2f", money)));
						return;
					}
					plugin.getEcoApi().withdrawPlayer(player, money);
					plugin.getEcoApi().depositPlayer(owner, money);
				}
				Location target = block.getRelative(doorFace, 1).getLocation();
				player.teleport(new Location(block.getWorld(), target.getBlockX(), target.getBlockY() - (door.getHalf().equals(Half.BOTTOM) ? 0 : 1), target.getBlockZ()));
				if(action) {
					player.sendMessage(I18n.t("locks.used", true).replace("%money%", String.format("%.2f", money)));
					if(owner.getPlayer() != null) {
						owner.getPlayer().sendMessage(I18n.t("locks.used-owner", true)
							.replace("%money%", String.format("%.2f", money))
							.replace("&player&", player.getName()));
					}
				}else {
					player.sendMessage(I18n.t("locks.used-leave", true));
				}
				return;
			}
			if (block.getWorld().getName().equals("spawn") && block.getX() == 155 && block.getY() == 37
					&& block.getZ() == -58) {
				// 打开谜题 GUI
				player.sendMessage("undefined");
			}
		}
	}

	// 禁止传送门在领地內生成
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPortalCreate(PortalCreateEvent event) {
		if (event.getReason() == CreateReason.NETHER_PAIR) {
			for (BlockState bs : event.getBlocks()) {
				ClaimedResidence res = plugin.getResidenceApi().getByLoc(bs.getLocation());
				if (res != null) {
					if (res.getPermissions().has(Flags.build, false)) {
						event.setCancelled(true);
						if (event.getEntity() instanceof Player) {
							Player player = (Player) event.getEntity();
							if (res.getPermissions().playerHas(player, Flags.build, true)) {
								event.setCancelled(false);
								return;
							}
							player.sendMessage(I18n.t("no-nether-portal-message"));
						}
						return;
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPistonExtend(BlockPistonExtendEvent event) {
		List<Block> blocksInWay = event.getBlocks();
		boolean isCancel = false;
		for (Block block : blocksInWay) {
			if (plugin.isPisionExtendBlackList(block.getType())) {
				isCancel = true;
				break;
			}
		}
		if (isCancel) {
			event.setCancelled(true);
		}
	}
}
