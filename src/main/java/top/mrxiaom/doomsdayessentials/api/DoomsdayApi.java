package top.mrxiaom.doomsdayessentials.api;

import com.google.common.collect.Lists;
import fr.xephi.authme.api.v3.AuthMeApi;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.manager.UUIDManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.configs.KitConfig;
import top.mrxiaom.doomsdayessentials.configs.ParkourConfig;
import top.mrxiaom.doomsdayessentials.configs.PlayerConfig;
import top.mrxiaom.doomsdayessentials.configs.TagConfig;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoomsdayApi {
    public enum PlayerGroup {
        UNREGISTER, DEFAULT, VIP, SVIP, OP
    }
    public static List<TagConfig.Tag> getPlayerTags(String player){
        return Lists.newArrayList(Main.getInstance().getTagConfig().getPlayerTags(player).values());
    }
    public static int getPlayerNeedles(String player) {
        return Main.getInstance().getPlayerConfig().getNeedle(player);
    }
    public static PlayerGroup getPlayerGroup(String player){
        OfflinePlayer p = Util.getOfflinePlayer(player);
        if (p == null || !AuthMeApi.getInstance().isRegistered(player)) return PlayerGroup.UNREGISTER;
        if (p.isOp()) return PlayerGroup.OP;
        if (Main.getInstance().getPermsApi().playerHas("", p, "servermarket.tax.svip")) return PlayerGroup.SVIP;
        if (Main.getInstance().getPermsApi().playerHas("", p, "servermarket.tax.vip")) return PlayerGroup.VIP;
        return PlayerGroup.DEFAULT;
    }
    public static double getPlayerMoney(String player) {
        OfflinePlayer p = Util.getOfflinePlayer(player);
        return p == null ? 0 : Main.getInstance().getEcoApi().getBalance(p);
    }
    public static boolean isPlayerRegistered(String player) {
        return AuthMeApi.getInstance().isRegistered(player);
    }
    public static List<PlayerConfig.LastSignInfo> getKitsUseInfo(String player) {
        List<PlayerConfig.LastSignInfo> result = new ArrayList<>();
        for (KitConfig.Kit kit :  Main.getInstance().getKitConfig().getAllKits().values()) {
            PlayerConfig.LastSignInfo info = Main.getInstance().getPlayerConfig().getLastSignInfo(player, kit.getId());
            if(info.signTime != null) result.add(info);
        }
        return result;
    }
    public static Map<String ,Integer> getParkoursProcess(String player) {
        Map<String, Integer> result = new HashMap<>();
        for (ParkourConfig.Parkour parkour : Main.getInstance().getParkoursConfig().all()) {
            FileConfiguration config = Main.getInstance().getPlayerConfig().getConfig();
            String key = player + ".parkours." + parkour.getId() + ".checkpoint";
            if(config.contains(key)) result.put(parkour.getDisplayName(), config.getInt(key, 0));
        }
        return result;
    }
    public static boolean isPlayerMuted(String player) {
        return PunishmentManager.get().isMuted(UUIDManager.get().getUUID(player));
    }
    public static boolean isPlayerCursed(String player){
        return Main.getInstance().getPlayerConfig().getConfig().getBoolean(player + ".curse", false);
    }
}
