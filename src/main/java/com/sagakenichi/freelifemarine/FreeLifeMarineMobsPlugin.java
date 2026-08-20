package com.sagakenichi.freelifemarine;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class FreeLifeMarineMobsPlugin extends JavaPlugin {

    private MarineMobService mobs;
    private OrcaShowManager shows;
    private MarineFinalMotionController finalMotion;
    private MarineNaturalBehaviorController naturalBehavior;
    private OrcaShowEnhancementController showEnhancement;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        MarineFood food = new MarineFood(this);
        mobs = new MarineMobService(this, food);
        shows = new OrcaShowManager(this, mobs);
        finalMotion = new MarineFinalMotionController(this, mobs);
        naturalBehavior = new MarineNaturalBehaviorController(this, mobs);
        showEnhancement = new OrcaShowEnhancementController(this, mobs);
        MarineCommand command = new MarineCommand(mobs, food, shows);
        PluginCommand marine = getCommand("marine");
        if (marine == null) {
            throw new IllegalStateException("Command 'marine' is missing from plugin.yml");
        }
        marine.setExecutor(command);
        marine.setTabCompleter(command);
        getServer().getPluginManager().registerEvents(new MarineMobListener(mobs), this);
        mobs.start();
        shows.start();
        finalMotion.start();
        naturalBehavior.start();
        showEnhancement.start();
        getLogger().info("FreeLifeMarineMobs 1.10.0 enabled: continuous natural autonomy, boat-impact breaking, high breaches, deterministic air gravity, and 3D gaze riding are active.");
    }

    @Override
    public void onDisable() {
        if (showEnhancement != null) {
            showEnhancement.shutdown();
        }
        if (naturalBehavior != null) {
            naturalBehavior.shutdown();
        }
        if (finalMotion != null) {
            finalMotion.shutdown();
        }
        if (shows != null) {
            shows.shutdown();
        }
        if (mobs != null) {
            mobs.shutdown();
        }
    }
}
