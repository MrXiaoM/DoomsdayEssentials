package top.mrxiaom.doomsdayessentials.modules.reviveme.utils;

import com.google.common.base.Splitter;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class SpigboardEntry {
    private final String key;
    private final Spigboard spigboard;
    private String name;
    private Team team;
    private Score score;
    private int value;
    private String origName;
    private final int count;

    public SpigboardEntry(@NotNull String key, @NotNull Spigboard spigboard, int value) {
        this.key = key;
        this.spigboard = spigboard;
        this.value = value;
        this.count = 0;
    }

    public SpigboardEntry(String key, Spigboard spigboard, int value, String origName, int count) {
        this.key = key;
        this.spigboard = spigboard;
        this.value = value;
        this.origName = origName;
        this.count = count;
    }

    public String getKey() {
        return this.key;
    }

    public Spigboard getSpigboard() {
        return this.spigboard;
    }

    public String getName() {
        return this.name;
    }

    public Team getTeam() {
        return this.team;
    }

    public Score getScore() {
        return this.score;
    }

    public int getValue() {
        return this.score != null ? (this.value = this.score.getScore()) : this.value;
    }

    public void setValue(int value) {
        if (!this.score.isScoreSet()) {
            this.score.setScore(-1);
        }

        this.score.setScore(value);
    }

    public void update(String newName) {
        int value = this.getValue();
        if (newName.equals(this.origName)) {
            StringBuilder newNameBuilder = new StringBuilder(newName);
            for (int i = 0; i < this.count; ++i) {
                newNameBuilder.insert(0, ChatColor.RESET);
            }
            newName = newNameBuilder.toString();
        } else if (newName.equals(this.name)) {
            return;
        }

        this.create(newName);
        this.setValue(value);
    }

    void remove() {
        if (this.score != null) {
            this.score.getScoreboard().resetScores(this.score.getEntry());
        }

    }

    private void create(String name) {
        this.name = name;
        this.remove();
        if (name.length() <= 46) {
            int value = this.getValue();
            this.score = this.spigboard.getObjective().getScore(name);
            this.score.setScore(value);
        } else {
            this.team = this.spigboard.getScoreboard().registerNewTeam("spigboard-" + this.spigboard.getTeamId());
            Iterator<String> iterator = Splitter.fixedLength(16).split(name).iterator();
            this.team.setPrefix(iterator.next());

            String entry = iterator.next();
            this.score = this.spigboard.getObjective().getScore(entry);
            this.team.setSuffix(iterator.next());

            this.team.addEntry(entry);
        }
    }
}
