package top.mrxiaom.doomsdayessentials.placeholder;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.ResidenceUtil;

public class PlaceholderResidence  extends PlaceholderExpansion {
    private final Main plugin;

    public PlaceholderResidence(Main plugin) {
        this.plugin = plugin;
    }

    public @NotNull String getAuthor() {
        return "mrxiaom";
    }

    public @NotNull String getIdentifier() {
        return "res";
    }

    public String onRequest(OfflinePlayer player, String identifier) {
        if (identifier.toLowerCase().startsWith("exists_")) {
            return plugin.getResidenceApi().getByName(identifier.substring(7)) != null ? "yes" : "no";
        }
        if (identifier.toLowerCase().startsWith("flag_")) {
            String s = identifier.substring(5);
            if (!s.contains("_")) return identifier;
            String flag = s.substring(0, s.indexOf("_"));
            String resName = s.substring(s.indexOf("_") + 1);
            ClaimedResidence res = plugin.getResidenceApi().getByName(resName);
            if (res == null || !res.getPermissions().isPlayerSet(player.getName(), flag)) return "undefined";
            return ResidenceUtil.isPlayerHasFlag(res, player.getName(), flag, false) ? "yes" : "no";
        }
        if (identifier.toLowerCase().startsWith("get_")) {
            String s = identifier.substring(4);
            if (!s.contains("_")) return identifier;
            ClaimedResidence res = plugin.getResidenceApi().getByName(s.substring(s.lastIndexOf("_") + 1));
            if (res == null) return "?";
            String operator = s.substring(0, s.lastIndexOf("_"));
            if (operator.equalsIgnoreCase("owner")) {
                return res.getOwner();
            }
            if (operator.equalsIgnoreCase("is_owner")) {
                return res.isOwner(player.getName()) ? "yes" : "no";
            }
            if (operator.equalsIgnoreCase("²")){
                CuboidArea area = res.getMainArea();
                return (area.getXSize() * area.getZSize()) + "m²";
            }
            if (operator.equalsIgnoreCase("³")){
                CuboidArea area = res.getMainArea();
                return (area.getXSize() * area.getYSize() * area.getZSize()) + "m³";
            }
            if (operator.equalsIgnoreCase("size")) {
                CuboidArea area = res.getMainArea();
                return area.getXSize() + "*" + area.getYSize() + "*" + area.getZSize();
            }
            if (operator.equalsIgnoreCase("size_x")){
                CuboidArea area = res.getMainArea();
                return String.valueOf(area.getXSize());
            }
            if (operator.equalsIgnoreCase("size_y")){
                CuboidArea area = res.getMainArea();
                return String.valueOf(area.getYSize());
            }
            if (operator.equalsIgnoreCase("size_z")){
                CuboidArea area = res.getMainArea();
                return String.valueOf(area.getZSize());
            }
            if (operator.equalsIgnoreCase("worth")) {
                return String.valueOf(res.getWorth());
            }
        }
        if (identifier.toLowerCase().startsWith("is_menu_access_")) {
            ClaimedResidence res = plugin.getResidenceApi().getByName(identifier.substring(15));
            if (res == null) return "no";
            if (player.isOp() || res.isOwner(player.getName()) || ResidenceUtil.isPlayerHasFlag(res,player.getName(), "admin", false))
                return "yes";
            return "no";
        }
        return identifier;
    }

    public @NotNull String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    public boolean persist() {
        return true;
    }

    public boolean canRegister() {
        return true;
    }

}
