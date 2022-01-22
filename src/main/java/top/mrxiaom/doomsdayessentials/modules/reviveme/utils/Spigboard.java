package top.mrxiaom.doomsdayessentials.modules.reviveme.utils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Spigboard {
    private final Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    private final Objective objective;
    private final BiMap<String, SpigboardEntry> entries;
    private int teamId;

    public Spigboard(String title) {
        this.objective = this.scoreboard.registerNewObjective("spigobjective", "dummy", "spigobjective");
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("hearts");
        if (objective != null) {
            Objective objective2 = this.scoreboard.registerNewObjective("hearts", "health", objective.getDisplayName(), objective.getRenderType());
            objective2.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        }

        this.setTitle(title);
        this.entries = HashBiMap.create();
        this.teamId = 1;
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public Objective getObjective() {
        return this.objective;
    }

    public void setTitle(String title) {
        this.objective.setDisplayName(title);
    }

    public BiMap<String, SpigboardEntry> getEntries() {
        return HashBiMap.create(this.entries);
    }

    public SpigboardEntry getEntry(String key) {
        return this.entries.get(key);
    }

    public SpigboardEntry add(String name, int value) {
        return this.add((String) null, name, value, true);
    }

    public SpigboardEntry add(Enum<?> key, String name, int value) {
        return this.add(key.name(), name, value);
    }

    public SpigboardEntry add(String key, String name, int value) {
        return this.add(key, name, value, false);
    }

    public SpigboardEntry add(Enum<?> key, String name, int value, boolean overwrite) {
        return this.add(key.name(), name, value, overwrite);
    }

    public SpigboardEntry add(String key, String name, int value, boolean overwrite) {
        if (key == null && !this.contains(name)) {
            throw new IllegalArgumentException("Entry could not be found with the supplied name and no key was supplied");
        } else if (overwrite && this.contains(name)) {
            SpigboardEntry entry = this.getEntryByName(name);
            if (key != null && this.entries.get(key) != entry) {
                throw new IllegalArgumentException("Supplied key references a different score than the one to be overwritten");
            } else {
                entry.setValue(value);
                return entry;
            }
        } else if (this.entries.get(key) != null) {
            throw new IllegalArgumentException("Score already exists with that key");
        } else {
            int count = 0;
            if (!overwrite) {
                Map<Integer, String> created = this.create(name);
                for(Entry<Integer, String> entry : created.entrySet()){
                    count = entry.getKey();
                    name = entry.getValue();
                }
            }

            SpigboardEntry entry = new SpigboardEntry(key, this, value, name, count);
            entry.update(name);
            this.entries.put(key, entry);
            return entry;
        }
    }

    public void remove(String key) {
        this.remove(this.getEntry(key));
    }

    public void remove(SpigboardEntry entry) {
        if (entry.getSpigboard() != this) {
            throw new IllegalArgumentException("Supplied entry does not belong to this Spigboard");
        } else {
            String key = this.entries.inverse().get(entry);
            if (key != null) {
                this.entries.remove(key);
            }

            entry.remove();
        }
    }

    private Map<Integer, String> create(String name) {
        int count;
        for (count = 0; this.contains(name); ++count) {
            name = ChatColor.RESET + name;
        }

        if (name.length() > 48) {
            name = name.substring(0, 47);
        }

        if (this.contains(name)) {
            throw new IllegalArgumentException("Could not find a suitable replacement name for '" + name + "'");
        } else {
            Map<Integer, String> created = new HashMap<>();
            created.put(count, name);
            return created;
        }
    }

    public int getTeamId() {
        return this.teamId++;
    }

    public SpigboardEntry getEntryByName(String name) {
        for (SpigboardEntry entry : this.entries.values()) {
            if (entry.getName().equals(name)) {
                return entry;
            }
        }

        return null;
    }

    public boolean contains(String name) {
        for (SpigboardEntry entry : this.entries.values()) {
            if (entry.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public void add(Player player) {
        player.setScoreboard(this.scoreboard);
    }
}
