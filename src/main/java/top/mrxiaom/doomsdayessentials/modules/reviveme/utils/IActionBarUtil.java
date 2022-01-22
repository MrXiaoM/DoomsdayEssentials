package top.mrxiaom.doomsdayessentials.modules.reviveme.utils;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public interface IActionBarUtil {
    Map PENDING_MESSAGES = new HashMap();

    void sendActionBarMessage(Player var1, String var2);

    void sendRawActionBarMessage(Player var1, String var2);

    void sendActionBarMessage(Player var1, String var2, int var3, Plugin var4);

    void cancelPendingMessages(Player var1);
}
