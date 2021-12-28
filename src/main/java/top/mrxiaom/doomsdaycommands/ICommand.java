package top.mrxiaom.doomsdaycommands;

import top.mrxiaom.doomsdayessentials.Main;

import java.util.logging.Logger;

public abstract class ICommand extends AbstractCommand{

	private final Logger logger;
	public final Main plugin;
	public ICommand(Main plugin, String label, String[] aliases) {
		this(plugin, label, aliases, "An experimental command");
	}
	public ICommand(Main plugin, String label, String[] aliases, String description) {
		super(plugin, label, aliases, description);
		this.plugin = plugin;
		this.logger = plugin.getLogger();
	}

	
	public Logger getLogger() {
		return this.logger;
	}
}
