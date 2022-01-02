package top.mrxiaom.doomsdayessentials.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.mrxiaom.doomsdaycommands.ICommand;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.chapter.Chapter;
import top.mrxiaom.doomsdayessentials.utils.I18n;
import top.mrxiaom.doomsdayessentials.utils.Util;

public class CommandChapter extends ICommand {
    public CommandChapter(Main main){
        super(main, "chapter", new String[]{ "剧情" });
    }
    @Override
    public boolean onCommand(CommandSender sender, String label, String[] args, boolean isPlayer) {
        if(!sender.isOp()) return true;
        if(args.length == 3 && args[0].equalsIgnoreCase("start")){
            Player player = Util.getOnlinePlayer(args[1]);
            if(player == null){
                sender.sendMessage(I18n.t("not-online",true));
                return true;
            }
            Chapter chapter = plugin.getChapterManager().getChapterById(args[2]);
            if(chapter == null){
                sender.sendMessage(I18n.t("chapter.not-found", true));
                return true;
            }
            if(plugin.getChapterManager().startChapter(player, chapter)){
                sender.sendMessage(I18n.t("chapter.processing", true));
            }
            else{
                sender.sendMessage(I18n.t("chapter.start", true)
                        .replace("%player%", player.getName())
                        .replace("%name%", chapter.getName()));
            }
        }

        return true;
    }
}