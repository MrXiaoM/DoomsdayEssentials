package top.mrxiaom.doomsdayessentials.chapter.tasks;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.chapter.IChapterTask;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MsgTask implements IChapterTask<PlayerSwapHandItemsEvent> {
    public static Sound DEFAULT_SOUND = Sound.BLOCK_NOTE_BLOCK_HARP;
    String sender;
    String message;
    Sound sound;
    float pitch;
    float volume;
    List<String> playingPlayer = new ArrayList<>();
    List<String> skipingPlayer = new ArrayList<>();
    public MsgTask(String sender, String message){
        this(sender, message, DEFAULT_SOUND, 1.0F, 1.0F);
    }
    public MsgTask(String sender, String message, @Nullable Sound sound, float pitch, float volume) {
        this.sender = sender;
        this.message = message;
        this.sound = sound;
        this.pitch = pitch;
        this.volume = volume;
    }
    public String toString() {
        return "talk:" + sender + ":" + message + (sound != null ? ("," + sound.name() + "," + pitch + "," + volume) : "");
    }
    @Override
    public String display() {
        return null;
    }

    @Override
    public void execute(Player player, PlayerSwapHandItemsEvent event) {
        org.bukkit.event.player.PlayerSwapHandItemsEvent e;
        if (!playingPlayer.contains(player.getName())) return;
        event.setCancelled(true);
        if (Main.getInstance().getPlayerConfig().getConfig().getBoolean(player.getName() + ".auto-next-msg", false))
            return;
        if(!skipingPlayer.contains(player.getName())){
            skipingPlayer.add(player.getName());
            return;
        }
        end(player);
        next(player);
    }

    @Override
    public void start(Player player) {
        playingPlayer.add(player.getName());
        play(player, 0);
    }

    public void play(Player player, int index) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
            int i = between(index, 0, message.length());
            if(i + 1 <= message.length() && message.substring(i, i + 1).equalsIgnoreCase(String.valueOf(ChatColor.COLOR_CHAR))) i = Math.min(i + 1, message.length());
            String msg = message.substring(0, i);
            player.sendTitle(
                    "§7§o| §r" + sender + " §7:",
                    msg.replace("`", "") + (msg.endsWith("`") ? " " : "") + (i < message.length() ? (ChatColor.getLastColors(sender) + "§l§k_") : ""),
                    0, 20, 10);
            if(i < message.length() && !msg.endsWith("`")){
                try {
                    player.playNote(player.getLocation(), Instrument.PIANO, new Note(5));
                }catch(Throwable ignored){
                }
            }
            if (playingPlayer.contains(player.getName())) {
                if(i >= message.length() && !skipingPlayer.contains(player.getName())) skipingPlayer.add(player.getName());
                play(player, skipingPlayer.contains(player.getName()) ? message.length() : (i + 1));
            } else {
                // 开启自动对话时
                if (Main.getInstance().getPlayerConfig().getConfig().getBoolean(player.getName() + ".auto-next-msg", false)) {
                    end(player);
                    Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> next(player), 30);
                }
            }
        }, 2);
    }

    @Override
    public void end(Player player) {
        playingPlayer.remove(player.getName());
        skipingPlayer.remove(player.getName());
        player.sendTitle("§7§o| §r" + sender + " §7:", message.replace("`", ""), 0, 1, 10);
    }

    @Override
    public boolean hasEvent() {
        return true;
    }

    public static int between(int num, int a, int b){
        return Math.min(Math.max(Math.min(a, b), num), Math.max(a, b));
    }
}
