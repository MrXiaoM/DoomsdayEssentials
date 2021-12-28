package top.mrxiaom.doomsdayessentials.chapter.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.chapter.IChapterTask;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MsgTask implements IChapterTask<PlayerInteractEvent> {
    public static Sound DEFAULT_SOUND = Sound.BLOCK_NOTE_BLOCK_HARP;
    String sender;
    String message;
    Sound sound;
    float pitch;
    float volume;
    List<String> playingPlayer = new ArrayList<>();
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
    public void execute(Player player, PlayerInteractEvent event) {
        if (!playingPlayer.contains(player.getName())) return;
        if (Main.getInstance().getPlayerConfig().getConfig().getBoolean(player.getName() + ".auto-next-msg", false))
            return;
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            playingPlayer.remove(player.getName());
            next(player);
        }
    }

    @Override
    public void start(Player player) {
        play(player, 0);
    }

    public void play(Player player, int index) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> {
            if (index >= 0 && index < message.length()) {
                player.sendTitle(sender, message, index == 0 ? 1 : 0, 40, 0);
                return;
            }
            if (playingPlayer.contains(player.getName())) {
                play(player, index);
            } else {

                if (Main.getInstance().getPlayerConfig().getConfig().getBoolean(player.getName() + ".auto-next-msg", false)) {
                    player.sendTitle(sender, message, 0, 35, 5);
                    Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), () -> next(player), 40);
                }
            }
        }, 1);
    }

    @Override
    public void end(Player player) {
        player.sendTitle(sender, message, 0, 5, 5);
    }

    @Override
    public boolean hasEvent() {
        return true;
    }
}
