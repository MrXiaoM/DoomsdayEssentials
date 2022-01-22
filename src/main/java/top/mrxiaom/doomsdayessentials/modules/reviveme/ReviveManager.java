package top.mrxiaom.doomsdayessentials.modules.reviveme;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import top.mrxiaom.doomsdayessentials.modules.reviveme.utils.IActionBarUtil;
import top.mrxiaom.doomsdayessentials.modules.reviveme.utils.Spigboard;
import top.mrxiaom.doomsdayessentials.modules.reviveme.utils.SpigboardEntry;
import top.mrxiaom.doomsdayessentials.modules.reviveme.utils.VectorUtils;
import top.mrxiaom.doomsdayessentials.modules.reviveme.version.ISendPlay;
import top.mrxiaom.doomsdayessentials.utils.NMSUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.io.File;
import java.util.*;

public class ReviveManager {
    ISendPlay isendPlay = null;
    IActionBarUtil iactionbarutil = null;
    String packageName = Bukkit.getServer().getClass().getPackage().getName();
    String spigot = NMSUtil.getNMSVersion();
    public Map<Player, Integer> relivingCount = new HashMap<>();
    public Map<Player, Player> relivingPlayer = new HashMap<>();
    public Map<Player, Player> relivingPlayerOther = new HashMap<>();
    public List<Player> playersPose = new ArrayList<>();
    public Map<Player, Spigboard> board1 = new HashMap<>();
    public Map<Player, EntityDamageEvent> damageEvents = new HashMap<>();
    public Map<Player, Float> oldSpeed = new HashMap<>();
    public List<String> disableWorlds = new ArrayList<>();
    public List<String> enableWorlds = new ArrayList<>();
    public Map<Player, Integer> deathDelay = new HashMap<>();
    public Map<Player, Integer> invulnerabilityDelay = new HashMap<>();
    public List<Player> relivingList = new ArrayList<>();
    public List<Player> usedTotem = new ArrayList<>();
    public Map<Player, GameMode> oldGameMode = new HashMap<>();
    public boolean disable_message = false;
    public int secondvalue = 0;
    public List<Player> debugPlayers = new ArrayList<>();
    public List<Player> forcedDeath = new ArrayList<>();
    String boardTitle = "&4&l[&e&l我已重伤倒地&4&l]";
    String boardStatus = "&f状态: ";
    String boardWaiting = "&c等死中";
    String boardReliving = "&a救助中";
    String boardDeathIn = "&f死亡倒数 &a<TIME>";
    String boardInvulnerableFor = "&f无敌时间 &a<TIME>";
    String boardVulnerable = "&c脆弱";
    float speed = 0.04F;
    String title = "&f状态 : <STATUS> <BAR>";
    String subTitle = "&f死亡倒计时: &a<DEATHTIME> &f<INVULNERABILITY>";
    int relivingTime = 10;
    boolean firstTotem = false;
    boolean potionEnable = true;
    String potionEffect = "BLINDNESS";
    int potionLevel = 1;

    int invulnerability;
    boolean enableWorldsEnable;
    boolean disableWorldsEnable;
    public ReviveManager() {
        
    }

    FileConfiguration cfg;
    public void onStart() {
        ReviveMe reviveMe = ReviveMe.getInstance();
        cfg = reviveMe.getConfig();
        disableWorlds = cfg.getStringList("disableWorlds");

        saveNewConfig("permissions.revivePlayerEnablePermission", false);
        saveNewConfig("permissions.reviveVictimEnablePermission", false);
        saveNewConfig("totem-first", false);
        ReviveMe.getInstance().saveConfig();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(ReviveMe.getInstance().getPlugin(), () -> {
            for (int i = 0; i < playersPose.size(); ++i) {
                Player p = playersPose.get(i);
                List<Player> var3 = VectorUtils.getNear(100.0D, p);
            }

            if (secondvalue <= 19) {
                ++secondvalue;
            } else {
                countSeconds();
                secondvalue = 0;
            }

        }, 0L, 1L);

        try {
            String packageName = ISendPlay.class.getPackageName() + ".";
            Class<?> clazz = Class.forName(packageName + spigot + ".SendPlay");
            if (ISendPlay.class.isAssignableFrom(clazz)) {
                isendPlay = (ISendPlay) clazz.getConstructor().newInstance();
            }

            Class<?> claz = Class.forName(packageName + spigot + ".ActionBarUtil");
            if (IActionBarUtil.class.isAssignableFrom(claz)) {
                iactionbarutil = (IActionBarUtil) claz.getConstructor().newInstance();
            }
        } catch (Exception var2) {
            debug("§cversion error, report.");
            var2.printStackTrace();
        }

        enableWorldsEnable = cfg.getBoolean("enableWorldsEnable");
        disableWorldsEnable = cfg.getBoolean("disbleWorldsEnable");
        invulnerability = cfg.getInt("delays.invulnerabilityDelay");
        enableWorlds = cfg.getStringList("enableWorlds");
        disableWorlds = cfg.getStringList("disableWorlds");
        if (cfg.getString("relivingTime") == null) {
            cfg.set("relivingTime", "10");
        }

        relivingTime = cfg.getInt("relivingTime");
        firstTotem = cfg.getBoolean("totem-first");
        potionEnable = cfg.getBoolean("potion.enable");
        potionEffect = cfg.getString("potion.effect").toLowerCase();
        potionLevel = cfg.getInt("potion.level");
        disable_message = cfg.getBoolean("newVersionPremiumMessageDisable", false);
    }

    public void startPose(Player p, String cause) {
        debug("Downed, " + cause);
        int deathd = cfg.getInt("delays.deathDelay");
        p.setSwimming(true);
        p.setSprinting(true);
        playersPose.add(p);
        deathDelay.put(p, deathd);
        invulnerabilityDelay.put(p, invulnerability);
        Block b = p.getLocation().getBlock().getRelative(BlockFace.UP);
        p.sendBlockChange(b.getLocation(), Material.BARRIER, (byte) 0);
        oldGameMode.put(p, p.getGameMode());
        p.setGameMode(GameMode.ADVENTURE);
        oldSpeed.put(p, p.getWalkSpeed());
        p.setWalkSpeed(speed);
        Entity vehicle = p.getVehicle();
        if (vehicle != null) {
            vehicle.eject();
        }

        if (cfg.getBoolean("info.title")) {
            p.sendTitle(cfg.getString("title.title").replace("<STATUS>", boardWaiting).replace("<BAR>", "§7██████████").replace("&", "§"), cfg.getString("title.subTitle").replace("<DEATHTIME>", getDeathCountText(deathd)).replace("<INVULNERABILITY>", boardInvulnerableFor.replace("&", "§").replace("<TIME>", getDeathCountText(invulnerability))).replace("&", "§"), 1, 200, 1);
        }

        if (cfg.getBoolean("info.scoreboard")) {
            Spigboard b1;
            if (!board1.containsKey(p)) {
                b1 = new Spigboard(boardTitle.replace("&", "§"));
                b1.add("vacio0", "§b  ", 6);
                b1.add("status", (boardStatus + boardWaiting).replace("&", "§"), 5);
                b1.add("count", "§7██████████", 4);
                b1.add("vacio1", "§b ", 3);
                b1.add("deathCount", boardDeathIn.replace("&", "§").replace("<TIME>", getDeathCountText(deathd)), 2);
                b1.add("vacio2", "§b ", 1);
                b1.add("invulnerableCount", boardInvulnerableFor.replace("<TIME>", getDeathCountText(invulnerability)).replace("&", "§"), 0);
                b1.add(p);
                board1.put(p, b1);
            } else {
                b1 = board1.get(p);
                SpigboardEntry score = b1.getEntry("count");
                SpigboardEntry score2 = b1.getEntry("status");
                SpigboardEntry score3 = b1.getEntry("deathCount");
                SpigboardEntry score4 = b1.getEntry("invulnerableCount");

                try {
                    score.update("§7██████████");
                    score2.update((boardStatus + boardWaiting).replace("&", "§"));
                    score3.update(boardDeathIn.replace("<TIME>", getDeathCountText(deathd).replace("&", "§")));
                    score4.update(boardInvulnerableFor.replace("<TIME>", getDeathCountText(invulnerability)).replace("&", "§"));
                } catch (IllegalStateException ignored) {
                }

                b1.add(p);
            }
        }

        ReviveMe.getInstance().cache.set("players." + p.getName() + ".oldSpeed", oldSpeed.get(p));
        ReviveMe.getInstance().saveCache();
        setPotion(p);
    }

    public boolean isDamaged(Player p) {
        return playersPose.contains(p);
    }

    public void endPose(Player p, String cause) {
        debug("Revived, " + cause);
        float ospeed = 0.19F;
        boolean aospeed = false;
        if (ReviveMe.getInstance().cacheFile.exists() && ReviveMe.getInstance().cache.getString("players." + p.getName() + ".oldSpeed") != null) {
            ospeed = (float) ReviveMe.getInstance().cache.getDouble("players." + p.getName() + ".oldSpeed");
            aospeed = true;
            ReviveMe.getInstance().cache.set("players." + p.getName() + ".oldSpeed", null);
            ReviveMe.getInstance().saveCache();
        }

        p.setWalkSpeed(oldSpeed.get(p));
        if (!oldSpeed.get(p).equals(ospeed) && aospeed) {
            p.setWalkSpeed(oldSpeed.get(p));
        } else {
            p.setWalkSpeed(ospeed);
        }

        p.setGameMode(oldGameMode.getOrDefault(p, GameMode.SURVIVAL));

        playersPose.remove(p);
        p.setSwimming(false);
        p.setSprinting(false);
        Block b = p.getLocation().getBlock().getRelative(BlockFace.UP);
        p.sendBlockChange(b.getLocation(), b.getBlockData());
        invulnerabilityDelay.remove(p);
        relivingList.remove(p);

        if (cfg.getBoolean("info.scoreboard")) {
            p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }

        deathDelay.remove(p);
        if (cfg.getBoolean("info.title")) {
            p.sendTitle("", "");
        }

        List<Player> others = VectorUtils.getNear(100.0D, p);
        isendPlay.playStand(p, others);
        if (ReviveMe.getInstance().cacheFile.exists() && ReviveMe.getInstance().cache.getString("players." + p.getName() + ".oldSpeed") != null) {
            ReviveMe.getInstance().cache.set("players." + p.getName() + ".oldSpeed", null);
            ReviveMe.getInstance().saveCache();
        }

        double health = 0.5D;
        if (cfg.getString("relivedHealth") != null) {
            health = cfg.getDouble("relivedHealth");
        }

        if (!p.isDead()) {
            if (!usedTotem.contains(p)) {
                p.setHealth(health);
            } else {
                usedTotem.remove(p);
            }
        }

        removePotion(p);
        List<HumanEntity> viewList = p.getInventory().getViewers();

        for (HumanEntity he : viewList) {
            he.closeInventory();
        }
    }

    public void play(List<Player> others, Player target) {
        isendPlay.play(others, target);
    }

    public void sendActionBar(Player p, String text, int duration, Boolean time) {
        if (time) {
            iactionbarutil.sendActionBarMessage(p, text, duration, ReviveMe.getInstance().getPlugin());
        } else {
            iactionbarutil.sendActionBarMessage(p, text);
        }

    }

    public void toggleRelive(Player p, Player p2) {
        if (!p.isSneaking()) {
            if (!relivingList.contains(p2)) {
                startReliving(p, p2);
            }
        } else {
            cancelReliving(p, p2);
        }

    }

    public void startReliving(Player p, Player p2) {
        p.sendMessage(cfg.getString("messages.reliving.player").replace("&", "§").replace("<VICTIM>", p2.getName()));
        p2.sendMessage(cfg.getString("messages.reliving.victim").replace("&", "§").replace("<PLAYER>", p.getName()));
        if (cfg.getBoolean("info.scoreboard")) {
            Spigboard b1 = board1.get(p2);
            b1.add(p);
        }

        relivingPlayer.put(p, p2);
        relivingPlayerOther.put(p2, p);
        relivingCount.put(p2, 0);
        relivingList.add(p2);
    }

    public void cancelReliving(Player p, Player p2) {
        if (relivingCount.containsKey(p2) && relivingPlayer.containsKey(p)) {
            p.sendMessage(cfg.getString("messages.cancelReliving.player").replace("&", "§").replace("<VICTIM>", p2.getName()));
            p2.sendMessage(cfg.getString("messages.cancelReliving.victim").replace("&", "§").replace("<PLAYER>", p.getName()));
            if (cfg.getBoolean("info.scoreboard")) {
                Spigboard b1 = board1.get(p2);
                SpigboardEntry score = b1.getEntry("count");
                SpigboardEntry score2 = b1.getEntry("status");

                try {
                    score.update("§7██████████");
                    score2.update((boardStatus + boardWaiting).replace("&", "§"));
                } catch (IllegalStateException ignored) {
                }

                b1.add(p2);
                b1.add(p);
                p.setScoreboard(Bukkit.getServer().getScoreboardManager().getMainScoreboard());
            }

            relivingPlayerOther.remove(p2);
            relivingPlayer.remove(p);
            relivingCount.remove(p2);
            relivingList.remove(p2);
        }

    }

    public void reliveAll() {
        if (!playersPose.isEmpty()) {
            for (Player p : playersPose) {
                relivingCount.remove(p);
                endPose(p, "All. Code: 007");
            }
        }

        if (!relivingPlayer.isEmpty()) {
            for (Player p : relivingPlayer.keySet()) {
                relivingPlayer.remove(p);
            }
        }

    }

    public void debug(String message) {

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (debugPlayers.contains(p)) {
                p.sendMessage("§6[§cDebug§6]§e " + message);
            }
        }

    }

    public void countSeconds() {
        int invulnerability;
        for (Player p : relivingCount.keySet()) {
            relivingCount.put(p, relivingCount.get(p) + 1);
            Player p2 = relivingPlayerOther.get(p);
            if (p2 != null) {
                int value = relivingCount.get(p);
                if (cfg.getBoolean("info.scoreboard")) {
                    Spigboard b1 = board1.get(p);
                    SpigboardEntry score = b1.getEntry("count");
                    SpigboardEntry score2 = b1.getEntry("status");

                    try {
                        score.update(getCountText(value));
                        score2.update((boardStatus + boardReliving).replace("&", "§"));
                    } catch (IllegalStateException ignored) {
                    }
                }

                p2.spawnParticle(Particle.HEART, p.getLocation(), 1);
                Random r = new Random();
                invulnerability = cfg.getInt("sounds.reliving");
                if (invulnerability > 9) {
                    invulnerability = 1;
                }
                //p2.playNote(p2.getLocation(), Instrument.values()[invulnerability], Note.natural(1 + r.nextInt(20), Note.Tone.F));
                //p.playNote(p2.getLocation(), Instrument.values()[invulnerability], Note.natural(1 + r.nextInt(20), Note.Tone.F));
                // 能用就行，who care
                p2.playNote(p2.getLocation(), (byte) invulnerability, (byte) (r.nextInt(20) + 1));
                p.playNote(p2.getLocation(), (byte) invulnerability, (byte) (r.nextInt(20) + 1));
                if (cfg.getBoolean("info.title")) {
                    p2.sendTitle(getCountText(value), "", 1, 20, 1);
                }

                if (relivingCount.get(p) >= relivingTime) {
                    relivingCount.remove(p);
                    relivingPlayer.remove(p2);
                    endPose(p, "For player. code: 008");
                    p.sendMessage(cfg.getString("messages.revivedSuccessfully.victim").replace("&", "§").replace("<PLAYER>", p2.getName()));
                    p2.sendMessage(cfg.getString("messages.revivedSuccessfully.player").replace("&", "§").replace("<VICTIM>", p.getName()));
                    if (cfg.getBoolean("info.scoreboard")) {
                        p2.setScoreboard(Bukkit.getServer().getScoreboardManager().getMainScoreboard());
                    }

                    p.playSound(p.getLocation(), Sound.valueOf(cfg.getString("sounds.revivedSuccessfully").toUpperCase()), 1.0F, 1.0F);
                    p.playSound(p.getLocation(), Sound.valueOf(cfg.getString("sounds.revivedSuccessfully").toUpperCase()), 1.0F, 1.0F);
                    p2.resetTitle();
                }
            }
        }

        for (Player p : deathDelay.keySet()) {
            int value = deathDelay.get(p);
            if (value >= 0) {
                if (!relivingList.contains(p)) {
                    deathDelay.put(p, value - 1);
                    if (cfg.getBoolean("info.scoreboard")) {
                        Spigboard b1 = board1.get(p);
                        SpigboardEntry score4 = b1.getEntry("deathCount");

                        try {
                            score4.update(boardDeathIn.replace("&", "§").replace("<TIME>", getDeathCountText(value)));
                        } catch (IllegalStateException ignored) {
                        }
                    }
                }
            } else {
                deathDelay.remove(p);
                forceDeath(p);
            }

            if (cfg.getBoolean("info.title")) {
                String status = boardWaiting;
                String bar = "§7██████████";
                if (relivingList.contains(p)) {
                    status = boardReliving;
                    if (relivingCount.containsKey(p)) {
                        bar = getCountText((double) relivingCount.get(p));
                    }
                }

                invulnerability = 0;
                if (invulnerabilityDelay.containsKey(p)) {
                    invulnerability = invulnerabilityDelay.get(p);
                }

                p.sendTitle(
                        cfg.getString("title.title")
                                .replace("<STATUS>", status)
                                .replace("<BAR>", bar)
                                .replace("&", "§"),
                        cfg.getString("title.subTitle")
                                .replace("<DEATHTIME>", getDeathCountText(value))
                                .replace("<INVULNERABILITY>", boardInvulnerableFor.replace("&", "§").replace("<TIME>", getDeathCountText(invulnerability)))
                                .replace("&", "§"),
                        1, 20, 1);
            }
        }

        if (!invulnerabilityDelay.isEmpty()) {
            for (Player p : invulnerabilityDelay.keySet()) {
                int value = invulnerabilityDelay.get(p);
                if (value >= 1) {
                    invulnerabilityDelay.put(p, value - 1);
                    if (cfg.getBoolean("info.scoreboard")) {
                        Spigboard b1 = board1.get(p);
                        SpigboardEntry score4 = b1.getEntry("invulnerableCount");

                        try {
                            score4.update(boardInvulnerableFor.replace("&", "§").replace("<TIME>", getDeathCountText(value)));
                        } catch (IllegalStateException ignored) {
                        }
                    }
                } else {
                    invulnerabilityDelay.remove(p);
                    if (cfg.getBoolean("info.scoreboard")) {
                        Spigboard b1 = board1.get(p);
                        SpigboardEntry score4 = b1.getEntry("invulnerableCount");

                        try {
                            score4.update(boardVulnerable.replace("&", "§"));
                        } catch (IllegalStateException ignored) {
                        }
                    }
                }
            }
        }

        if (!playersPose.isEmpty()) {
            for (Player p : playersPose) {
                p.setHealth(0.1D);
                p.setGliding(false);
            }
        }

    }

    public void forceDeath(Player p) {
        boolean totem = equipTotem(p);
        if (totem) {
            usedTotem.add(p);
            forcedDeath.add(p);
            p.damage(p.getHealth() + 20.0D);
            endPose(p, "ForceDeath usedTotem. code: 009");
        } else {
            p.setHealth(0.0D);
            endPose(p, "ForceDeath NotUsedTotem. code: 010");
        }
        EntityDamageEvent e = damageEvents.get(p);
        if (e != null) {
            e.setDamage(20.0D);
            p.setLastDamageCause(e);
            p.setLastDamage(e.getFinalDamage());
        } else {
            e = new EntityDamageEvent(p, DamageCause.FALL, 0.0D);
            e.setDamage(20.0D);
            p.setLastDamageCause(e);
            p.setLastDamage(e.getFinalDamage());
        }
    }

    public boolean equipTotem(Player p) {
        return (!p.getInventory().getItemInOffHand().getType().equals(Material.AIR)
                && p.getInventory().getItemInOffHand().getType().equals(Material.TOTEM_OF_UNDYING))
                || (!p.getInventory().getItemInMainHand().getType().equals(Material.AIR)
                && p.getInventory().getItemInMainHand().getType().equals(Material.TOTEM_OF_UNDYING));
    }

    public int getValue(int value) {
        int st = 10;
        return value / st * relivingTime;
    }

    public String getCountText(double value) {
        int count = (int) (value / (double) relivingTime * 10.0D);
        StringBuilder text = new StringBuilder("██████████");
        if (count <= 10)
            text.insert(count, "§7");
        text.insert(0, "§a");
        return text.toString();
    }

    public String getDeathCountText(int value) {
        String text;
        int minutes = value / 60;
        int seconds = value - minutes * 60;
        String m = String.valueOf(minutes);
        String s = String.valueOf(seconds);
        if (minutes / 10 == 0) {
            m = "0" + m;
        }

        if (seconds / 10 == 0) {
            s = "0" + s;
        }

        text = m + ":" + s;
        if (minutes == 0) {
            text = s;
        }

        return text;
    }

    public void saveNewConfig(String text, boolean value) {
        File configfile = new File(ReviveMe.getInstance().getDataFolder(), "reviveme.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configfile);
        if (config.getString(text) == null) {
            cfg.set(text, value);
        }

    }

    public boolean hasPermission(CommandSender p, String permission) {
        boolean haspermission = false;
        if (p.hasPermission(permission)) {
            haspermission = true;
        } else if (!permission.equalsIgnoreCase("ReviveMe.shotdown") && !permission.equalsIgnoreCase("ReviveMe.itemstealVictim")) {
            p.sendMessage(cfg.getString("messages.noPermission")
                    .replace("&", "§")
                    .replace("<PERMISSION>", permission));
            if (p instanceof Player) {
                Player player = (Player) p;
                player.playSound(player.getLocation(), Sound.valueOf(cfg.getString("sounds.noPermission").toUpperCase()), 1.0F, 1.0F);
            }
        }

        return haspermission;
    }

    public void onReload() {
        enableWorldsEnable = cfg.getBoolean("enableWorldsEnable");
        disableWorldsEnable = cfg.getBoolean("disbleWorldsEnable");
        invulnerability = cfg.getInt("delays.invulnerabilityDelay");
        enableWorlds = cfg.getStringList("enableWorlds");
        disableWorlds = cfg.getStringList("disableWorlds");
        firstTotem = cfg.getBoolean("totem-first");
        potionEnable = cfg.getBoolean("potion.enable");
        potionEffect = cfg.getString("potion.effect");
        potionLevel = cfg.getInt("potion.level");

        boardTitle = cfg.getString("scoreboard.title");
        boardStatus = cfg.getString("scoreboard.status");
        boardWaiting = cfg.getString("scoreboard.waiting");
        boardReliving = cfg.getString("scoreboard.reliving");
        boardDeathIn = cfg.getString("scoreboard.deathIn");
        boardInvulnerableFor = cfg.getString("scoreboard.invulnerableFor");
        boardVulnerable = cfg.getString("scoreboard.vulnerable");
        speed = Util.getFloatFromConfig(cfg, "playersConfig.speed", 0.04F);
        title = cfg.getString("title.title");
        subTitle = cfg.getString("title.subTitle");
        potionEnable = cfg.getBoolean("potion.enable");
        potionEffect = cfg.getString("potion.effect");
        potionLevel = cfg.getInt("potion.level");
    }

    public void setPotion(Player p) {
        if (potionEnable && potionEffect != null) {
            PotionEffectType type = PotionEffectType.getByName(potionEffect.toLowerCase());
            if(type == null) return;
            p.addPotionEffect(new PotionEffect(type, -1, potionLevel));
        }
    }

    public void removePotion(Player p) {
        if (potionEnable && !p.getActivePotionEffects().isEmpty()) {
            for (PotionEffect pe : p.getActivePotionEffects()) {
                if (pe.getType().getName().equalsIgnoreCase(potionEffect) && pe.getAmplifier() == potionLevel) {
                    p.removePotionEffect(pe.getType());
                }
            }
        }
    }
}
