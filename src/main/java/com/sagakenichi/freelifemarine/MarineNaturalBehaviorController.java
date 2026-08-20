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
import org.bukkit.entity.Boat;
import org.bukkit.entity.ChestBoat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Last-stage natural-motion layer for unpiloted aquatic mobs.
 *
 * <p>The normal marine AI remains responsible for food pursuit, jumps, breathing,
 * wall avoidance, and show control. This layer only prevents an autonomous animal
 * from settling into a near-stop and adds slow variation to speed, heading, and depth.
 * It also gives the visible animal body a solid interaction with boats even though the
 * invisible movement carrier itself is intentionally non-collidable.</p>
 */
final class MarineNaturalBehaviorController {

    private static final double DIRECTION_EPSILON = 1.0E-6;
    private static final double STRONG_VERTICAL_MANEUVER = 0.075;
    private static final double BODY_MARGIN = 0.10;
    private static final double[] WATER_SEARCH_ANGLES = {
            0.0, 18.0, -18.0, 36.0, -36.0, 60.0, -60.0,
            90.0, -90.0, 135.0, -135.0, 180.0
    };

    private final JavaPlugin plugin;
    private final MarineMobService mobs;
    private final Map<UUID, SwimState> swimStates = new HashMap<>();
    private BukkitTask task;
    private long serverTick;

    MarineNaturalBehaviorController(JavaPlugin plugin, MarineMobService mobs) {
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
        swimStates.clear();
    }

    private void tick() {
        serverTick++;
        Set<UUID> seen = new HashSet<>();

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntitiesByClasses(Horse.class, Slime.class)) {
                MarineMobService.MarineMob mob = mobs.find(entity);
                if (mob == null || !mob.id().equals(entity.getUniqueId())) {
                    continue;
                }

                UUID id = entity.getUniqueId();
                seen.add(id);
                if (mob.type().movementStyle() != MarineMobType.MovementStyle.AQUATIC) {
                    continue;
                }

                breakCollidingBoats(entity, mob.type());

                if (mob.showControlled() || hasPlayerPassenger(entity)) {
                    continue;
                }
                Location location = entity.getLocation();
                if (!isWaterContact(location)) {
                    continue;
                }

                Vector velocity = entity.getVelocity();
                if (Math.abs(velocity.getY()) > STRONG_VERTICAL_MANEUVER) {
                    continue;
                }

                SwimState state = swimStates.computeIfAbsent(id,
                        ignored -> SwimState.create(mob.type(), serverTick));
                state.update(mob.type(), serverTick);
                applyNaturalCruise(entity, mob, state);
            }
        }

        swimStates.keySet().retainAll(seen);
    }

    private void applyNaturalCruise(Entity entity, MarineMobService.MarineMob mob, SwimState state) {
        Location location = entity.getLocation();
        Vector velocity = entity.getVelocity();
        double currentSpeed = Math.hypot(velocity.getX(), velocity.getZ());

        Vector baseDirection = velocity.clone().setY(0.0);
        if (baseDirection.lengthSquared() < DIRECTION_EPSILON) {
            baseDirection = forwardFromYaw(location.getYaw());
        } else {
            baseDirection.normalize();
        }

        // Apply only the change in the slow weave curve. Applying the absolute curve
        // every tick would accumulate rotation and eventually make the animal spin.
        double weaveDelta = state.headingWeaveDelta(mob.type(), serverTick);
        Vector preferred = rotateY(baseDirection, Math.toRadians(weaveDelta));
        Vector direction = findOpenWaterDirection(location, preferred, mob.type());
        if (direction == null) {
            return;
        }

        double intendedSpeed = MarineSpeedLevel.of(mob.speedLevel()).blocksPerTick();
        double minimumSpeed = MarineSpeedLevel.of(
                MarineNaturalMotionProfile.continuousCruiseLevel(mob.type())).blocksPerTick();
        double pulse = MarineNaturalMotionProfile.pacePulse(mob.type(), serverTick, state.phase);
        double targetSpeed = intendedSpeed * state.pace * pulse;

        // Food pursuit and an already-started burst may temporarily be faster than the
        // current natural intent. Let that momentum decay gently rather than cancelling it.
        if (currentSpeed > intendedSpeed * 1.10) {
            targetSpeed = Math.max(targetSpeed, currentSpeed * 0.992);
        }
        targetSpeed = clamp(targetSpeed, minimumSpeed, MarineSpeedLevel.LEVEL_10.blocksPerTick());

        double speedStep = targetSpeed >= currentSpeed ? 0.035 : 0.018;
        double nextSpeed = moveTowards(currentSpeed, targetSpeed, speedStep);
        if (currentSpeed < 0.01) {
            nextSpeed = Math.max(nextSpeed, 0.04);
        }

        double vertical = velocity.getY();
        boolean waterAbove = isWaterAt(location.clone().add(0.0, 0.90, 0.0));
        boolean waterBelow = isWaterAt(location.clone().add(0.0, -0.90, 0.0));
        if (waterAbove && waterBelow && Math.abs(vertical) < 0.035) {
            double targetVertical = MarineNaturalMotionProfile.verticalWave(
                    mob.type(), serverTick, state.phase);
            vertical = moveTowards(vertical, targetVertical, 0.0035);
        }

        Vector natural = direction.multiply(nextSpeed).setY(vertical);
        entity.setVelocity(natural);
        entity.setRotation(yawFromVector(natural),
                (float) clamp(-vertical * 125.0, -11.0, 11.0));
    }

    private static Vector findOpenWaterDirection(Location location, Vector preferred, MarineMobType type) {
        double probeDistance = type == MarineMobType.ORCA ? 1.85 : 1.55;
        Vector unit = preferred.clone().setY(0.0);
        if (unit.lengthSquared() < DIRECTION_EPSILON) {
            unit = forwardFromYaw(location.getYaw());
        } else {
            unit.normalize();
        }

        for (double degrees : WATER_SEARCH_ANGLES) {
            Vector candidate = rotateY(unit, Math.toRadians(degrees)).normalize();
            Location ahead = location.clone().add(candidate.clone().multiply(probeDistance));
            if (isWaterAt(ahead)
                    || isWaterAt(ahead.clone().add(0.0, -0.65, 0.0))
                    || isWaterAt(ahead.clone().add(0.0, 0.45, 0.0))) {
                return candidate;
            }
        }
        return null;
    }

    private void breakCollidingBoats(Entity anchor, MarineMobType type) {
        if (!MarineNaturalMotionProfile.breaksBoats(type)) {
            return;
        }
        double radius = MarineNaturalMotionProfile.collisionScanRadius(type);
        Location base = anchor.getLocation();

        for (Entity nearby : anchor.getWorld().getNearbyEntities(base, radius, 3.5, radius)) {
            if (!(nearby instanceof Boat boat) || !boat.isValid()) {
                continue;
            }
            if (!bodyOverlapsBoat(base, type, boat.getBoundingBox())) {
                continue;
            }
            breakBoat(boat);
        }
    }

    private static boolean bodyOverlapsBoat(Location base, MarineMobType type, BoundingBox boatBox) {
        for (MarineHitboxProfile.Hitbox hitbox : MarineHitboxProfile.forType(type)) {
            Location center = relative(base, hitbox.forward(), hitbox.up(), hitbox.right());
            double halfWidth = hitbox.width() * 0.5 + BODY_MARGIN;
            BoundingBox body = new BoundingBox(
                    center.getX() - halfWidth,
                    center.getY() - BODY_MARGIN,
                    center.getZ() - halfWidth,
                    center.getX() + halfWidth,
                    center.getY() + hitbox.height() + BODY_MARGIN,
                    center.getZ() + halfWidth);
            if (body.overlaps(boatBox)) {
                return true;
            }
        }
        return false;
    }

    private static void breakBoat(Boat boat) {
        Location effect = boat.getLocation().clone().add(0.0, 0.35, 0.0);
        World world = boat.getWorld();

        // A chest boat should not silently delete its cargo when the hull is destroyed.
        if (boat instanceof ChestBoat chestBoat) {
            for (ItemStack stack : chestBoat.getInventory().getContents()) {
                if (stack == null || stack.getType().isAir()) {
                    continue;
                }
                world.dropItemNaturally(boat.getLocation(), stack.clone());
            }
            chestBoat.getInventory().clear();
        }

        boat.eject();
        boat.remove();
        world.spawnParticle(Particle.BLOCK, effect, 28,
                0.65, 0.35, 0.65, 0.08, Material.OAK_PLANKS.createBlockData());
        world.spawnParticle(Particle.SPLASH, effect, 18,
                0.70, 0.25, 0.70, 0.10);
        world.playSound(effect, Sound.BLOCK_WOOD_BREAK, 1.25F, 0.82F);
    }

    private static boolean hasPlayerPassenger(Entity entity) {
        for (Entity passenger : entity.getPassengers()) {
            if (passenger instanceof Player) {
                return true;
            }
        }
        return false;
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

    private static Location relative(Location base, double forward, double up, double right) {
        Vector unitForward = forwardFromYaw(base.getYaw());
        Vector forwardVector = unitForward.clone().multiply(forward);
        Vector rightVector = new Vector(unitForward.getZ(), 0.0, -unitForward.getX()).multiply(right);
        return base.clone().add(forwardVector).add(rightVector).add(0.0, up, 0.0);
    }

    private static Vector forwardFromYaw(float yaw) {
        double radians = Math.toRadians(yaw);
        return new Vector(-Math.sin(radians), 0.0, Math.cos(radians));
    }

    private static Vector rotateY(Vector vector, double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double x = vector.getX() * cos + vector.getZ() * sin;
        double z = -vector.getX() * sin + vector.getZ() * cos;
        return new Vector(x, vector.getY(), z);
    }

    private static float yawFromVector(Vector vector) {
        return (float) Math.toDegrees(Math.atan2(-vector.getX(), vector.getZ()));
    }

    private static double moveTowards(double current, double target, double maxStep) {
        if (current < target) {
            return Math.min(target, current + maxStep);
        }
        return Math.max(target, current - maxStep);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static final class SwimState {
        private final double phase;
        private double pace;
        private double targetPace;
        private long nextPaceChangeTick;
        private double lastHeadingWeave;

        private SwimState(double phase, double pace, double targetPace,
                          long nextPaceChangeTick, double lastHeadingWeave) {
            this.phase = phase;
            this.pace = pace;
            this.targetPace = targetPace;
            this.nextPaceChangeTick = nextPaceChangeTick;
            this.lastHeadingWeave = lastHeadingWeave;
        }

        private static SwimState create(MarineMobType type, long tick) {
            ThreadLocalRandom random = ThreadLocalRandom.current();
            double phase = random.nextDouble(0.0, Math.PI * 2.0);
            double pace = random.nextDouble(
                    MarineNaturalMotionProfile.minPace(type),
                    Math.nextUp(MarineNaturalMotionProfile.maxPace(type)));
            long next = tick + random.nextInt(
                    MarineNaturalMotionProfile.minPaceHoldTicks(type),
                    MarineNaturalMotionProfile.maxPaceHoldTicksExclusive(type));
            double weave = MarineNaturalMotionProfile.headingWeaveDegrees(type, tick, phase);
            return new SwimState(phase, pace, pace, next, weave);
        }

        private void update(MarineMobType type, long tick) {
            if (tick >= nextPaceChangeTick) {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                targetPace = random.nextDouble(
                        MarineNaturalMotionProfile.minPace(type),
                        Math.nextUp(MarineNaturalMotionProfile.maxPace(type)));
                nextPaceChangeTick = tick + random.nextInt(
                        MarineNaturalMotionProfile.minPaceHoldTicks(type),
                        MarineNaturalMotionProfile.maxPaceHoldTicksExclusive(type));
            }
            pace += (targetPace - pace) * 0.035;
        }

        private double headingWeaveDelta(MarineMobType type, long tick) {
            double current = MarineNaturalMotionProfile.headingWeaveDegrees(type, tick, phase);
            double delta = current - lastHeadingWeave;
            lastHeadingWeave = current;
            return delta;
        }
    }
}
