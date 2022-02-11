package top.mrxiaom.doomsdayessentials;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;

public class AntiAntiAntiXray extends PacketAdapter {
    Main plugin;
    public AntiAntiAntiXray(Main plugin){
        super(plugin, ListenerPriority.NORMAL,
                PacketType.Play.Server.BLOCK_CHANGE,
                PacketType.Play.Server.MULTI_BLOCK_CHANGE);
        this.plugin = plugin;
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PacketContainer packet = event.getPacket();
        if (event.getPacketType().equals(PacketType.Play.Server.BLOCK_CHANGE)) {
            BlockPosition loc = packet.getBlockPositionModifier().read(0);

        }
    }
}
