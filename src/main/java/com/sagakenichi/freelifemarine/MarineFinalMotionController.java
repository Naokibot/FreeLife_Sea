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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Final per-tick motion pass for behavior that must win after the normal marine AI.
 *
 * <p>Airborne movement is integrated manually. This intentionally does not depend
 * on the invisible Horse/Slime carrier accepting Bukkit velocity updates, because
 * live servers have shown cases where the carrier keeps a non-zero velocity yet its
 * position remains suspended. Water and grounded movement still use the normal AI.</p>
 */
final class MarineFinalMotionController {

    private static final double DIRECTION_EPSILON = 1.0E-6;
    private static final double SUPPORT_PROBE = 0.10;

    private final JavaPlugin plugin;
    private final MarineMobService mobs;
    private final Map<UUID, AirState> airborne = new HashMap<>();
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
        airborne.clear();
    }

    private void tick() {
        Set<UUID> seen = new HashSet<>();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClasses(Horse.class, Slime.class)) {
                MarineMobService.MarineMob mob = mobs.find(entity);
                if (mob == null || !mob.id().equals(entity.getUniqueId())) {
                    continue;
                }
                seen.add(entity.getUniqueId());

                Location location = entity.getLocation();
                boolean inWater = isStrictWaterContact(location);
                boolean supported = entity.isOnGround() || hasSolidSupport(location);
                if (!inWater && !supported) {
                    if (entity instanceof Horse horse) {
                        horse.setAI(false);
                    }
                    integrateAirborne(entity);
                    continue;
                }

                airborne.remove(entity.getUniqueId());
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
        airborne.keySet().retainAll(seen);
    }

    private void integrateAirborne(Entity entity) {
        UUID id = entity.getUniqueId();
        AirState state = airborne.computeIfAbsent(id, ignored -> AirState.from(entity.getVelocity()));

        // The carrier's own gravity/velocity integration is deliberately disabled only
        // while airborne. We apply gravity ourselves and move the entity by coordinates.
        entity.setGravity(false);

        state.vertical = MarineAirKinematics.nextVerticalVelocity(state.vertical);
        Vector displacement = new Vector(state.horizontalX, state.vertical, state.horizontalZ);
        MoveResult result = sweepMove(entity.getLocation(), displacement);

        if (result.enteredWater) {
            entity.teleport(result.location);
            entity.setGravity(true);
            entity.setVelocity(new Vector(
                    state.horizontalX,
                    Math.min(state.vertical, -0.05),
                    state.horizontalZ));
            airborne.remove(id);
            return;
        }

        if (result.hitSolid) {
            entity.teleport(result.location);
            entity.setGravity(true);
            entity.setVelocity(new Vector(0.0, 0.0, 0.0));
            airborne.remove(id);
            return;
        }

        entity.teleport(result.location);
        entity.setVelocity(new Vector(0.0, 0.0, 0.0));
        state.horizontalX = MarineAirKinematics.nextHorizontalVelocity(state.horizontalX);
        state.horizontalZ = MarineAirKinematics.nextHorizontalVelocity(state.horizontalZ);
    }

    private static MoveResult sweepMove(Location start, Vector displacement) {
        int steps = MarineAirKinematics.sweepSteps(
                displacement.getX(), displacement.getY(), displacement.getZ());
        Vector increment = displacement.clone().multiply(1.0 / steps);
        Location current = start.clone();
        Location lastSafe = start.clone();

        for (int i = 0; i < steps; i++) {
            current.add(increment);
            if (isStrictWaterContact(current)) {
                return new MoveResult(current.clone(), true, false);
            }
            if (collidesAt(current)) {
                return new MoveResult(lastSafe, false, true);
            }
            lastSafe = current.clone();
        }
        return new MoveResult(current, false, false);
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

    private static boolean hasSolidSupport(Location location) {
        Location probe = location.clone().add(0.0, -SUPPORT_PROBE, 0.0);
        Block block = probe.getBlock();
        return block.getType().isSolid() && !block.isPassable();
    }

    private static boolean collidesAt(Location location) {
        return solidCollision(location)
                || solidCollision(location.clone().add(0.0, 0.90, 0.0))
                || solidCollision(location.clone().add(0.0, 1.65, 0.0));
    }

    private static boolean solidCollision(Location location) {
        Block block = location.getBlock();
        return block.getType().isSolid() && !block.isPassable();
    }

    private static boolean isStrictWaterContact(Location location) {
        return isWaterAt(location)
                || isWaterAt(location.clone().add(0.0, 0.35, 0.0));
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

    private static final class AirState {
        private double horizontalX;
        private double vertical;
        private double horizontalZ;

        private AirState(double horizontalX, double vertical, double horizontalZ) {
            this.horizontalX = horizontalX;
            this.vertical = vertical;
            this.horizontalZ = horizontalZ;
        }

        private static AirState from(Vector velocity) {
            return new AirState(velocity.getX(), velocity.getY(), velocity.getZ());
        }
    }

    private record MoveResult(Location location, boolean enteredWater, boolean hitSolid) {
    }
}
