package com.sagakenichi.freelifemarine;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Slime;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks show-only breach landings and emits the deliberately oversized re-entry splash.
 * Jump velocity is set at the source in MarineMobService so no later controller has to
 * race the show scheduler to turn a small hop into a real breach.
 */
final class OrcaShowEnhancementController {

    private final JavaPlugin plugin;
    private final MarineMobService mobs;
    private final Map<UUID, JumpState> jumpStates = new HashMap<>();
    private BukkitTask task;

    OrcaShowEnhancementController(JavaPlugin plugin, MarineMobService mobs) {
        this.plugin = plugin;
        this.mobs = mobs;
    }

    void start() {
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        jumpStates.clear();
    }

    private void tick() {
        Set<UUID> seen = new HashSet<>();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClasses(Horse.class, Slime.class)) {
                MarineMobService.MarineMob mob = mobs.find(entity);
                if (mob == null || !mob.id().equals(entity.getUniqueId())
                        || mob.type() != MarineMobType.ORCA || !mob.showControlled()) {
                    continue;
                }

                UUID id = entity.getUniqueId();
                seen.add(id);
                JumpState state = jumpStates.computeIfAbsent(id, ignored -> new JumpState());
                boolean inWater = isWaterContact(entity.getLocation());

                if (!mob.showJumpActive()) {
                    state.leftWater = false;
                    continue;
                }

                if (!inWater) {
                    state.leftWater = true;
                    if (entity.isOnGround()) {
                        mobs.finishShowJump(mob);
                        state.leftWater = false;
                    }
                    continue;
                }

                if (state.leftWater) {
                    emitLandingSplash(world, entity.getLocation());
                    mobs.finishShowJump(mob);
                    state.leftWater = false;
                }
            }
        }
        jumpStates.keySet().retainAll(seen);
    }

    private static void emitLandingSplash(World world, Location location) {
        Location impact = location.clone().add(0.0, 0.45, 0.0);

        // Wide sheet: visible out to roughly eight blocks from the impact point.
        world.spawnParticle(Particle.SPLASH, impact, 320,
                8.0, 1.8, 8.0, 0.58);
        // Tall central plume gives the orca a heavy, aquarium-show style re-entry.
        world.spawnParticle(Particle.SPLASH, impact, 190,
                3.2, 4.8, 3.2, 0.76);
        world.spawnParticle(Particle.CLOUD, impact, 64,
                3.4, 3.1, 3.4, 0.20);
        world.spawnParticle(Particle.BUBBLE, location, 110,
                4.8, 1.3, 4.8, 0.22);
        world.playSound(location, Sound.ENTITY_GENERIC_SPLASH, 2.0F, 0.58F);
    }

    private static boolean isWaterContact(Location location) {
        return isWaterAt(location)
                || isWaterAt(location.clone().add(0.0, 0.35, 0.0))
                || isWaterAt(location.clone().add(0.0, -0.45, 0.0));
    }

    private static boolean isWaterAt(Location location) {
        Block block = location.getBlock();
        Material type = block.getType();
        if (type == Material.WATER || type == Material.BUBBLE_COLUMN) {
            return true;
        }
        BlockData data = block.getBlockData();
        return data instanceof Waterlogged waterlogged && waterlogged.isWaterlogged();
    }

    private static final class JumpState {
        private boolean leftWater;
    }
}
