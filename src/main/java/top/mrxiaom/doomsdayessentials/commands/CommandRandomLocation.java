package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.RandomTPConfig.TeleportMode;
import top.mrxiaom.doomsdayessentials.configs.RandomTPConfig.TeleportResult;
import top.mrxiaom.doomsdayessentials.configs.RandomTPConfig.Zone;
import top.mrxiaom.doomsdayessentials.modules.reviveme.ReviveMeApi;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class CommandRandomLocation extends ICommand {
	public CommandRandomLocation(Main plugin) {
		super(plugin, "rloc", new String[] { "rspawn", "rplayer" });
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if(isPlayer && ReviveMeApi.isPlayerDowned((Player) sender)){
			sender.sendMessage(I18n.t("reviveme.no-command",true));
			return true;
		}
		if (label.equalsIgnoreCase("rloc")) {
			return onCommandRLoc(sender, args, isPlayer);
		}
		if (label.equalsIgnoreCase("rplayer")) {
			return onCommandRPlayer(sender, args);
		}
		if (label.equalsIgnoreCase("rspawn")) {
			if (!isPlayer) {
				return Util.noPlayer(sender);
			}
			return onCommandRSpawn((Player) sender, args);
		}

		return true;
	}

	public boolean onCommandRLoc(CommandSender sender, String[] args, boolean isPlayer) {
		if (!sender.isOp()) {
			return Util.noPerm(sender);
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
			sender.sendMessage(I18n.t("randomlocation.rloc-list.header", true));
			for (Zone zone : this.plugin.getRandomTPConfig().getAllZone()) {
				sender.sendMessage(I18n.t("randomlocation.rloc-list.line", true).replace("%zone%", zone.getName())
						.replace("%price%", String.valueOf(zone.getPrice()))
						.replace("%world%", zone.getWorld().getName()));
			}
			return true;
		}
		if (args.length >= 2) {
			if (args[0].equalsIgnoreCase("cache")) {
				String zoneName = args[1];
				List<Location> list = this.plugin.getRandomTPConfig().getCacheLocations(zoneName);
				if(list == null) {
					sender.sendMessage(I18n.t("randomlocation.nocache", true).replace("%zone%", zoneName));
					return true;
				}
				sender.sendMessage(I18n.t("randomlocation.cache", true).replace("%zone%", zoneName).replace("%size%", String.valueOf(list.size())));
				return true;
			}
			if (args[0].equalsIgnoreCase("info")) {
				String zoneName = args[1];
				if (!this.plugin.getRandomTPConfig().contains(zoneName)) {
					sender.sendMessage(I18n.t("randomlocation.nozone", true));
					return true;
				}
				Zone zone = this.plugin.getRandomTPConfig().get(zoneName);
				if(zone == null) return true;
				for (String s : I18n.l("randomlocation.rloc-info")) {
					sender.sendMessage(I18n.prefix() + s.replace("%zone%", zoneName)
							.replace("%world%", zone.getWorld().getName()).replace("%x1%", String.valueOf(zone.getX1()))
							.replace("%y1%", String.valueOf(zone.getY1())).replace("%z1%", String.valueOf(zone.getZ1()))
							.replace("%x2%", String.valueOf(zone.getX2())).replace("%y2%", String.valueOf(zone.getY2()))
							.replace("%z2%", String.valueOf(zone.getZ2()))
							.replace("%price%", String.valueOf(zone.getPrice()))
							.replace("%mode%", zone.getMode().name()));
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("create") && isPlayer) {
				Player player = (Player) sender;
				String zoneName = args[1];
				if (this.plugin.getRandomTPConfig().contains(zoneName)) {
					sender.sendMessage(I18n.t("randomlocation.create-exist", true));
					return true;
				}
				int x = player.getLocation().getBlockX();
				int z = player.getLocation().getBlockZ();
				if (plugin.getRandomTPConfig().newZone(zoneName, player.getWorld().getName(), x - 20000, 0, z - 20000,
						x + 20000, 255, z + 20000, 0, TeleportMode.TOP_GROUND, new ArrayList<>()) == null) {
					player.sendMessage(I18n.t("randomlocation.create-exist"));
					return true;
				}
				player.sendMessage(I18n.t("randomlocation.create").replace("%zone%", zoneName));
				return true;
			}
			if (args[0].equalsIgnoreCase("pos1")) {
				String zoneName = args[1];
				if (!this.plugin.getRandomTPConfig().contains(zoneName)) {
					sender.sendMessage(I18n.t("randomlocation.nozone", true));
					return true;
				}
				if (!isPlayer) {
					return Util.noPlayer(sender);
				}
				Zone zone = this.plugin.getRandomTPConfig().get(zoneName);
				if(zone == null) return true;
				if (args.length == 3) {
					Location loc = ((Player) sender).getLocation();
					zone.setWorld(loc.getWorld()).setX1(loc.getBlockX()).setY1(loc.getBlockY()).setZ1(loc.getBlockZ())
							.save();
					sender.sendMessage(I18n.t("randomlocation.set-pos", true).replace("%zone%", zoneName)
							.replace("%pos%", "1").replace("%world%", loc.getWorld() != null ? loc.getWorld().getName() : "???")
							.replace("%x%", String.valueOf(loc.getBlockX()))
							.replace("%y%", String.valueOf(loc.getBlockY()))
							.replace("%z%", String.valueOf(loc.getBlockZ())));
					return true;
				}
				if (args.length >= 5) {
					World world = args.length >= 6 ? Bukkit.getWorld(args[5]) : zone.getWorld();
					if (world == null) {
						sender.sendMessage(I18n.t("randomlocation.noworld", true));
						return true;
					} else {
						zone.setWorld(world);
					}
					try {
						int x = Integer.parseInt(args[2]);
						int y = Integer.parseInt(args[3]);
						int z = Integer.parseInt(args[4]);
						zone.setX1(x).setY1(y).setZ1(z).save();
						sender.sendMessage(
								I18n.t("randomlocation.set-pos", true).replace("%zone%", zoneName).replace("%pos%", "1")
										.replace("%world%", world.getName()).replace("%x%", String.valueOf(x))
										.replace("%y%", String.valueOf(y)).replace("%z%", String.valueOf(z)));
						return true;
					} catch (NumberFormatException e) {
						sender.sendMessage(I18n.t("not-integer", true));
						return true;
					}
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("pos2")) {
				String zoneName = args[1];
				if (!this.plugin.getRandomTPConfig().contains(zoneName)) {
					sender.sendMessage(I18n.t("randomlocation.nozone", true));
					return true;
				}
				if (!isPlayer) {
					return Util.noPlayer(sender);
				}
				Zone zone = this.plugin.getRandomTPConfig().get(zoneName);
				if(zone == null) return true;
				if (args.length == 3) {
					Location loc = ((Player) sender).getLocation();
					zone.setWorld(loc.getWorld()).setX2(loc.getBlockX()).setY2(loc.getBlockY()).setZ2(loc.getBlockZ())
							.save();
					sender.sendMessage(I18n.t("randomlocation.set-pos", true).replace("%zone%", zoneName)
							.replace("%pos%", "2").replace("%world%", loc.getWorld() != null ? loc.getWorld().getName() : "???")
							.replace("%x%", String.valueOf(loc.getBlockX()))
							.replace("%y%", String.valueOf(loc.getBlockY()))
							.replace("%z%", String.valueOf(loc.getBlockZ())));
					return true;
				}
				if (args.length >= 5) {
					World world = args.length >= 6 ? Bukkit.getWorld(args[5]) : zone.getWorld();
					if (world == null) {
						sender.sendMessage(I18n.t("randomlocation.noworld", true));
						return true;
					} else {
						zone.setWorld(world);
					}
					try {
						int x = Integer.parseInt(args[2]);
						int y = Integer.parseInt(args[3]);
						int z = Integer.parseInt(args[4]);
						zone.setX2(x).setY2(y).setZ2(z).save();
						sender.sendMessage(
								I18n.t("randomlocation.set-pos", true).replace("%zone%", zoneName).replace("%pos%", "2")
										.replace("%world%", world.getName()).replace("%x%", String.valueOf(x))
										.replace("%y%", String.valueOf(y)).replace("%z%", String.valueOf(z)));
						return true;
					} catch (NumberFormatException e) {
						sender.sendMessage(I18n.t("not-integer", true));
						return true;
					}
				}
				return true;
			}
			if (args.length >= 3) {
				if (args[0].equalsIgnoreCase("range")) {
					if (!isPlayer) {
						return Util.noPlayer(sender);
					}
					String zoneName = args[1];
					if (!this.plugin.getRandomTPConfig().contains(zoneName)) {
						sender.sendMessage(I18n.t("randomlocation.nozone", true).replace("%zone%", zoneName));
						return true;
					}
					Zone zone = this.plugin.getRandomTPConfig().get(zoneName);
					if(zone == null) return true;
					Player player = (Player) sender;
					try {
						int xRange = Integer.parseInt(args[2]);
						int zRange = args.length >= 4 ? Integer.parseInt(args[3]) : xRange;
						if (args.length >= 5) {
							int y = Integer.parseInt(args[4]);
							zone.setY1(y);
						}
						if (args.length >= 6) {
							int y = Integer.parseInt(args[5]);
							zone.setY2(y);
						}
						int x = player.getLocation().getBlockX();
						int z = player.getLocation().getBlockZ();
						zone.setX1(x - xRange).setX2(x + xRange).setZ1(z - zRange).setZ2(z + zRange).save();
						player.sendMessage(I18n.t("randomlocation.set-range", true).replace("%zone%", zoneName)
								.replace("%world%", zone.getWorld().getName())
								.replace("%x1%", String.valueOf(zone.getX1()))
								.replace("%y1%", String.valueOf(zone.getY1()))
								.replace("%z1%", String.valueOf(zone.getZ1()))
								.replace("%x1%", String.valueOf(zone.getX2()))
								.replace("%y2%", String.valueOf(zone.getY2()))
								.replace("%z3%", String.valueOf(zone.getZ2()))
								.replace("%price%", String.valueOf(zone.getPrice()))
								.replace("%mode%", zone.getMode().name()));
						return true;
					} catch (NumberFormatException e) {
						sender.sendMessage(I18n.t("not-integer", true));
						return true;
					}
				}
				if (args[0].equalsIgnoreCase("world")) {
					String zoneName = args[1];
					if (!this.plugin.getRandomTPConfig().contains(zoneName)) {
						sender.sendMessage(I18n.t("randomlocation.nozone", true).replace("%zone%", zoneName));
						return true;
					}
					World world = Bukkit.getWorld(args[2]);
					if (world == null) {
						sender.sendMessage(I18n.t("randomlocation.noworld", true));
						return true;
					}
					Zone zone = this.plugin.getRandomTPConfig().get(zoneName);
					if(zone == null) return true;
					zone.setWorld(world).save();
					sender.sendMessage(I18n.t("randomlocation.set-world", true).replace("%zone%", zoneName)
							.replace("%world%", world.getName()));
					return true;
				}
				if (args[0].equalsIgnoreCase("price")) {
					String zoneName = args[1];
					if (!this.plugin.getRandomTPConfig().contains(zoneName)) {
						sender.sendMessage(I18n.t("randomlocation.nozone", true).replace("%zone%", zoneName));
						return true;
					}
					int price = Util.strToInt(args[2], -1);
					if (price < 0) {
						sender.sendMessage(I18n.t("not-integer", true));
						return true;
					}
					Zone zone = this.plugin.getRandomTPConfig().get(zoneName);
					if(zone == null) return true;
					zone.setPrice(price).save();
					sender.sendMessage(I18n.t("randomlocation.set-price", true).replace("%zone%", zoneName)
							.replace("%money%", String.valueOf(price)));
					return true;
				}
				if (args[0].equalsIgnoreCase("mode")) {
					String zoneName = args[1];
					if (!this.plugin.getRandomTPConfig().contains(zoneName)) {
						sender.sendMessage(I18n.t("randomlocation.nozone", true).replace("%zone%", zoneName));
						return true;
					}
					TeleportMode mode = Util.valueOf(TeleportMode.class, args[2]);
					if (mode == null) {
						sender.sendMessage(I18n.t("randomlocation.nomode", true));
					}
					Zone zone = this.plugin.getRandomTPConfig().get(zoneName);
					if(zone == null) return true;
					zone.setMode(mode).save();
					sender.sendMessage(I18n.t("randomlocation.set-mode", true).replace("%zone%", zoneName)
							.replace("%mode%", mode != null ? mode.name() : "???"));
					return true;
				}
			}
		}
		sender.sendMessage(I18n.array("randomlocation.rloc-help", true));
		return true;
	}

	public boolean onCommandRPlayer(CommandSender sender, String[] args) {
		if (!sender.isOp()) {
			return Util.noPerm(sender);
		}
		if (args.length == 2) {
			Player player = Util.getOnlinePlayer(args[0]);
			if(player == null){
				sender.sendMessage(I18n.t("not-online", true));
				return true;
			}
			String zoneName = args[0];
			Zone zone = plugin.getRandomTPConfig().get(zoneName);
			if (zone == null) {
				player.sendMessage(I18n.t("randomlocation.nozone", true));
				return true;
			}
			if(this.plugin.getPlayerCooldownManager().isRandomTPCooldown(player.getName())) {
				player.sendMessage(I18n.t("randomlocation.cooldown", true).replace("%cooldown%", "120"));
				return true;
			}
			TeleportResult result = zone.teleport(player, true);
			if (result.equals(TeleportResult.NO_LOC)) {
				player.sendMessage(I18n.t("randomlocation.noloc", true));
				return true;
			}
			if (result.equals(TeleportResult.SUCCESS)) {
				player.sendMessage(I18n.t("randomlocation.teleport", true).replace("%zone%", zoneName));
				return true;
			}
		}
		sender.sendMessage(I18n.array("randomlocation.rplayer-help", true));
		return true;
	}

	public boolean onCommandRSpawn(Player player, String[] args) {
		if (args.length != 1) {
			player.sendMessage(I18n.array("randomlocation.rspawn-help", true));
			return true;
		}
		String zoneName = args[0];
		Zone zone = plugin.getRandomTPConfig().get(zoneName);
		if (zone == null) {
			player.sendMessage(I18n.t("randomlocation.nozone", true));
			return true;
		}
		if(this.plugin.getPlayerCooldownManager().isRandomTPCooldown(player.getName())) {
			player.sendMessage(I18n.t("randomlocation.cooldown", true).replace("%cooldown%", "120"));
			return true;
		}
		TeleportResult result = zone.teleport(player);
		if (result.equals(TeleportResult.NO_PERM)) {
			Util.noPerm(player);
			return true;
		}
		if (result.equals(TeleportResult.NO_MONEY)) {
			player.sendMessage(
					I18n.t("randomlocation.nomoney", true).replace("%money%", String.valueOf(zone.getPrice())));
			return true;
		}
		if (result.equals(TeleportResult.NO_LOC)) {
			player.sendMessage(I18n.t("randomlocation.noloc", true));
			return true;
		}
		if (result.equals(TeleportResult.SUCCESS)) {
			this.plugin.getPlayerCooldownManager().putRandomTP(player.getName(), 120);
			if (zone.getPrice() > 0) {
				plugin.getEcoApi().withdrawPlayer(player, zone.getPrice());
				player.sendMessage(I18n.t("randomlocation.deduct-money", true).replace("%money%",
						String.valueOf(zone.getPrice())));
			}
			player.sendMessage(I18n.t("randomlocation.teleport", true).replace("%zone%", zoneName));
			return true;
		}
		return true;
	}
}
