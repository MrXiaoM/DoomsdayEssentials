package top.mrxiaom.doomsdayessentials.chapter.tasks;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import top.mrxiaom.doomsdayessentials.chapter.IChapterTask;

public class ConsoleCommandTask implements IChapterTask<Event>{
	final String cmd;
	public ConsoleCommandTask(String cmd) {
		this.cmd = cmd;
	}
	@Override
	public void start(Player player) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderAPI.setPlaceholders(player, cmd));
		next(player);
	}
	@Override
	public String toString(){
		return "console:" + cmd;
	}
	@Override
	public void end(Player player) {
		
	}
	
	@Override
	public String display() {
		return "";
	}

	@Override
	public void execute(Player player, Event event) {
		// do nothing.
	}

	@Override
	public boolean hasEvent() {
		return false;
	}
}
