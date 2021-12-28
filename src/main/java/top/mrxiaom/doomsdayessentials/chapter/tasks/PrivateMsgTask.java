package top.mrxiaom.doomsdayessentials.chapter.tasks;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import top.mrxiaom.doomsdayessentials.chapter.IChapterTask;

public class PrivateMsgTask implements IChapterTask<Event> {
	final String msg;
	public PrivateMsgTask(String msg) {
		this.msg = msg;
	}
	@Override
	public String toString(){
		return "msg:" + msg;
	}
	@Override
	public String display() {
		return "";
	}

	@Override
	public void execute(Player player, Event event) {
	}

	@Override
	public void start(Player player) {
		player.sendMessage(PlaceholderAPI.setPlaceholders(player, msg));
		next(player);
	}

	@Override
	public void end(Player player) {
	}

	@Override
	public boolean hasEvent() {
		return false;
	}

}
