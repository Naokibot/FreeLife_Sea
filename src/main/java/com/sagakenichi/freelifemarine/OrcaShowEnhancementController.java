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
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Adds the large show breach and landing splash without changing normal autonomous
 * or ridden movement. The existing show controller signals a jump with a uniquely
 * strong upward impulse; this final pass upgrades only that impulse.
 */
final class OrcaShowEnhancementController {

    private static final double SHOW_JUMP_TRIGGER_VERTICAL = 0.50;
    private static final int TARGET_HEIGHT_ABOVE_SURFACE = 10;
    private static final int MAX_SURFACE_SCAN_BLOCKS = 3;

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
                Location location = entity.getLocation();
                boolean inWater = isWaterContact(location);
                Vector velocity = entity.getVelocity();

                if (!state.active && inWater && velocity.getY() >= SHOW_JUMP_TRIGGER_VERTICAL) {
                    int requiredRise = requiredRiseFromCurrentDepth(location);
                    double vertical = MarineJumpProfile.initialVerticalVelocity(requiredRise);
                    entity.setVelocity(velocity.clone().setY(vertical));
                    entity.setRotation(location.getYaw(), -16.0F);
                    state.active = true;
                    state.leftWater = false;
                }

                if (!state.active) {
                    continue;
                }
                if (!inWater) {
                    state.leftWater = true;
                } else if (state.leftWater) {
                    emitLandingSplash(world, location);
                    state.active = false;
                    state.leftWater = false;
                }
            }
        }
        jumpStates.keySet().retainAll(seen);
    }

    private static int requiredRiseFromCurrentDepth(Location location) {
        int waterBlocksAbove = 0;
        for (int offset = 1; offset <= MAX_SURFACE_SCAN_BLOCKS; offset++) {
            if (!isWaterAt(location.clone().add(0.0, offset, 0.0))) {
                break;
            }
            waterBlocksAbove = offset;
        }
        return Math.min(13, TARGET_HEIGHT_ABOVE_SURFACE + waterBlocksAbove);
    }

    private static void emitLandingSplash(World world, Location location) {
        Location impact = location.clone().add(0.0, 0.45, 0.0);

        // The wide layer reaches roughly eight blocks from the impact point.
        world.spawnParticle(Particle.SPLASH, impact, 280,
                8.0, 1.8, 8.0, 0.52);
        // A tall center plume makes the re-entry look heavy rather than flat.
        world.spawnParticle(Particle.SPLASH, impact, 160,
                3.0, 4.2, 3.0, 0.68);
        world.spawnParticle(Particle.CLOUD, impact, 56,
                3.2, 2.8, 3.2, 0.18);
        world.spawnParticle(Particle.BUBBLE, location, 96,
                4.5, 1.1, 4.5, 0.20);
        world.playSound(location, Sound.ENTITY_GENERIC_SPLASH, 2.0F, 0.62F);
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
        private boolean active;
        private boolean leftWater;
    }
}
