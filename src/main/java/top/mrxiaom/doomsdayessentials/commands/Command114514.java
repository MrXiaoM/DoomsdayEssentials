package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Command114514 extends ICommand {
	Map<UUID, Integer> map = new HashMap<>();
	public Command114514(Main plugin) {
		super(plugin, "114514", new String[] {});
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (!isPlayer) {
			return Util.noPlayer(sender);
		}
		Player player = (Player) sender;
		if(!map.containsKey(player.getUniqueId())){
			map.put(player.getUniqueId(), 1);
		}else{
			int count = map.get(player.getUniqueId());
			if(count + 1 > 5){
				map.remove(player.getUniqueId());
				player.kickPlayer("臭 小 子");
				Util.alert(player.getName() + " &f因为太臭被踢出了服务器");
				return true;
			} else{
				map.put(player.getUniqueId(), count + 1);
			}
		}
		plugin.getChatListener().handleChat("哼 哼 哼 啊啊啊啊啊啊啊啊啊啊，啊啊啊啊啊啊啊啊啊啊啊啊啊，" + "啊，啊啊啊啊啊啊啊啊啊啊",
				player, -1);
		return true;
	}

}
