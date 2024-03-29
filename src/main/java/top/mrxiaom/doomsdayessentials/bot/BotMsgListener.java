package top.mrxiaom.doomsdayessentials.bot;

import com.ranull.graves.inventory.GraveInventory;
import fr.xephi.authme.api.v3.AuthMeApi;
import fr.xephi.authme.events.LoginEvent;
import me.albert.amazingbot.bot.Bot;
import me.albert.amazingbot.events.GroupMessageEvent;
import me.leoko.advancedban.manager.PunishmentManager;
import me.leoko.advancedban.utils.Punishment;
import me.leoko.advancedban.utils.SQLQuery;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.QuoteReply;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.TimeUtil;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

public class BotMsgListener implements Listener {
    Main plugin;
    public BotMsgListener(Main main){
        this.plugin = main;
        Bukkit.getPluginManager().registerEvents(this, main);
    }
    public int cooldown = 0;
    public final Map<String, Long> requestMap = new HashMap<>();

    @EventHandler
    public void onAuthmeLogin(LoginEvent event) {
        if(event.getPlayer().isOp()) return;
        String tag = Util.removeColor(plugin.getTagConfig().getPlayerTag(event.getPlayer().getName()));
        Bot.getApi().getGroup(951534513L).sendMessage(tag + " " + event.getPlayer().getName() + " 上线啦!");
    }
    @SuppressWarnings("deprecation")
    @EventHandler
    public void onGroupMessage(GroupMessageEvent e) {
        net.mamoe.mirai.event.events.GroupMessageEvent event = e.getEvent();
        Group g = event.getGroup();
        if (g.getId() == 951534513L) {
            plugin.getLogger().info(g.getName() + "(" + g.getId() + ") " + event.getSenderName() + "("
                    + event.getSender().getId() + "): " + event.getMessage().contentToString().replace("\n", " \\n "));
        }
        FileConfiguration config = plugin.getConfig();
        List<String> groups = config.getStringList("groups");
        if (!groups.contains(e.getGroupID().toString())) {
            return;
        }
        final QuoteReply quote = new QuoteReply(event.getSource());
        if (e.getMsg().trim().equalsIgnoreCase("确认绑定")) {
            boolean ok = false;
            boolean removeFlag = false;
            StringBuilder message = new StringBuilder("【正在处理绑定请求】");
            for (String player : requestMap.keySet()) {
                long target = requestMap.get(player);
                if (e.getUserID().equals(target)) {
                    ok = true;
                    if (!removeFlag) {
                        plugin.getWhitelistConfig().set(player, String.valueOf(target)).saveConfig();
                        message.append("\n你已成功绑定账号 ").append(player);
                        removeFlag = true;
                    } else {
                        message.append("\n当前QQ号已被绑定，你无法绑定账号 ").append(player);
                    }
                    requestMap.remove(player);
                }
            }
            if (ok) {
                g.sendMessage(quote.plus(message.toString()));
            }
            return;
        }
        if (e.getMsg().toLowerCase().startsWith("#切换坟墓状态")) {
            String type = e.getMsg().substring(7);
            boolean flag;
            if (type.equalsIgnoreCase("已保护")) {
                flag = true;
            } else if (type.equalsIgnoreCase("未保护")) {
                flag = false;
            } else {
                g.sendMessage(quote.plus("用法:\n" + "#切换坟墓状态已保护 - 将自己所有坟墓设置为已保护\n" + "#切换坟墓状态未保护 - 将自己所有坟墓设置为未保护\n"
                        + "这个功能可以在你停搏无法进入服务器后让别人能够帮你挖坟不至于丢失过多物品"));
                return;
            }
            for (String playerName : plugin.getWhitelistConfig().getSavedPlayers()) {
                if (plugin.getWhitelistConfig().getPlayerBindQQ(playerName).equals(e.getUserID().toString())) {
                    Player player = Bukkit.getPlayer(playerName);
                    if (player == null) {
                        g.sendMessage(quote.plus("错误: 绑定的玩家不存在"));
                        return;
                    }
                    ConcurrentMap<Location, GraveInventory> graves = plugin.getGraveApi()
                            .getPlayerGraves(player.getUniqueId());
                    if (graves.size() == 0) {
                        g.sendMessage(quote.plus("错误: 你目前没有坟"));
                        return;
                    }
                    for (GraveInventory grave : graves.values()) {
                        grave.setProtected(flag);
                    }
                    g.sendMessage(quote.plus("已设置你的坟墓保护状态为" + (flag ? "开启" : "关闭")));
                    return;
                }
            }
            g.sendMessage(quote.plus("错误: 你还没有绑定玩家"));
            return;
        }
        if (e.getMsg().toLowerCase().startsWith("/say ")) {
            String msg = Util.removeColor(e.getMsg().substring(5).replace("\n", ",").replace("\r", ""));
            if (msg.trim().length() <= 0) {
                g.sendMessage(quote.plus(I18n.t("bot.content")));
                return;
            }
            if (msg.length() > 128) {
                g.sendMessage(quote.plus(I18n.t("bot.too-long")));
            }
            if (plugin.getConfig().contains("blacklist-words")) {
                List<String> bw = plugin.getConfig().getStringList("blacklist-words");
                for (String s : bw) {
                    if (msg.toLowerCase().contains(s)) {
                        g.sendMessage(quote.plus(I18n.t("bot.banwords")));
                        return;
                    }
                }
            }
            String str = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("chat.group.to-game"))
                    .replace("%name%", Util.limitLength(Util.removeColor(e.getEvent().getSender().getNameCard()), 24))
                    .replace("%qq%", String.valueOf(e.getEvent().getSender().getId())).replace("%msg%", msg);
            plugin.getLogger().info(Util.removeColor(str));
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(str);
            }
            g.sendMessage(quote.plus(I18n.t("bot.forward")));
            return;
        }
        if (e.getMsg().equalsIgnoreCase("/list")) {
            StringBuilder players = new StringBuilder();
            Player[] onlinePlayers = Bukkit.getOnlinePlayers().toArray(Player[]::new);
            List<String> playersName = new ArrayList<>();
            for (Player p : onlinePlayers) {
                playersName.add(p.getName());
            }
            Collections.sort(playersName);
            for (int i = 0; i < playersName.size(); i++) {
                players.append(playersName.get(i)).append(i < playersName.size() - 1 ? ", " : "");
            }
            event.getGroup().sendMessage(quote.plus(I18n.t("bot.online").replace("\\n", "\n")
                    .replace("%count%", String.valueOf(playersName.size())).replace("%list%", players.toString())));
            return;
        }
        String seenKey = plugin.getConfig().getString("bot.seen-key");
        if (seenKey != null && e.getMsg().startsWith(seenKey)) {
            if (!e.getMsg().contains(" ")) {
                event.getGroup().sendMessage(quote.plus(I18n.t("bot.usage.seen")));
                return;
            }
            String[] args = e.getMsg().split(" ");
            if (!args[0].equals(seenKey))
                return;
            if (args.length != 2) {
                event.getGroup().sendMessage(quote.plus(I18n.t("bot.usage.seen")));
                return;
            }
            String playerName = args[1];
            final String regex = "[" + config.getString("name.regex") + "]*";
            if (!Pattern.matches(regex, playerName)) {
                event.getGroup().sendMessage(quote.plus(I18n.t("bot.invalid-name")));
                return;
            }

            if (!plugin.getPlayerConfig().getConfig().contains(playerName) || Util.getOfflinePlayer(playerName) == null) {
                event.getGroup().sendMessage(quote.plus(I18n.t("bot.unknown-player")));
                return;
            }

            Optional<OfflinePlayer> targetPlayer = Util.getOfflinePlayer(playerName);
            if (targetPlayer.isPresent() && targetPlayer.get().isOnline()) {
                if (!plugin.getUpdaterApi().getPlayerIPMap().containsKey(playerName)) {
                    event.getGroup().sendMessage(quote.plus(I18n.t("bot.online-nodata")));
                    return;
                }
                top.mrxiaom.pluginupdater.Main.PlayerOnlineStatus status = plugin.getUpdaterApi().getPlayerIPMap().get(playerName);
                if (status == null) {
                    event.getGroup().sendMessage(quote.plus(I18n.t("bot.online-nodata")));
                    return;
                }
                long now = Calendar.getInstance().getTimeInMillis();
                long last = plugin.getUpdaterApi().getPlayerIPMap().get(playerName).getLoginTime();
                // TODO 更换过期接口
                String time = TimeUtil.getChineseTime_Old(now - last, "", "");
                event.getGroup().sendMessage(
                        quote.plus(I18n.t("bot.online-time").replace("%player%", playerName).replace("%time%", time)));
                return;
            } else if(targetPlayer.isPresent()){
                long now = Calendar.getInstance().getTimeInMillis();
                long last = targetPlayer.get().getLastPlayed();
                String time = TimeUtil.getChineseTime_Old(now - last, "", "");
                String result = I18n.t("bot.offline-time").replace("%player%", playerName).replace("%time%", time);
                try {
                    List<Punishment> pList = PunishmentManager.get()
                            .getPunishments(SQLQuery.SELECT_ALL_PUNISHMENTS_LIMIT, 150);
                    for (Punishment p : pList) {
                        if (p.getName().equalsIgnoreCase(playerName)) {
                            result += "\n" + I18n.t("bot.offline-banned.title") + "\n"
                                    + I18n.t("bot.offline-banned.type").replace("%type%", p.getType().getName()) + "\n"
                                    + I18n.t("bot.offline-banned.reason").replace("%reason%", p.getReason()) + "\n"
                                    + I18n.t("bot.offline-banned.operator").replace("%operator%", p.getOperator());
                            break;
                        }
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
                if (plugin.getPlayerConfig().getNeedle(playerName) < 0) {
                    result += "\n" + I18n.t("bot.offline-no-needle");
                }
                event.getGroup().sendMessage(quote.plus(result));
                return;
            }
        }
        String refreshKey = plugin.getConfig().getString("bot.refresh-key");
        if (refreshKey != null && e.getMsg().startsWith(refreshKey)) {
            if (!e.getMsg().contains(" ")) {
                event.getGroup().sendMessage(quote.plus(I18n.t("bot.usage.refresh")));
                return;
            }
            Optional<OfflinePlayer> player = Util.getOfflinePlayer(e.getMsg().split(" ")[1]);
            if(player.isEmpty() || player.get().getName() == null){
                event.getGroup().sendMessage(quote.plus(I18n.t("bot.invalid-name")));
                return;
            }
            if(cooldown > 0) {
                event.getGroup().sendMessage(quote.plus("命令冷却中，请等待" + cooldown + "秒"));
                return;
            }
            cooldown = 15;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> event.getGroup().sendMessage(quote.plus(Util.removeColor(plugin.checkPoints(player.get())))));
            return;
        }
        final String key = config.getString("keyword") + " ";
        if (!e.getMsg().startsWith(key)) {
            return;
        }
        String name = e.getMsg().substring(key.length());
        if (name.contains("\n"))
            name = name.substring(0, name.indexOf("\n"));
        final int min = config.getInt("name.min_length");
        final int max = config.getInt("name.max_length");
        if (name.length() < min || name.length() > max) {
            event.getGroup().sendMessage(quote.plus(I18n.t("bot.length")));
            return;
        }
        final String regex = "[" + config.getString("name.regex") + "]*";
        if (!Pattern.matches(regex, name)) {
            event.getGroup().sendMessage(quote.plus(I18n.t("bot.char")));
            return;
        }
        for (final String s : plugin.getWhitelistConfig().getSavedPlayers()) {
            if (plugin.getWhitelistConfig().getPlayerBindQQ(s).equalsIgnoreCase(e.getUserID().toString())) {
                event.getGroup().sendMessage(quote.plus(I18n.t("bot.binded").replace("%name%", s)));
                return;
            }
        }
        if (plugin.getWhitelistConfig().getPlayerBindQQ(name).length() > 0 || AuthMeApi.getInstance().isRegistered(name)) {
            event.getGroup().sendMessage(quote.plus(I18n.t("bot.fail")));
            return;
        }
        plugin.getWhitelistConfig().set(name, e.getUserID().toString()).saveConfig();
        event.getGroup().sendMessage(quote.plus(I18n.t("bot.bind").replace("%name%", name)));
    }

}
