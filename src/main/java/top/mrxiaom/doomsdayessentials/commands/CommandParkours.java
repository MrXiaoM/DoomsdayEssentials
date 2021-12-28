package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.gui.GuiParkours;
import top.mrxiaom.doomsdayessentials.utils.Util;

public class CommandParkours extends ICommand {
	public CommandParkours(Main plugin) {
		super(plugin, "parkours", new String[] {});
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (!isPlayer) {
			return Util.noPlayer(sender);
		}
		Player player = (Player) sender;
		plugin.getGuiManager().openGui(new GuiParkours(plugin, player, args.length > 0 && args[0].equalsIgnoreCase("backToMenuButton")));
		return true;
	}
}
