package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.gui.GuiWarps;
import top.mrxiaom.doomsdayessentials.utils.Util;

public class CommandWarps extends ICommand {
	public CommandWarps(Main plugin) {
		super(plugin, "warps", new String[] { "warpgui" });
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (!isPlayer) {
			return Util.noPlayer(sender);
		}
		Player player = (Player) sender;
		plugin.getGuiManager().openGui(new GuiWarps(plugin, player, 1, (args.length != 0 && args[0].equalsIgnoreCase("menu"))));
		return true;
	}
}
