package top.mrxiaom.doomsdaycommands;

import com.google.common.collect.Lists;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.util.List;

/**
 * 命令模板
 * 
 * @author MrXiaoM
 *
 */
public abstract class AbstractCommand {
	public final Plugin plugin;
	public final String label;
	public final String description;
	public final String[] aliases;

	public AbstractCommand(Plugin plugin, String label, String[] aliases, String description) {
		this.plugin = plugin;
		this.label = label;
		this.aliases = aliases;
		this.description = description;
	}
	
	/**
	 * 将命令转换成可在Bukkit注册的命令
	 */
	public PluginCommand toPluginCommand() {
		try {
			Constructor<PluginCommand> constCommand = PluginCommand.class
				.getDeclaredConstructor(String.class, Plugin.class);
			constCommand.setAccessible(true);
			
			PluginCommand newCmd = constCommand.newInstance(this.label, plugin);
			newCmd.setDescription(description);
			newCmd.setUsage("/" + this.label);
			newCmd.setAliases(Lists.newArrayList(this.aliases));
			return newCmd;
		}catch(Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	public boolean isAliase(String aliase) {
		for (String s : this.aliases) {
			if (s.equalsIgnoreCase(aliase)) {
				return true;
			}
		}
		return false;
	}

	public boolean isLabel(String label) {
		return this.label.equalsIgnoreCase(label);
	}

	public boolean isCommand(String command) {
		return this.isLabel(command) || this.isAliase(command);
	}

	public abstract boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer);

	public List<String> onTabComplete(CommandSender sender, String[] args, boolean isPlayer) {
		return null;
	}
}
