package top.mrxiaom.doomsdayessentials.commands;

import com.google.common.collect.Lists;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandNear extends ICommand {
	public CommandNear(Main plugin) {
		super(plugin, "near", new String[] {"nearby"});
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if(!isPlayer) return Util.noPlayer(sender);
		Player player = (Player) sender;
		Location loc = player.getLocation();
		Map<String, Double> players = new HashMap<>();
		for(Player p : player.getWorld().getPlayers()) {
			double distance = loc.distance(p.getLocation());
			if(distance <= 100.0D) {
				if(plugin.getPlayerConfig().getConfig().getBoolean(p.getName() + ".invisible" ,false)
					|| p.getGameMode().equals(GameMode.SPECTATOR) || player.getName().equals(p.getName())) continue;
				players.put(p.getName(), distance);
			}
		}
		if(players.isEmpty()) {
			player.sendMessage(I18n.t("nearby-nobody", true));
			return true;
		}
		player.sendMessage(I18n.t("nearby", true).replace("%data%", getString(players, I18n.t("nearby-prefix"), I18n.t("nearby-suffix"))));
		return true;
	}
	
	private String getString(Map<String, Double> players, String p, String s) {
		 List<Map.Entry<String, Double>> list = Lists.newArrayList(players.entrySet());
		 list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
		 String result = p;
		 DecimalFormat df = new DecimalFormat("#.0");
		 for(int i = 0; i < list.size(); i++) {
			 result = result + list.get(i).getKey() + s + "(" + df.format(list.get(i).getValue()) + "m)" + p + (i < list.size()-1? ", ":"");
		 }
		 return result;
	}
}
