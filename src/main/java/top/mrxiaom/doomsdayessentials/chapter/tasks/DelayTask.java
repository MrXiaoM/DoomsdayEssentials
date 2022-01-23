package top.mrxiaom.doomsdayessentials.chapter.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitTask;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.chapter.IChapterTask;

import java.util.HashMap;
import java.util.Map;

public class DelayTask implements IChapterTask<Event>{
	final int ticks;
	public DelayTask(int ticks) {
		this.ticks = ticks;
	}
	final Map<String, BukkitTask> tasks = new HashMap<>();
	@Override
	public void start(Player player) {
		tasks.put(player.getName(), Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> next(player), ticks));
	}
	@Override
	public String toString() {
		return "delay:" + ticks;
	}
	@Override
	public void end(Player player) {
		if(tasks.containsKey(player.getName())) {
			BukkitTask task = tasks.get(player.getName());
			if(!task.isCancelled()) task.cancel();
			tasks.remove(player.getName());
		}
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
