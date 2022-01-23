package top.mrxiaom.doomsdayessentials.chapter;

import net.citizensnpcs.api.event.NPCClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import top.mrxiaom.doomsdayessentials.Main;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

@SuppressWarnings({"rawtypes", "unused"})
public class ChapterManager implements Listener {
	
	final File mainDir;
	final File configDir;
	final File playerDir;
	final Main plugin;
	final List<Chapter> chapters = new ArrayList<>();
	final Map<String, ProcessChapter> playerProcessChapters = new HashMap<>();
	public ChapterManager(Main plugin) {
		this.plugin = plugin;
		this.mainDir = new File(plugin.getDataFolder(), "chapter");
		this.configDir = new File(this.mainDir, "config");
		this.playerDir = new File(this.mainDir, "players");
		Bukkit.getPluginManager().registerEvents(this, plugin);
		plugin.getLogger().info("剧情管理器已加载");
	}

	public Map<String, ProcessChapter> getPlayerProcessChapters(){
		return playerProcessChapters;
	}

	public boolean isPlayerProcessingChapter(String player){
		return playerProcessChapters.containsKey(player);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		executeEvent(event.getPlayer(), event);
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		executeEvent(event.getPlayer(), event);
	}

	@EventHandler
	public void onBlockBreak(EntityDamageByEntityEvent event) {
		if(event.getDamager() instanceof Player) {
			executeEvent((Player) event.getDamager(), event);
		}
		if(event.getEntity() instanceof Player) {
			executeEvent((Player) event.getEntity(), event);
		}
	}
	
	@EventHandler
	public void onNPCClickEvent(NPCClickEvent event) {
		executeEvent(event.getClicker(), event);
	}
	@EventHandler
	public void onPlayerIntractEvent(PlayerInteractEvent event) {
		executeEvent(event.getPlayer(), event);
	}
	@EventHandler
	public void onPlayerSwapHandItemsEvent(PlayerSwapHandItemsEvent event) {
		executeEvent(event.getPlayer(), event);
	}
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		executeEvent(event.getPlayer(), event);
	}

	@SuppressWarnings({"unchecked"})
	public void executeEvent(Player player, Event event) {
		String playerName = player.getName();
		if(playerProcessChapters.containsKey(playerName)) {
			ProcessChapter chapter = playerProcessChapters.get(playerName);
			IChapterTask task = chapter.getConfig().getTask(chapter.getTaskIndex());
			if(task == null || !task.hasEvent()) return;
			if(task.eventClass() != null && task.eventClass().isAssignableFrom(event.getClass())) {
				task.execute(player, event);
			}
			else if(task.moreEvents().length > 0){
				for (Class c : task.moreEvents()) {
					if (c != null && c.isAssignableFrom(event.getClass())) {
						task.execute(player, event);
						break;
					}
				}
			}
		}
	}
	
	public void nextTask(Player player) {
		String playerName = player.getName();
		if(playerProcessChapters.containsKey(playerName)) {
			ProcessChapter chapter = playerProcessChapters.get(playerName);
			if(chapter.nextTask()) {
				chapter.end();
				playerProcessChapters.remove(playerName);
			}
		}
	}
	
	public void jumpTo(Player player, int i) {
		String playerName = player.getName();
		if(playerProcessChapters.containsKey(playerName)) {
			ProcessChapter chapter = playerProcessChapters.get(playerName);
			if(chapter.jumpTo(i)) {
				chapter.end();
				playerProcessChapters.remove(playerName);
			}
		}
	}

	public void end(Player player){
		String playerName = player.getName();
		if(playerProcessChapters.containsKey(playerName)) {
			playerProcessChapters.get(playerName).end();
			playerProcessChapters.remove(playerName);
		}
	}
	
	/**
	 * 开启章节
	 * 返回true代表玩家已经在进行一个章节了
	 * @param player 玩家名
	 * @param chapterCfg 剧情配置
	 * @return true 为玩家已在进行剧情了，不能进行新剧情
	 */
	public boolean startChapter(Player player, Chapter chapterCfg) {
		String playerName = player.getName();
		if(playerProcessChapters.containsKey(playerName)) {
			return true;
		}
		ProcessChapter chapter = new ProcessChapter(player, chapterCfg);
		playerProcessChapters.put(playerName, chapter);
		IChapterTask task = chapter.getConfig().getTask(chapter.getTaskIndex());
		if(task != null) {
			task.start(player);
		}
		else {
			nextTask(player);
		}
		return false;
	}
	
	public ChapterManager reloadConfig() {
		try {
			this.chapters.clear();
			File[] configFiles = this.configDir.listFiles();
			File[] playerFiles = this.playerDir.listFiles();
			if(configFiles != null) Arrays.stream(configFiles).forEach(file -> {
				YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
				Chapter chapter = Chapter.fromYamlConfig(cfg);
				if(chapter == null) {
					plugin.getLogger().warning("无法加载章节文件 /chapter/config/" + file.getName());
				}
				else {
					plugin.getLogger().info("已载入剧情 " + chapter.getId() + " " + chapter.getName());
					this.chapters.add(chapter);
				}
			});
			if(playerFiles != null) Arrays.stream(playerFiles).forEach(file -> {
				YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
				cfg.getName();
				// TODO 载入玩家配置
			});
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return this;
	}
	
	public ChapterManager saveConfig() {
		try {
			// 剧情章节配置文件只读不写，不需要保存 chapters
			// TODO 保存玩家配置
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return this;
	}

	public List<Chapter> getChapters() {
		return chapters;
	}

	@Nullable
	public Chapter getChapterById(String id){
		for(Chapter chapter : chapters){
			if(chapter.getId().equals(id)) return chapter;
		}
		return null;
	}
}
