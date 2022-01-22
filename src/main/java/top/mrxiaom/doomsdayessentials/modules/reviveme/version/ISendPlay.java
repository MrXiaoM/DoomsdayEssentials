package top.mrxiaom.doomsdayessentials.modules.reviveme.version;

import org.bukkit.entity.Player;

import java.util.List;

public interface ISendPlay {
    void play(List<Player> var1, Player var2);

    void playStand(Player var1, List<Player> var2);
}
