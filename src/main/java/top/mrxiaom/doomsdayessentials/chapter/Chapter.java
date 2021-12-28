package top.mrxiaom.doomsdayessentials.chapter;

import com.google.common.collect.Lists;
import org.bukkit.configuration.file.YamlConfiguration;
import top.mrxiaom.doomsdayessentials.Main;

import javax.annotation.Nullable;
import java.util.List;

public class Chapter {
	final String id;
	final String name;
	final List<IChapterTask> tasks;
	public Chapter(String id, String name, List<IChapterTask> tasks) {
		this.id = id;
		this.name = name;
		this.tasks = tasks;
	}
	/**
	 * 从配置文件读取
	 * @param cfg
	 * @return
	 */
	@Nullable
	public static Chapter fromYamlConfig(YamlConfiguration cfg) {
		try {
			String id = cfg.getString("id");
			String name = cfg.getString("name");
			if(id == null || name == null) throw new NullPointerException("章节 " + cfg.getName() + " 的 ID 或名称不能为空");
			List<IChapterTask> tasks = Lists.newArrayList();
			cfg.getStringList("tasks").stream().forEach(s -> {
				IChapterTask task = IChapterTask.fromString(s);
				if(task == null) {
					Main.getInstance().getLogger().warning(id + "的一个任务[" + tasks.size() + "]加载失败: " + s);
				}
				tasks.add(task);
			});
			return new Chapter(id, name, tasks);
		}catch(Throwable t) {
			t.printStackTrace();
		}
		return null;
	}
	
	/**
	 * TODO 保存到配置文件
	 * @return
	 */
	public YamlConfiguration toYamlConfig() {
		return null;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<IChapterTask> getTasks() {
		return tasks;
	}
	
	@Nullable
	public IChapterTask getTask(int i) {
		return i >= 0 && i < tasks.size() ? tasks.get(i) : null;
	}
}
