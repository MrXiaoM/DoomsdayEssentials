package top.mrxiaom.doomsdayessentials;

import com.bekvon.bukkit.residence.api.ResidenceApi;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.concurrent.atomic.AtomicInteger;

public class Cleaner {
    Main plugin;
    String cleanResult = "";
    int cleanerWaitTimer = 0;
    int cleanerAniTimer = 0;
    int cleanProcessTimer = 20;
    BossBar cleanerBossbar;
    private long lastActionTime = System.currentTimeMillis();
    private long cleanTimer;
    public Cleaner(Main plugin) {
        this.plugin = plugin;
        this.cleanTimer = Main.cleanTimerDefault;
        cleanerBossbar = Bukkit.createBossBar(I18n.t("cleaner.idle"), BarColor.WHITE, BarStyle.SEGMENTED_6);
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::onTick, 1, 1);
    }


    private void onTick() {
        if (Bukkit.getOnlinePlayers().size() < 1) return;
        if (cleanProcessTimer > 0){
            cleanProcessTimer--;
        }
        else {
            cleanProcessTimer = 20;
            cleanTimer--;
            onCleanSecond();
        }
        if (cleanerAniTimer > 0) {
            cleanerAniTimer--;
            for (Player p : Bukkit.getOnlinePlayers()){
                if (cleanerBossbar.getPlayers().contains(p)) continue;
                cleanerBossbar.addPlayer(p);
            }
            String cleanerAniPrefix = I18n.t("cleaner.loading-prefix");
            String cleanerAniText = I18n.t("cleaner.loading");
            cleanerBossbar.setTitle(cleanerAniPrefix + cleanerAniText.substring(0, Math.min(36 - (cleanerAniTimer / 2) , cleanerAniText.length())));
            cleanerBossbar.setProgress(1.0F - (float)cleanerAniTimer / 36);
            cleanerBossbar.setVisible(true);
        }
        if (cleanTimer <= 60) {
            for (Player p : Bukkit.getOnlinePlayers()){
                if (cleanerBossbar.getPlayers().contains(p)) continue;
                cleanerBossbar.addPlayer(p);
            }
            cleanerBossbar.setTitle(I18n.t("cleaner.clean-pre").replace("%time%", String.valueOf(cleanTimer)));
            cleanerBossbar.setProgress(((float)(cleanTimer - 1) * 20.0F + (float)cleanProcessTimer) / 1200.0F);
            cleanerBossbar.setVisible(true);
            return;
        }
        if (cleanerWaitTimer > 0) {
            cleanerBossbar.setTitle(cleanResult);
            cleanerBossbar.setProgress(1.0F - (float)Math.min(cleanerWaitTimer, 60) / 60);
            if(cleanerWaitTimer <= 70) cleanerWaitTimer--;
            if (cleanerWaitTimer <= 0){
                cleanerBossbar.setVisible(false);
                cleanerBossbar.removeAll();
                cleanerBossbar.setTitle(I18n.t("cleaner.idle"));
            }
        }
    }

    private void onCleanSecond() {
        if (this.plugin.tps.getAverageTPS() < 15 && lastActionTime + 60000 < System.currentTimeMillis()) {
            lastActionTime = System.currentTimeMillis();
            int count = 0;
            for (World world : Bukkit.getWorlds()) {
                for (Entity entity : world.getEntities()) {
                    if (Util.checkCustomNpc(entity)) {
                        continue;
                    }
                    if (entity instanceof Monster) {
                        if (entity.getCustomName() == null) {
                            entity.remove();
                            count++;
                        }
                    }
                }
            }
            if (count > 0) {
                Util.alert(I18n.t("cleaner.clean-monster").replace("%count%", String.valueOf(count)));
            }
            cleanTimer = 0;
            System.runFinalization();
            System.gc();
        }
        if(cleanTimer == 62) cleanerAniTimer = 36;
        if (cleanTimer == 60 || cleanTimer == 30 || cleanTimer == 10) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1.0F, 1.0F);
            }
            //Util.alert(I18n.t("cleaner.clean-pre").replace("%time%", String.valueOf(cleanTimer)));
        }
        if (cleanTimer <= 0) {
            cleanResult = I18n.t("cleaner.cleaning");
            cleanerWaitTimer = 71;
            this.cleanTimer = Main.cleanTimerDefault;
            AtomicInteger count = new AtomicInteger();
            AtomicInteger countM = new AtomicInteger();
            for (World world : Bukkit.getWorlds()) {
                // 同步
                Bukkit.getScheduler().runTask(this.plugin, ()-> {
                    for (Entity e : world.getEntities()) {
                        if (e.getType() == EntityType.DROPPED_ITEM) {
                            e.remove();
                            count.getAndIncrement();
                        }
                        if (e.getType() == EntityType.ARROW && e.isOnGround()) {
                            e.remove();
                        }
                    }
                    for (Entity entity : world.getEntities()) {
                        if (Util.checkCustomNpc(entity) || Util.isNoClearEntities(entity)) {
                            continue;
                        }
                        if (entity instanceof Monster) {
                            if (ResidenceApi.getResidenceManager().getByLoc(entity.getLocation()) == null) {
                                if (entity.getCustomName() == null || entity.getCustomName().length() == 0) {
                                    entity.remove();
                                    countM.getAndIncrement();
                                }
                            }
                        }
                    }
                    world.save();
                });
            }
            cleanResult = (I18n.t("cleaner.clean-done").replace("%items%", String.valueOf(count.get())).replace("%monsters%",
                    String.valueOf(countM.get())));
            cleanerWaitTimer = 70;
        }
    }

    public BossBar getBossbar(){
        return cleanerBossbar;
    }

    public long getCleanTime() {
        return cleanTimer;
    }
    public void setCleanTime(int cleanSecond) {
        cleanTimer = Math.min(Main.cleanTimerDefault, cleanSecond);
    }
}
