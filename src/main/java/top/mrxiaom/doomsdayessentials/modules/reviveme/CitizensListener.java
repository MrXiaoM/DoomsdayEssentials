package top.mrxiaom.doomsdayessentials.modules.reviveme;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

public class CitizensListener {
    public static boolean isNpc(Entity e) {
        return e.hasMetadata("NPC");
    }

    public static void debug(String message) {
        Bukkit.broadcastMessage(message);
    }
}
