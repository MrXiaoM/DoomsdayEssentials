package top.mrxiaom.doomsdayessentials.modules.reviveme;

import org.bukkit.entity.Player;

public class ReviveMeApi {
    public static void downPlayer(Player p) {
        ReviveMe.getInstance().getManager().startPose(p, "api");
        p.setHealth(0.1D);
    }

    public static void revivePlayer(Player p) {
        ReviveMe.getInstance().getManager().endPose(p, "api");
    }

    public static void reviveAll() {
        ReviveMe.getInstance().getManager().reliveAll();
    }

    public static int getRelivingCount(Player p) {
        int seconds = 0;
        if (ReviveMe.getInstance().getManager().relivingCount.containsKey(p)) {
            seconds = ReviveMe.getInstance().getManager().relivingCount.get(p);
        }

        return seconds;
    }

    public static Boolean isPlayerDowned(Player p) {
        return ReviveMe.getInstance().getManager().deathDelay.containsKey(p);
    }

    public static int geyDeathDelay(Player p) {
        int seconds = 0;
        if (ReviveMe.getInstance().getManager().deathDelay.containsKey(p)) {
            seconds = ReviveMe.getInstance().getManager().deathDelay.get(p);
        }

        return seconds;
    }

    public static Boolean isRelivingPlayer(Player p) {
        return ReviveMe.getInstance().getManager().relivingCount.containsKey(p);
    }
}
