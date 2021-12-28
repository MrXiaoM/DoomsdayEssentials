package top.mrxiaom.doomsdayessentials.chapter.tasks;

import org.bukkit.Location;
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
        Location from = event.getFrom();
        Location to = event.getTo();
        if(!(from.getWorld().getName().equals(to.getWorld().getName())
            && from.getBlockX() == to.getBlockX()
            && from.getBlockY() == to.getBlockY()
            && from.getBlockZ() == to.getBlockZ())
            && (to.getWorld().getName().equals(worldName)
            && to.getBlockX() == x
            && to.getBlockY() == y
            && to.getBlockZ() == z)) {
            next(player);
        }
    }

    @Override
    public void start(Player player) {

    }

    @Override
    public void end(Player player) {

    }

    @Override
    public boolean hasEvent() {
        return true;
    }
}
