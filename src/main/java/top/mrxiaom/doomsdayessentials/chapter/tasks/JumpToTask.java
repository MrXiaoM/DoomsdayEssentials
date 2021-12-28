package top.mrxiaom.doomsdayessentials.chapter.tasks;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import top.mrxiaom.doomsdayessentials.chapter.IChapterTask;

public class JumpToTask implements IChapterTask<Event>{
	final int index;
	public JumpToTask(int index) {
		this.index = index;
	}
	@Override
	public String toString(){
		return "jumpto:" + index;
	}
	@Override
	public void start(Player player) {
		jump(player, index);
	}

	@Override
	public void end(Player player) { }

	@Override
	public String display() {
		return "";
	}

	@Override
	public void execute(Player player, Event event) { }

	@Override
	public boolean hasEvent() {
		return false;
	}
}
