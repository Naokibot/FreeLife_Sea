package com.sagakenichi.freelifemarine;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class FreeLifeMarineMobsPlugin extends JavaPlugin {

    private MarineMobService mobs;

    @Override
    public void onEnable() {
        mobs = new MarineMobService(this);
        MarineCommand command = new MarineCommand(mobs);
        PluginCommand marine = getCommand("marine");
        if (marine == null) {
            throw new IllegalStateException("Command 'marine' is missing from plugin.yml");
        }
        marine.setExecutor(command);
        marine.setTabCompleter(command);
        getServer().getPluginManager().registerEvents(new MarineMobListener(mobs), this);
        mobs.start();
        getLogger().info("FreeLifeMarineMobs 1.0.0 enabled.");
    }

    @Override
    public void onDisable() {
        if (mobs != null) {
            mobs.shutdown();
        }
    }
}
