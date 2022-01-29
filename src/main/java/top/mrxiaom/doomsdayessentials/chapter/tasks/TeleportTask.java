package top.mrxiaom.doomsdayessentials.chapter.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import top.mrxiaom.doomsdayessentials.chapter.IChapterTask;

public class TeleportTask implements IChapterTask<IChapterTask.NoEvent> {
    World world;
    int x;
    int y;
    int z;
    public TeleportTask(World world, int x, int y, int z){
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    @Override
    public String toString(){
        return "teleport:" + world.getName() + "," + x + "," + y + "," + z;
    }
    @Override
    public String display() {
        return null;
    }

    @Override
    public void start(Player player) {
        try {
            player.teleport(new Location(world, x + (x < 0 ? -1 : 1) * 0.5D, y + 0.1D, z + (z < 0 ? -1 : 1) * 0.5D));
        }catch(Throwable ignored){
        }
        next(player);
    }

    @Override
    public void end(Player player) {

    }
}
