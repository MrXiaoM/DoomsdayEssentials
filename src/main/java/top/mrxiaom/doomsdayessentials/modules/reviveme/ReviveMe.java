package top.mrxiaom.doomsdayessentials.modules.reviveme;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import top.mrxiaom.doomsdayessentials.Main;
import top.mrxiaom.doomsdayessentials.utils.Util;

import java.io.File;
import java.io.IOException;

public class ReviveMe implements Listener {
    final File configFile;
    public boolean Citizens = false;
    File cacheFile;
    FileConfiguration cache;
    FileConfiguration config;
    Main plugin;
    ReviveManager manager;
    File reviveManagerFile;
    public ReviveMe(Main plugin) {
        this.plugin = plugin;
        this.reviveManagerFile = new File(this.getDataFolder(), "ReviveManager.dat");
        this.cacheFile = new File(this.getDataFolder(), "antiCrashBugs_Cache.yml");
        this.cache = YamlConfiguration.loadConfiguration(this.cacheFile);
        this.configFile = new File(plugin.getDataFolder(), "reviveme.yml");
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
        onEnable();
    }

    public ReviveManager getManager(){
        return manager;
    }

    public static ReviveMe getInstance() {
        return Main.getInstance().getModuleReviveMe();
    }

    public File getManagerDataFile(){
        return reviveManagerFile;
    }

    public void onEnable() {
        if (!this.configFile.exists()) {
            this.reloadConfig();
            this.config.options().copyDefaults(true);
            this.saveConfig();
        }

        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getPluginManager().registerEvents(new EventsListener(), plugin);
        Bukkit.getConsoleSender().sendMessage("§aReviveMe enable");
        if (reviveManagerFile.exists()) {
            manager = (ReviveManager) Util.readObjectFromFile(reviveManagerFile).orElse(null);
            if(manager == null) manager = new ReviveManager();
        }
        else {
            manager = new ReviveManager();
        }

        if (this.config.getString("relivedHealth") == null) {
            this.config.set("relivedHealth", 0.5D);
            this.saveConfig();
        }

        if (this.config.getString("CitizensCompatibility.enable") == null) {
            this.config.set("CitizensCompatibility.enable", false);
            this.config.set("CitizensCompatibility.damagedNpc", false);
            this.saveConfig();
        }

        if (this.config.getString("EnableForceDeath") == null) {
            this.config.set("EnableForceDeath", true);
            this.saveConfig();
        }

        if (Bukkit.getPluginManager().isPluginEnabled("Citizens") && this.config.getBoolean("CitizensCompatibility.enable")) {
            this.Citizens = true;
        }

    }
    public void saveCache() {
        try {
            this.cache.save(this.cacheFile);
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    public Main getPlugin(){
        return plugin;
    }

    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    public FileConfiguration getConfig(){
        return this.config;
    }

    public void reloadConfig(){

    }

    public void saveConfig(){

    }

    public void debug(String message) {
        Bukkit.broadcastMessage(message);
    }
}
