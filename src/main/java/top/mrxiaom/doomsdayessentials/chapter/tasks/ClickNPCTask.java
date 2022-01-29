package top.mrxiaom.doomsdayessentials.chapter.tasks;

import me.clip.placeholderapi.PlaceholderAPI;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdayessentials.chapter.IChapterTask;

public class ClickNPCTask implements IChapterTask<NPCClickEvent>{
	public enum ClickType{
		LEFT, RIGHT
	}
	final int id;
	final ClickType type;
	final boolean shift;
	final String shiftTips;
	public ClickNPCTask(int id, ClickType type, boolean shift, String shiftTips) {
		this.id = id;
		this.type = type;
		this.shift = shift;
		this.shiftTips = shiftTips;
	}
	@Override
	public String toString(){
		return "clicknpc:" + id + "," + type.name() + (shift ? (",shift" + (shiftTips.length() > 0 ? ("," + shiftTips) : "")) : "");
	}
	@Override
	public String display() {
		// TODO 自动生成的方法存根
		return null;
	}

	@Override
	public void execute(Player player, NPCClickEvent event) {
		if(event.getNPC().getId() == id) {
			if(shift && !player.isSneaking()) {
				if(shiftTips.length() > 0) {
					player.sendMessage(PlaceholderAPI.setPlaceholders(player, shiftTips));
				}
				return;
			}
			if((type.equals(ClickType.LEFT) && event instanceof NPCLeftClickEvent) || 
					(type.equals(ClickType.RIGHT) && event instanceof NPCRightClickEvent)) {
				next(player);
			}
		}
	}

	@Override
	public void start(Player player) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void end(Player player) {
		// TODO 自动生成的方法存根
		
	}
}
