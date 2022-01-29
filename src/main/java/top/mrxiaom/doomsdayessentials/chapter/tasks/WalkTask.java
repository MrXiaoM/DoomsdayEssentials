package top.mrxiaom.doomsdayessentials.chapter.tasks;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import top.mrxiaom.doomsdayessentials.chapter.IChapterTask;

public class WalkTask implements IChapterTask<PlayerMoveEvent> {
    String worldName;
    int x;
    int y;
    int z;
    public WalkTask(String worldName, int x, int y, int z){
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    @Override
    public String toString(){
        return "walk:" + worldName + "," + x + "," + y + "," + z;
    }
    @Override
    public String display() {
        return null;
    }

    @Override
    public void execute(Player player, PlayerMoveEvent event) {
        Location to = event.getTo();
        if(player.getWorld().getName().equals(worldName)
                && (int)player.getLocation().getX() == x
                && (int)player.getLocation().getY() == y
                && (int)player.getLocation().getZ() == z){
            next(player);
        }
    }

    @Override
    public void start(Player player) {

    }

    @Override
    public void end(Player player) {

    }
}
