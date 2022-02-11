package top.mrxiaom.doomsdayessentials.utils;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ResidenceUtil {
    public static boolean isPlayerHasFlag(ClaimedResidence res, String player, String flag, boolean def){
        if(res == null) return def;
        FlagPermissions flags = res.getPermissions();
        try{
            Class<ResidencePermissions> cls = ResidencePermissions.class;
            Class<FlagPermissions> cls2 = FlagPermissions.class;
            Field fieldWorld = cls.getDeclaredField("world");
            fieldWorld.setAccessible(true);
            Method playerHas = cls2.getDeclaredMethod("playerHas", String.class, String.class, String.class, boolean.class);
            playerHas.setAccessible(true);
            String world = (String) fieldWorld.get(flags);
            return (Boolean) playerHas.invoke(flags, player, world, flag, def);
        } catch (Throwable ignored){
        }
        return def;
    }
}
