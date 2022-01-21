package top.mrxiaom.doomsdaycommands;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.doomsdayessentials.api.CommandProcessEvent;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 命令管理器
 * 
 * @author MrXiaoM
 *
 */
public class CmdManager implements CommandExecutor, TabCompleter, Listener {
	private final List<AbstractCommand> commands = new ArrayList<>();
	private final Plugin plugin;
	private final String packageName;
	/**
	 * 命令管理器，可动态注册命令
	 * (目前只管注册不管卸载)
	 * 
	 * @author MrXiaoM
	 * @param plugin 插件主类实例
	 * @param packageName 所有命令所在包
	 */
	public CmdManager(Plugin plugin, String packageName) {
		this.plugin = plugin;
		this.packageName = packageName;
	}
	
	/**
	 * 寻找包内所有命令并注册
	 * 
	 * @author MrXiaoM
	 */
	public CmdManager register() {
		List<Command> list = new ArrayList<>();
		try {
			String jar = URLDecoder.decode(this.plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath(), StandardCharsets.UTF_8);
			ZipFile zip = new ZipFile(jar);
			// 遍历插件压缩包內所有内容
			Enumeration<? extends ZipEntry> files = zip.entries();
			while (files.hasMoreElements()) {
				try {
					String url = files.nextElement().getName().replace("\\", "/");
					// 只认 .class 文件
					if (url.toLowerCase().endsWith(".class")) {
						// 去除 .class 后缀并替换分隔符为.
						url = url.substring(0, url.toLowerCase().lastIndexOf(".class"))
								.replace('/', '.').replace('\\', '.');
						// 条件: 在commands包内，不是子类
						if (url.startsWith(packageName + ".") && !url.contains("$")) {
							Class<?> c = Class.forName(url);
							Constructor<?> constAbstractCmd = c.getDeclaredConstructor(plugin.getClass());
							AbstractCommand cmd = (AbstractCommand) constAbstractCmd.newInstance(plugin);
							PluginCommand plugCmd = cmd.toPluginCommand();
							plugCmd.setExecutor(this);
							plugCmd.setTabCompleter(this);
							list.add(plugCmd);
							this.commands.add(cmd);
						}
					}
				} catch (Throwable t1) {
					t1.printStackTrace();
				}
			}
			zip.close();
			this.pushCommands(list);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return this;
	}
	
	/**
	 * 将已注册的命令手动推送到服务器命令列表
	 * 
	 * @author MrXiaoM
	 * @param list 命令列表
	 */
	public void pushCommands(List<Command> list) {
		try {
			Class<?> classCraftServer = Class.forName("org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().substring(23) + ".CraftServer");
			SimpleCommandMap commandMap = (SimpleCommandMap) classCraftServer.getDeclaredMethod("getCommandMap")
					.invoke(Bukkit.getServer());
			commandMap.registerAll(plugin.getDescription().getName(), list);
			Method syncCommands = classCraftServer.getDeclaredMethod("syncCommands");
			syncCommands.setAccessible(true);
			syncCommands.invoke(Bukkit.getServer());
		}catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * 获取命令实例，在命令不在列表时返回null
	 * 
	 * @author MrXiaoM
	 * @param <T> 命令类型
	 * @param clazz 命令类
	 * @return 命令实例
	 */
	@Nullable
	public <T> T getCommandInstance(Class<T> clazz) {
		if(clazz == null) return null;
		for(AbstractCommand cmd : this.commands) {
			try {
				return clazz.cast(cmd);
			} catch(Throwable t) {
				// 收声
			}
		}
		return null;
	}
	
	/**
	 * 获取所有已载入并注册的命令
	 * 
	 * @author MrXiaoM
	 * @return 命令列表
	 */
	public List<AbstractCommand> getLoadedCommands(){
		return this.commands;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, String label, String[] args) {
		String cmdHead = label.contains(":") ? label.substring(label.indexOf(":") + 1) : label;
		for (AbstractCommand c : this.commands) {
			if (c.isCommand(cmdHead)) {
				CommandProcessEvent event = new CommandProcessEvent(sender, cmdHead, args);
				Bukkit.getServer().getPluginManager().callEvent(event);
				if (!event.isCancelled()) {
				    return c.onCommand(sender, cmdHead, args, sender instanceof Player);
				}
			}
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, String label, String[] args) {
		String cmdHead = label.contains(":") ? label.substring(label.indexOf(":") + 1) : label;
		for (AbstractCommand c : commands) {
			if (c.isCommand(cmdHead)) {
				return c.onTabComplete(sender, args, sender instanceof Player);
			}
		}
		return new ArrayList<>();
	}
}
