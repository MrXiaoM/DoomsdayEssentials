package top.mrxiaom.doomsdayessentials.chapter;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.chapter.tasks.*;
import top.mrxiaom.doomsdayessentials.chapter.tasks.ClickNPCTask.ClickType;
import top.mrxiaom.doomsdayessentials.utils.Util;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;

public interface IChapterTask<T extends Event> {
	String display();

	void execute(Player player, T event);

	void start(Player player);

	void end(Player player);

	boolean hasEvent();
	default Class<? extends Event>[] moreEvents(){
		return new Class[] { };
	}
	@SuppressWarnings("unchecked")
	default Class<T> eventClass() {
		try {
			Class<T> cls = (Class<T>) ((ParameterizedType) this.getClass().getAnnotatedInterfaces()[0].getType()).getActualTypeArguments()[0];
			if (!cls.getName().equals(Event.class.getName())) return cls;
		} catch(Throwable t) {
			// 收声
		}
		return null;
	}

	default void jump(Player player, int index) {
		Main.getInstance().getChapterManager().jumpTo(player, index);
	}

	default void next(Player player) {
		Main.getInstance().getChapterManager().nextTask(player);
	}

	/*
	* 从文本将剧情操作反序列化
	* */
	@Nullable
	static IChapterTask<?> fromString(String text) {
		if (text.startsWith("delay:")) {
			try {
				int ticks = Integer.parseInt(text.substring(6));
				return new DelayTask(ticks);
			} catch(Throwable t){
				return handleError(text, t);
			}
		}
		if (text.startsWith("msg:")) {
			return new PrivateMsgTask(text.substring(4));
		}
		if (text.startsWith("clicknpc:")) {
			if (text.contains(",")) {
				String[] args = text.substring(9).split(",");
				if (args.length > 1) {
					try {
						int id = Integer.parseInt(args[0]);
						ClickType type = Util.valueOf(ClickType.class, args[1], null);
						if (type == null) throw new IllegalArgumentException("输入的 " + args[1] + " 不是有效的 ClickType");
						boolean shift = false;
						StringBuilder shiftTips = new StringBuilder();
						if (args.length > 2) {
							if (args[2].equalsIgnoreCase("shift")) {
								shift = true;
								if (args.length > 3) {
									for (int i = 3; i < args.length; i++) {
										shiftTips.append(args[i]).append(i < args.length - 1 ? "," : "");
									}
								}
							}
						}
						return new ClickNPCTask(id, type, shift, shiftTips.toString());
					}
					catch(Throwable t){
						return handleError(text, t);
					}
				}
			}
		}
		if (text.startsWith("title:")) {
			if (text.contains(",")) {
				String[] args = text.substring(9).split(",");
				if (args.length > 1) {
					try {
						int fadeIn = 5;
						int time = 30;
						int fadeOut = 5;
						if (args.length == 5) {
							fadeIn = Integer.parseInt(args[2]);
							time = Integer.parseInt(args[3]);
							fadeOut = Integer.parseInt(args[4]);
						}
						return new TitleMsgTask(args[0], args[1], fadeIn, time, fadeOut);
					} catch(Throwable t){
						return handleError(text, t);
					}
				}
			}
		}
		if (text.startsWith("console:")) {
			return new ConsoleCommandTask(text.substring(8));
		}
		if (text.startsWith("jumpto:")) {
			try {
				int index = Integer.parseInt(text.substring(7));
				return new JumpToTask(index);
			}catch(Throwable t){
				return handleError(text, t);
			}
		}
		if (text.startsWith("clickblock:")) {
			if (text.contains(",")) {
				String[] args = text.substring(11).split(",");
				if (args.length > 3) {
					try {
						World world = Bukkit.getWorld(args[0]);
						if (world == null) throw new IllegalArgumentException("世界 " + args[0] + " 不存在");
						int x = Integer.parseInt(args[1]);
						int y = Integer.parseInt(args[2]);
						int z = Integer.parseInt(args[3]);
						Action action = null;
						BlockFace face = null;
						if (args.length > 4) {
							action = Util.valueOf(Action.class, args[4]);
							if (action == null) {
								System.out.println("[警告] " + args[4] + "不是有效的 Action 值");
							}
						}
						if (args.length > 5) {
							face = Util.valueOf(BlockFace.class, args[5]);
							if (face == null) {
								System.out.println("[警告] " + args[5] + "不是有效的 BlockFace 值");
							}
						}
						return new ClickBlockTask(world.getName(), x, y, z, action, face);
					} catch (Throwable t) {
						return handleError(text, t);
					}
				}
			}
		}
		if (text.startsWith("walk:")) {
			if (text.contains(",")) {
				String[] args = text.substring(5).split(",");
				if (args.length > 3) {
					try {
						World world = Bukkit.getWorld(args[0]);
						if (world == null) throw new IllegalArgumentException("世界 " + args[0] + " 不存在");
						int x = Integer.parseInt(args[1]);
						int y = Integer.parseInt(args[2]);
						int z = Integer.parseInt(args[3]);
						return new WalkTask(world.getName(), x, y, z);
					} catch (Throwable t) {
						handleError(text, t);
					}
				}
			}
		}
		if(text.startsWith("talk:")){
			if (text.contains(":")){
				String[] args = text.substring(5).split(":");
				if(args.length >= 2) {
					try {
						String sender = args[0];
						StringBuilder message = new StringBuilder(args[1]);
						if (args.length >= 5) {
							Sound sound = Util.valueOf(Sound.class, args[args.length - 3]);
							float pitch = Float.parseFloat(args[args.length - 2]);
							float volume = Float.parseFloat(args[args.length - 1]);
							for (int i = 2; i < args.length - 3; i++) {
								message.append(":").append(args[i]);
							}
							return new MsgTask(sender, message.toString(), sound, pitch, volume);
						} else {
							for (int i = 2; i < args.length; i++) {
								message.append(":").append(args[i]);
							}
							return new MsgTask(sender, message.toString());
						}
					} catch (Throwable t){
						return handleError(text, t);
					}
				}
			}
		}
		System.out.println("[错误] 任务" + text + " 的参数不全");
		return null;
	}

	/*
	* 输出异常日志并恒返回 null
	* */
	static IChapterTask<?> handleError(String text, Throwable t){
		Main.getInstance().getLogger().warning("[错误] 在读取任务 " + text + " 时发生一个错误");
		t.printStackTrace();
		return null;
	}
}
