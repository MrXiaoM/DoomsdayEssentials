package top.mrxiaom.doomsdayessentials.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.entity.Player;

public class FunnyFunction {
    /**
     * 发包让玩家假死，因为不是真的死了，玩家点击重生会被当作无效操作
     * 使用 Util.updateHealth(Player) 更新一下玩家血量就能恢复了
     * 
     * @author MrXiaoM
     **/
    public static void fakeDeath(Player player) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.UPDATE_HEALTH));
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }
}
