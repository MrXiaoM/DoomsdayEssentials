package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.McbbsUtil;
import top.mrxiaom.doomsdayessentials.utils.McbbsUtil.UserInfo;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class CommandMcbbs extends ICommand {
	public CommandMcbbs(Main plugin) {
		super(plugin, "mcbbs", new String[] {});
	}
	
	private final List<String> processPlayers = new ArrayList<>();
	
	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
		if(!isPlayer) {
			return Util.noPlayer(sender);
		}
		Player player = (Player) sender;
		if(player.isOp() && args.length == 1 && args[0].equalsIgnoreCase("set")) {
			if(player.getInventory().getItemInMainHand().getType().equals(Material.AIR)) {
				player.sendMessage(I18n.t("mcbbs.need-item", true));
				return true;
			}
			plugin.getMcbbsConfig().setReward(player.getInventory().getItemInMainHand());
			player.sendMessage(I18n.t("mcbbs.set-reward", true));
			return true;
		}
		if(args.length == 1 && args[0].equalsIgnoreCase("info")) {
			if(plugin.getMcbbsConfig().isNowMatchedLastDate()) {
				player.sendMessage(I18n.tn("mcbbs.info-claimed", true).replace("%player%", plugin.getMcbbsConfig().getLastPlayer()));
			}
			else {
				player.sendMessage(I18n.tn("mcbbs.info-not-claimed", true));
			}
			return true;
		}
		if(args.length == 2 && args[0].equalsIgnoreCase("bind")) {
			if(this.processPlayers.contains(player.getName())) {
				player.sendMessage(I18n.t("mcbbs.binding", true));
				return true;
			}
			if(plugin.getMcbbsConfig().isPlayerBound(player.getName())) {
				player.sendMessage(I18n.t("mcbbs.bound-server", true));
				return true;
			}
			String uid = args[1];
			if(plugin.getMcbbsConfig().getUidPlayer() != null) {
				player.sendMessage(I18n.t("mcbbs.bound-mcbbs", true));
				return true;
			}
			this.processPlayers.add(player.getName());
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				public void run() {
					try {
						UserInfo info = McbbsUtil.getUserInfoUid(uid);
						if(info.isPromotionUserGroup()) {
							plugin.getMcbbsConfig().setPlayerUid(player.getName(), info.getUid());
							player.sendMessage(I18n.t("mcbbs.bound", true).replace("%username%", info.getUsername()).replace("%uid%", info.getUid()));
						}
						else {
							player.sendMessage(I18n.t("mcbbs.need-verify", true));
						}
					}catch(Throwable t) {
						t.printStackTrace();
					} finally {
						processPlayers.remove(player.getName());
					}
				}
			});
			return true;
		}
		player.sendMessage(I18n.tn("mcbbs.help", true));
		return true;
	}
}
