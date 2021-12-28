package top.mrxiaom.doomsdayessentials.chapter;

import org.bukkit.entity.Player;

public class ProcessChapter {
	final Chapter config;
	int taskIndex = 0;
	final Player player;
	public ProcessChapter(Player player, Chapter config) {
		this.config = config;
		this.taskIndex = 0;
		this.player = player;
	}
	public Chapter getConfig() {
		return config;
	}
	public int getTaskIndex() {
		return taskIndex;
	}
	public Player getPlayer() {
		return player;
	}
	public void setTaskIndex(int taskIndex) {
		this.taskIndex = taskIndex;
	}
	/**
	 * 转跳到下一个任务，返回true则代表没有任务了
	 * @return 是否已结束
	 */
	public boolean nextTask() {
		return jumpTo(taskIndex + 1);
	}
	
	public boolean jumpTo(int i){
		IChapterTask task = config.getTask(taskIndex);
		if(task == null) {
			return true;
		}
		task.end(player);
		if((task = config.getTask(i)) == null){
			return true;
		}
		taskIndex = i;
		task.start(player);
		return false;
	}
	
	public void end() {
		// TODO 章节结束时结算奖励和进度
	}
}
