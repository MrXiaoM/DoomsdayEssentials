package top.mrxiaom.doomsdayessentials.chapter;

import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.Util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChapterPlayer {
	// 玩家名
	final String name;
	// 已完成章节的ID
	final List<String> doneChapters;
	// 进行中的章节
	final ProcessChapter processChapter;
	// 特殊配置
	Map<String, Object> values = new HashMap<>();
	public ChapterPlayer(String name, List<String> doneChapters, ProcessChapter processChapter, Map<String, Object> values) {
		this.name = name;
		this.doneChapters = doneChapters;
		this.processChapter = processChapter;
		this.values = values;
	}
	
	@Nullable
	public OfflinePlayer getPlayer() {
		return Util.getOfflinePlayer(name);
	}
	
	public String getName() {
		return name;
	}
	
	public List<String> getDoneChapterIDs(){
		return doneChapters;
	}
	
	public List<Chapter> getDoneChapters(){
		List<Chapter> result = new ArrayList<>();
		for(Chapter chapter : Main.getInstance().getChapterManager().getChapters()) {
			if(doneChapters.contains(chapter.getId())) {
				result.add(chapter);
			}
		}
		return result;
	}
	
	public static ChapterPlayer fromYamlConfig() {
		return null;
	}
	
	public YamlConfiguration toYamlConfig() {
		return null;
	}
}
