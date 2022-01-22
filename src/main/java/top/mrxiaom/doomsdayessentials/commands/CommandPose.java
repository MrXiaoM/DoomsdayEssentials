package top.mrxiaom.doomsdayessentials.commands;

import com.google.common.collect.Lists;
import net.minecraft.server.v1_15_R1.DataWatcherObject;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityPose;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.scheduler.BukkitTask;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.modules.reviveme.ReviveMeApi;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.NMSUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandPose extends ICommand {
	Map<UUID, BukkitTask> map = new HashMap<>();
	public CommandPose(Main plugin) {
		super(plugin, "pose", new String[] {});
	}
	public boolean a(Player player){
		return map.containsKey(player.getUniqueId());
	}
	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if (!isPlayer) {
			return Util.noPlayer(sender);
		}
		Player player = (Player) sender;
		if (ReviveMeApi.isPlayerDowned(player)){
			player.sendMessage(I18n.t("reviveme.no-command",true));
			return true;
		}
		boolean flag = map.containsKey(player.getUniqueId());
		if(flag) {
			map.get(player.getUniqueId()).cancel();
			player.setSwimming(false);
		} else map.put(player.getUniqueId(), Bukkit.getScheduler().runTaskTimer(plugin,()->{
			player.setSwimming(true);
		},1,1));
		flag = map.containsKey(player.getUniqueId());
		player.sendMessage("趴下已" + (flag ? "开启" : "关闭"));
		return true;
	}
}
