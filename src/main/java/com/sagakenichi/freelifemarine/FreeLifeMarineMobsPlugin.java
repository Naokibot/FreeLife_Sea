package com.sagakenichi.freelifemarine;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class FreeLifeMarineMobsPlugin extends JavaPlugin {

    private MarineMobService mobs;
    private OrcaShowManager shows;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        mobs = new MarineMobService(this);
        shows = new OrcaShowManager(this, mobs);
        MarineCommand command = new MarineCommand(mobs, shows);
        PluginCommand marine = getCommand("marine");
        if (marine == null) {
            throw new IllegalStateException("Command 'marine' is missing from plugin.yml");
        }
        marine.setExecutor(command);
        marine.setTabCompleter(command);
        getServer().getPluginManager().registerEvents(new MarineMobListener(mobs), this);
        mobs.start();
        shows.start();
        getLogger().info("FreeLifeMarineMobs 1.3.0 enabled: scheduled orca shows and show music are available.");
    }

    @Override
    public void onDisable() {
        if (shows != null) {
            shows.shutdown();
        }
        if (mobs != null) {
            mobs.shutdown();
        }
    }
}
