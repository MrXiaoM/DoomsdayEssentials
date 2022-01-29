package top.mrxiaom.doomsdayessentials.modules.reviveme;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.*;
import org.spigotmc.event.entity.EntityMountEvent;
import top.mrxiaom.doomsdayessentials.utils.I18n;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class EventsListener implements Listener {
    static ReviveMe plugin = ReviveMe.getInstance();
    Map<String, Location> oldLoc = new HashMap<>();

    @EventHandler(
            priority = EventPriority.MONITOR
    )
    public void onEntityDamege(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (!e.isCancelled()) {
                if (plugin.Citizens && !plugin.getConfig().getBoolean("CitizensCompatibility.damagedNpc") && CitizensListener.isNpc(e.getEntity())) {
                    return;
                }

                if (!e.getCause().equals(DamageCause.CUSTOM)) {
                    plugin.getManager().damageEvents.put(p, e);
                }

                if (!plugin.getManager().disableWorlds.isEmpty() && plugin.getManager().disableWorldsEnable && plugin.getManager().disableWorlds.contains(p.getWorld().getName())) {
                    return;
                }

                if (!plugin.getManager().enableWorlds.isEmpty() && plugin.getManager().enableWorldsEnable && !plugin.getManager().enableWorlds.contains(p.getWorld().getName())) {
                    return;
                }

                if (plugin.getConfig().getBoolean("permissions.reviveVictimEnablePermission") && !plugin.getManager().hasPermission(p, "ReviveMe.shotdown")) {
                    return;
                }

                if (!plugin.getManager().playersPose.contains(p)) {
                    if (e.getFinalDamage() >= p.getHealth() && !e.getCause().equals(DamageCause.VOID)) {
                        EntityDamageByEntityEvent e2;
                        Player p2;
                        if (plugin.getManager().firstTotem) {
                            if (!plugin.getManager().equipTotem(p)) {
                                plugin.getManager().startPose(p, "Damage. code: 001");
                                e.setDamage(0.0D);
                                p.setHealth(0.1D);
                                if (e.getCause().equals(DamageCause.ENTITY_ATTACK)) {
                                    e2 = (EntityDamageByEntityEvent) e;
                                    e2.getDamager();
                                    if (e2.getDamager() instanceof Player) {
                                        p2 = (Player) e2.getDamager();
                                        p2.sendMessage(I18n.t("reviveme.shotDown.player", true).replace("<VICTIM>", p.getName()));
                                        p.sendMessage(I18n.t("reviveme.shotDown.victim", true).replace("<PLAYER>", p2.getName()));
                                    }
                                } else {
                                    p.sendMessage(I18n.t("reviveme.shotDown.victim", true).replace("<PLAYER>", e.getCause().toString()));
                                }
                            }
                        } else {
                            plugin.getManager().startPose(p, "Damage. code: 002");
                            e.setDamage(0.0D);
                            p.setHealth(0.1D);
                            if (e.getCause().equals(DamageCause.ENTITY_ATTACK)) {
                                e2 = (EntityDamageByEntityEvent) e;
                                if (e2.getDamager() instanceof Player) {
                                    p2 = (Player) e2.getDamager();
                                    p2.sendMessage(I18n.t("reviveme.shotDown.player", true).replace("<VICTIM>", p.getName()));
                                    p.sendMessage(I18n.t("reviveme.shotDown.victim", true).replace("<PLAYER>", p2.getName()));
                                }
                            } else {
                                p.sendMessage(I18n.t("reviveme.shotDown.victim", true).replace("<PLAYER>", e.getCause().toString()));
                            }
                        }

                        if (!plugin.getManager().firstTotem) {
                            plugin.getManager().equipTotem(p);
                        }
                    }
                } else {
                    if (e.getCause().equals(DamageCause.VOID)) {
                        return;
                    }

                    if (plugin.getManager().invulnerabilityDelay.containsKey(p)) {
                        if (plugin.getManager().forcedDeath.contains(p)) {
                            plugin.getManager().forcedDeath.remove(p);
                            if (plugin.getManager().playersPose.contains(p)) {
                                plugin.getManager().endPose(p, "Damage. code: 003");
                            }
                        } else {
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }

    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Block next;
        Block old;
        if (plugin.getManager().playersPose.contains(p) && e.getTo() != null && this.isNextBlock(e.getFrom(), e.getTo())) {
            next = e.getTo().getBlock().getRelative(BlockFace.UP);
            old = e.getFrom().getBlock().getRelative(BlockFace.UP);
            if (next.getType().equals(Material.AIR) || !next.getType().isSolid()) {
                if (!next.isLiquid() && !e.getTo().getBlock().isLiquid()) {
                    p.sendBlockChange(next.getLocation(), Material.BARRIER, (byte) 0);
                }

                p.sendBlockChange(old.getLocation(), old.getBlockData());
            }
        }

        if (plugin.getManager().relivingPlayer.containsKey(p) || plugin.getManager().relivingPlayerOther.containsKey(p)) {
            Player p1;
            Player p2;
            Location l1;
            Location l2;
            if (plugin.getManager().relivingPlayer.containsKey(p)) {
                p2 = plugin.getManager().relivingPlayer.get(p);
                l1 = e.getTo();
                l2 = p2.getLocation();
            } else {
                p1 = plugin.getManager().relivingPlayerOther.get(p);
                p2 = p;
                l1 = e.getTo();
                l2 = p1.getLocation();
            }

            if (l1 != null && l1.getWorld() != null && l1.getWorld().equals(l2.getWorld())) {
                if (l1.distance(l2) >= 1.0D) {
                    plugin.getManager().cancelReliving(p, p2);
                }
            } else {
                plugin.getManager().cancelReliving(p, p2);
            }
        }

    }

    @EventHandler(
            ignoreCancelled = true,
            priority = EventPriority.MONITOR
    )
    public void onHealtRegen(EntityRegainHealthEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (plugin.getManager().playersPose.contains(p)) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler(
            ignoreCancelled = true,
            priority = EventPriority.MONITOR
    )
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            if (!plugin.getManager().disableWorlds.isEmpty() && plugin.getManager().disableWorldsEnable && plugin.getManager().disableWorlds.contains(p.getWorld().getName())) {
                return;
            }

            if (!plugin.getManager().enableWorlds.isEmpty() && plugin.getManager().enableWorldsEnable && !plugin.getManager().enableWorlds.contains(p.getWorld().getName())) {
                return;
            }

            if (plugin.getManager().playersPose.contains(p)) {
                e.setCancelled(true);
            }
        }

    }

    public boolean isNextBlock(Location from, Location to) {
        int movX = from.getBlockX() - to.getBlockX();
        int movZ = from.getBlockZ() - to.getBlockZ();
        int movY = from.getBlockY() - to.getBlockY();
        return Math.abs(movX) > 0 || Math.abs(movZ) > 0 || Math.abs(movY) > 0;
    }

    public Player getPlayer(Player p) {
        Map<Player, Double> players = new HashMap<>();
        Player end = null;
        Iterator<Player> var5 = p.getWorld().getPlayers().iterator();

        while (true) {
            Player p2;
            double distance;
            String permission;
            do {
                do {
                    do {
                        if (!var5.hasNext()) {
                            var5 = players.keySet().iterator();

                            while (var5.hasNext()) {
                                p2 = var5.next();
                                if (plugin.getManager().playersPose.contains(p2)) {
                                    if (end == null) {
                                        end = p2;
                                    } else if (players.get(p2) < players.get(end)) {
                                        end = p2;
                                    }
                                }
                            }

                            return end;
                        }

                        p2 = var5.next();
                    } while (p2.equals(p));

                    distance = 1000.0D;
                    if (p2.getWorld().equals(p.getWorld())) {
                        distance = p2.getLocation().distance(p.getLocation());
                    }

                    //p.getName().equalsIgnoreCase("FavioMC19");
                } while (distance > 1.0D);

                permission = "none";
                if (!p.isSneaking() && plugin.getConfig().getBoolean("permissions.revivePlayerEnablePermission")) {
                    if (!plugin.getManager().hasPermission(p, "ReviveMe.reliving")) {
                        permission = "false";
                    } else {
                        permission = "true";
                    }
                }
            } while (!permission.equalsIgnoreCase("none") && (!permission.equalsIgnoreCase("true") || plugin.getManager().relivingPlayer.containsKey(p2) || plugin.getManager().relivingPlayer.containsKey(p)));

            players.put(p2, distance);
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent e) {
        Player f = Bukkit.getPlayer("FavioMC19");
        Player p = e.getPlayer();
        if (plugin.getManager().disableWorlds.isEmpty() || !plugin.getManager().disableWorldsEnable || !plugin.getManager().disableWorlds.contains(p.getWorld().getName())) {
            if (plugin.getManager().enableWorlds.isEmpty() || !plugin.getManager().enableWorldsEnable || plugin.getManager().enableWorlds.contains(p.getWorld().getName())) {
                Player p2 = this.getPlayer(p);
                if (p2 != null) {
                    plugin.getManager().toggleRelive(p, p2);
                }

                if (plugin.getManager().playersPose.contains(p) && plugin.getConfig().getBoolean("EnableForceDeath") && !p.isSneaking()) {
                    plugin.getManager().forceDeath(p);
                }

            }
        }
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        String n = p.getName();
        if (this.oldLoc.containsKey(n)) {
            Location l = this.oldLoc.get(n);
            if (e.getTo() != null && e.getTo().equals(l) || e.getTo() == l) {
                e.setCancelled(true);
                this.oldLoc.remove(n);
            }
        }

        if (!plugin.getManager().disableWorlds.isEmpty() && plugin.getManager().disableWorldsEnable && plugin.getManager().disableWorlds.contains(p.getWorld().getName())) {
            if (plugin.getManager().playersPose.contains(p)) {
                plugin.getManager().endPose(p, "Change world to disable");
            }

        } else if (plugin.getManager().enableWorlds.isEmpty() || !plugin.getManager().enableWorldsEnable || plugin.getManager().enableWorlds.contains(p.getWorld().getName())) {
            if (plugin.getManager().playersPose.contains(p)) {
                plugin.getManager().cancelReliving(p, plugin.getManager().relivingPlayer.get(p));
            }

        }
    }

    @EventHandler
    public void onToggleSwimming(EntityToggleSwimEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (plugin.getManager().playersPose.contains(p)) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onToggleSprint(PlayerToggleSprintEvent e) {
        Player p = e.getPlayer();
        if (plugin.getManager().playersPose.contains(p)) {
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (plugin.getManager().playersPose.contains(p)) {
            this.oldLoc.put(p.getName(), p.getLocation());
            plugin.getManager().forceDeath(p);
        }

        if (plugin.getManager().relivingPlayer.containsKey(p)) {
            plugin.getManager().cancelReliving(p, plugin.getManager().relivingPlayer.get(p));
        }

    }

    @EventHandler(
            ignoreCancelled = true,
            priority = EventPriority.MONITOR
    )
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (e.getEntity().getLastDamageCause() != null && e.getEntity().getLastDamageCause().getCause().equals(DamageCause.CUSTOM) && plugin.getManager().damageEvents.containsKey(e.getEntity())) {
            e.getEntity().setLastDamageCause(plugin.getManager().damageEvents.get(e.getEntity()));
        }

        Player p = e.getEntity();
        if (plugin.getManager().playersPose.contains(p)) {
            plugin.getManager().endPose(p, "Death. code: 004");
        }

    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (plugin.getManager().playersPose.contains(p)) {
            boolean blocked = false;
            List<String> blackList = plugin.getConfig().getStringList("commands-blacklist");
            if (!blackList.isEmpty()) {
                if (e.getMessage().contains(" ")) {
                    String text = e.getMessage().toLowerCase().split(" ")[0].replaceFirst(Pattern.quote("/"), "");
                    if (blackList.contains(text)) {
                        blocked = true;
                    }
                } else {
                    for (String text : blackList) {
                        if (text.equalsIgnoreCase(e.getMessage().toLowerCase().replaceFirst("/", ""))) {
                            blocked = true;
                            break;
                        }
                    }
                }

                if (blocked) {
                    p.sendMessage(I18n.t("reviveme.blockedCommand", true));
                    e.setCancelled(true);
                }
            }
        }

    }

    @EventHandler(
            ignoreCancelled = true,
            priority = EventPriority.MONITOR
    )
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (plugin.getManager().playersPose.contains(p)) {
            e.setCancelled(true);
        }

    }

    @EventHandler(
            ignoreCancelled = true,
            priority = EventPriority.MONITOR
    )
    public void onPlayeruseBow(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (plugin.getManager().playersPose.contains(p)) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler(
            ignoreCancelled = true,
            priority = EventPriority.MONITOR
    )
    public void onPlayeruseTrident(ProjectileLaunchEvent e) {
        Projectile pro = e.getEntity();
        if ((pro.getType().equals(EntityType.TRIDENT) || pro.getType().equals(EntityType.ARROW)) && pro.getShooter() instanceof Player) {
            Player p = (Player) pro.getShooter();
            if (plugin.getManager().playersPose.contains(p)) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onPlayerMount(EntityMountEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (plugin.getManager().playersPose.contains(p)) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onPlayerClickPlayer(PlayerInteractEntityEvent e) {
        if (e.getRightClicked() instanceof Player) {
            Player p = e.getPlayer();
            Player p2 = (Player) e.getRightClicked();
            if (plugin.getManager().playersPose.contains(p2) && plugin.getConfig().getBoolean("itemSteal")) {
                if (plugin.getConfig().getBoolean("permissions.itemStealPlayer") && !plugin.getManager().hasPermission(p, "ReviveMe.ItemStealPlayer")) {
                    return;
                }

                if (plugin.getConfig().getBoolean("permissions.itemStealVictim") && !plugin.getManager().hasPermission(p2, "ReviveMe.ItemStealVictim")) {
                    return;
                }

                if (!plugin.getManager().invulnerabilityDelay.containsKey(p2)) {
                    p.openInventory(p2.getInventory());
                }
            }

            if (plugin.getManager().playersPose.contains(p)) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onJoinPlayer(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        if (plugin.cacheFile.exists() && plugin.cache.getString("players." + p.getName() + ".oldSpeed") != null) {
            double oldSpeed = plugin.cache.getDouble("players." + p.getName() + ".oldSpeed");
            p.setWalkSpeed((float) oldSpeed);
            plugin.getManager().removePotion(p);
            plugin.cache.set("players." + p.getName() + ".oldSpeed", null);
            plugin.saveCache();
        }
    }

    @EventHandler
    public void onGliding(EntityToggleGlideEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (plugin.getManager().playersPose.contains(p)) {
                p.setGliding(false);
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTarget(EntityTargetEvent e) {
        if (e.getTarget() instanceof Player) {
            Player p = (Player) e.getTarget();
            if (plugin.getManager().isDamaged(p)) {
                e.setCancelled(true);
            }
        }
    }
}
