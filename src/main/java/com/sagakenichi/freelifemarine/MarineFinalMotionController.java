package com.sagakenichi.freelifemarine;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Final per-tick motion pass for behavior that must win after the normal marine AI.
 *
 * <p>This is intentionally small: it fixes a live-server carrier-hover edge case and
 * applies the requested player-facing orca riding control without rewriting the
 * autonomous movement service.</p>
 */
final class MarineFinalMotionController {

    private static final double DIRECTION_EPSILON = 1.0E-6;

    private final JavaPlugin plugin;
    private final MarineMobService mobs;
    private final Map<UUID, Double> lastAirborneY = new HashMap<>();
    private BukkitTask task;

    MarineFinalMotionController(JavaPlugin plugin, MarineMobService mobs) {
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
        lastAirborneY.clear();
    }

    private void tick() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClasses(Horse.class, Slime.class)) {
                MarineMobService.MarineMob mob = mobs.find(entity);
                if (mob == null || !mob.id().equals(entity.getUniqueId())) {
                    continue;
                }

                boolean inWater = isStrictWaterContact(entity.getLocation());
                boolean unsupportedAir = MarineMotionTuning.isUnsupportedAir(inWater, entity.isOnGround());
                if (unsupportedAir) {
                    if (entity instanceof Horse horse) {
                        horse.setAI(false);
                    }
                    applyAirborneFallRecovery(entity);
                    continue;
                }

                lastAirborneY.remove(entity.getUniqueId());
                entity.setGravity(true);

                if (!(entity instanceof Horse horse) || mob.type() != MarineMobType.ORCA) {
                    continue;
                }

                Player pilot = firstPlayerPassenger(horse);
                if (pilot == null || !inWater || mob.showControlled()) {
                    continue;
                }

                steerRiddenOrca(horse, pilot);
            }
        }
    }

    private void applyAirborneFallRecovery(Entity entity) {
        entity.setGravity(true);
        UUID id = entity.getUniqueId();
        double currentY = entity.getLocation().getY();
        double previousY = lastAirborneY.getOrDefault(id, Double.NaN);
        Vector velocity = entity.getVelocity();

        if (MarineMotionTuning.needsFallKick(previousY, currentY, velocity.getY())) {
            velocity.setY(MarineMotionTuning.fallKickVelocity(velocity.getY()));
            entity.setVelocity(velocity);
        }
        lastAirborneY.put(id, currentY);
    }

    private void steerRiddenOrca(Horse horse, Player pilot) {
        horse.setAI(false);
        horse.setGravity(true);

        Vector forward = pilot.getEyeLocation().getDirection().setY(0.0);
        if (forward.lengthSquared() < DIRECTION_EPSILON) {
            forward = forwardFromYaw(pilot.getLocation().getYaw());
        } else {
            forward.normalize();
        }

        Location location = horse.getLocation();
        Vector current = horse.getVelocity();
        double vertical = shallowPool(location)
                ? clamp(current.getY(), -0.045, 0.045)
                : clamp(pilot.getEyeLocation().getDirection().getY() * 0.28, -0.22, 0.22);

        Vector target = forward.multiply(MarineMotionTuning.ORCA_RIDDEN_BLOCKS_PER_TICK).setY(vertical);
        horse.setVelocity(target);
        horse.setRotation(pilot.getLocation().getYaw(), 0.0F);
    }

    private static Player firstPlayerPassenger(Entity entity) {
        for (Entity passenger : entity.getPassengers()) {
            if (passenger instanceof Player player) {
                return player;
            }
        }
        return null;
    }

    private static boolean shallowPool(Location location) {
        return isWaterAt(location)
                && isWaterAt(location.clone().add(0.0, -1.0, 0.0))
                && !isWaterAt(location.clone().add(0.0, -2.0, 0.0));
    }

    private static boolean isStrictWaterContact(Location location) {
        return isWaterAt(location) || isWaterAt(location.clone().add(0.0, 0.35, 0.0));
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

    private static Vector forwardFromYaw(float yaw) {
        double radians = Math.toRadians(yaw);
        return new Vector(-Math.sin(radians), 0.0, Math.cos(radians));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
