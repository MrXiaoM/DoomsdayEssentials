package top.mrxiaom.doomsdayessentials.chapter.tasks;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import top.mrxiaom.doomsdayessentials.chapter.IChapterTask;

public class TitleMsgTask implements IChapterTask<IChapterTask.NoEvent> {
	final String title;
	final String subtitle;
	final int fadeIn;
	final int time;
	final int fadeOut;

	public TitleMsgTask(String title, String subtitle, int fadeIn, int time, int fadeOut) {
		this.title = title;
		this.subtitle = subtitle;
		this.fadeIn = fadeIn;
		this.time = time;
		this.fadeOut = fadeOut;
	}
	@Override
	public String toString() {
		return "title:" + title + "," + subtitle + "," + fadeIn + "," + time + "," + fadeOut;
	}
	@Override
	public String display() {
		return "";
	}

	@Override
	public void start(Player player) {
		player.sendTitle(PlaceholderAPI.setPlaceholders(player, title), PlaceholderAPI.setPlaceholders(player, subtitle), fadeIn, time, fadeOut);
		next(player);
	}

	@Override
	public void end(Player player) {
	}
}
