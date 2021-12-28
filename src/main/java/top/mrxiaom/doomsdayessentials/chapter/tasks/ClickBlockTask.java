package top.mrxiaom.doomsdayessentials.chapter.tasks;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import top.mrxiaom.doomsdayessentials.chapter.IChapterTask;

public class ClickBlockTask implements IChapterTask<PlayerInteractEvent> {
    String worldName;
    int x;
    int y;
    int z;
    Action action;
    BlockFace face;
    public ClickBlockTask(String worldName, int x, int y, int z){
        this(worldName, x, y, z, null);
    }
    public ClickBlockTask(String worldName, int x, int y, int z, Action action){
        this(worldName, x, y, z, action, null);
    }
    public ClickBlockTask(String worldName, int x, int y, int z, Action action, BlockFace face){
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.action = action;
        this.face = face;
    }
    @Override
    public String toString(){
        return "clickblock:" + worldName + "," + x + "," + y + "," + z + (action != null ? ("," + action.name() + (face != null ? ("," + face.name()) : "")) : "");
    }
    @Override
    public String display() {
        return null;
    }

    @Override
    public void execute(Player player, PlayerInteractEvent event) {
        if (event.hasBlock()){
            Block block = event.getClickedBlock();
            if(block.getWorld().getName().equals(worldName)
                && block.getX() == x
                && block.getY() == y
                && block.getZ() == z
                && (action == null || event.getAction().equals(action)
                && (face == null || event.getBlockFace().equals(face)))) {
                event.setCancelled(true);
                next(player);
            }
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
