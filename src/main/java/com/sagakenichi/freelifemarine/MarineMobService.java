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
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public final class MarineMobService {

    private static final double DIRECTION_EPSILON = 1.0E-6;
    private static final double MAX_SEAT_SPEED = 1.45;

    private final JavaPlugin plugin;
    private final Map<UUID, MarineMob> byAnchor = new HashMap<>();
    private final Map<UUID, MarineMob> byEntity = new HashMap<>();
    private BukkitTask movementTask;

    public MarineMobService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        movementTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public MarineMob spawn(Player owner, MarineMobType type) {
        Location origin = owner.getLocation();
        Vector horizontal = origin.getDirection().setY(0.0);
        if (horizontal.lengthSquared() < DIRECTION_EPSILON) {
            horizontal = forwardFromYaw(origin.getYaw());
        }
        horizontal.normalize().multiply(3.0);

        double verticalOffset = type == MarineMobType.CRAB ? 0.15 : 0.40;
        Location spawn = origin.clone().add(horizontal).add(0.0, verticalOffset, 0.0);
        World world = spawn.getWorld();
        if (world == null) {
            throw new IllegalStateException("Player world is unavailable");
        }

        LivingEntity anchor = createAnchor(world, spawn, type);
        Interaction interaction = world.spawn(spawn, Interaction.class, hitbox -> {
            hitbox.setInteractionWidth(type.interactionWidth());
            hitbox.setInteractionHeight(type.interactionHeight());
            hitbox.setResponsive(true);
            hitbox.setPersistent(false);
        });

        List<BlockDisplay> displays = createDisplays(world, spawn, type);
        List<ArmorStand> passengerSeats = createPassengerSeats(world, spawn, type);

        MarineMob mob = new MarineMob(
                type,
                anchor,
                interaction,
                displays,
                passengerSeats,
                type.maxHealth(),
                origin.getYaw(),
                randomTicks(90, 180),
                ThreadLocalRandom.current().nextBoolean() ? 1 : -1,
                randomTicks(150, 250)
        );
        mob.wasInWater = isWaterContact(spawn);

        byAnchor.put(anchor.getUniqueId(), mob);
        byEntity.put(anchor.getUniqueId(), mob);
        byEntity.put(interaction.getUniqueId(), mob);
        updateFollowers(mob);
        updateDisplays(mob);
        return mob;
    }

    public MarineMob find(Entity entity) {
        return byEntity.get(entity.getUniqueId());
    }

    public boolean damage(Entity entity, double amount) {
        MarineMob mob = find(entity);
        if (mob == null || amount <= 0.0) {
            return false;
        }
        mob.health -= amount;
        if (mob.health <= 0.0) {
            remove(mob);
        }
        return true;
    }

    public boolean mount(Player player, Entity clicked) {
        MarineMob mob = find(clicked);
        if (mob == null || !mob.anchor.isValid() || !mob.type.rideable()) {
            return false;
        }
        if (player.isInsideVehicle()) {
            return true;
        }

        if (mob.anchor.getPassengers().isEmpty()) {
            mob.anchor.addPassenger(player);
            return true;
        }

        for (ArmorStand seat : mob.passengerSeats) {
            if (seat.isValid() && seat.getPassengers().isEmpty()) {
                seat.addPassenger(player);
                return true;
            }
        }

        player.sendMessage(mob.type.displayName() + " has no free seats.");
        return true;
    }

    public void shutdown() {
        if (movementTask != null) {
            movementTask.cancel();
            movementTask = null;
        }
        for (MarineMob mob : List.copyOf(byAnchor.values())) {
            remove(mob);
        }
        byAnchor.clear();
        byEntity.clear();
    }

    private void tick() {
        Iterator<Map.Entry<UUID, MarineMob>> iterator = byAnchor.entrySet().iterator();
        while (iterator.hasNext()) {
            MarineMob mob = iterator.next().getValue();
            if (!mob.anchor.isValid() || mob.anchor.isDead()) {
                removeEntities(mob);
                byEntity.remove(mob.anchor.getUniqueId());
                byEntity.remove(mob.interaction.getUniqueId());
                iterator.remove();
                continue;
            }

            mob.ageTicks++;
            mob.anchor.setRemainingAir(mob.anchor.getMaximumAir());
            mob.anchor.setFallDistance(0.0F);

            boolean inWater = isWaterContact(mob.anchor.getLocation());
            handleWaterTransition(mob, inWater);

            Player pilot = nativePilot(mob.anchor);
            if (mob.anchor instanceof Horse horse) {
                horse.setAI(pilot != null);
            }
            if (mob.type.movementStyle() == MarineMobType.MovementStyle.AQUATIC) {
                if (pilot != null) {
                    moveWithNativeRider(mob, inWater);
                } else {
                    moveAquaticAutonomously(mob, inWater);
                }
            } else {
                moveCrab(mob, inWater);
            }

            emitWakeAndBreath(mob, inWater);
            updateFollowers(mob);
            if ((mob.ageTicks & 1L) == 0L) {
                updateDisplays(mob);
            }
        }
    }

    private void moveWithNativeRider(MarineMob mob, boolean inWater) {
        mob.anchor.setGravity(true);

        Vector velocity = mob.anchor.getVelocity();
        double horizontalSpeed = Math.hypot(velocity.getX(), velocity.getZ());
        if (horizontalSpeed > mob.type.rideSpeed()) {
            double factor = mob.type.rideSpeed() / horizontalSpeed;
            velocity.setX(velocity.getX() * factor);
            velocity.setZ(velocity.getZ() * factor);
            mob.anchor.setVelocity(velocity);
        } else if (inWater && horizontalSpeed > 0.025) {
            double assisted = Math.min(mob.type.rideSpeed(), horizontalSpeed * 1.06 + 0.002);
            double factor = assisted / horizontalSpeed;
            velocity.setX(velocity.getX() * factor);
            velocity.setZ(velocity.getZ() * factor);
            mob.anchor.setVelocity(velocity);
        }

        if (!inWater) {
            Vector slowed = mob.anchor.getVelocity();
            slowed.setX(slowed.getX() * 0.55);
            slowed.setZ(slowed.getZ() * 0.55);
            mob.anchor.setVelocity(slowed);
        }
    }

    private void moveAquaticAutonomously(MarineMob mob, boolean inWater) {
        Location location = mob.anchor.getLocation();
        mob.anchor.setGravity(!inWater);

        if (!inWater) {
            if (mob.ageTicks % 20L == 0L || mob.cachedWaterDirection == null) {
                mob.cachedWaterDirection = findNearbyWaterDirection(location, 6);
            }
            Vector towardWater = mob.cachedWaterDirection;
            if (towardWater != null && towardWater.lengthSquared() > DIRECTION_EPSILON) {
                Vector horizontal = towardWater.clone().setY(0.0).normalize();
                float targetYaw = yawFromVector(horizontal);
                float yaw = turnTowards(location.getYaw(), targetYaw, 4.0F);
                mob.anchor.setRotation(yaw, 0.0F);
                Vector velocity = horizontal.multiply(mob.type.cruiseSpeed() * 0.48);
                velocity.setY(isSolidBelow(location) && mob.ageTicks % 12L == 0L ? 0.13 : -0.08);
                mob.anchor.setVelocity(velocity);
            } else {
                Vector velocity = mob.anchor.getVelocity().multiply(0.45);
                velocity.setY(isSolidBelow(location) ? 0.0 : -0.08);
                mob.anchor.setVelocity(velocity);
            }
            return;
        }

        mob.cachedWaterDirection = null;
        mob.turnTicks--;
        Vector currentForward = forwardFromYaw(location.getYaw());
        Location ahead = location.clone().add(currentForward.clone().multiply(1.8));
        boolean waterAhead = isWaterAt(ahead) || isWaterAt(ahead.clone().add(0.0, -0.7, 0.0));
        if (mob.turnTicks <= 0 || !waterAhead) {
            double turn = waterAhead
                    ? ThreadLocalRandom.current().nextDouble(-55.0, 55.0)
                    : ThreadLocalRandom.current().nextDouble(100.0, 220.0);
            mob.targetYaw = normalizeYaw((float) (location.getYaw() + turn));
            mob.turnTicks = randomTicks(80, 170);
        }

        float yaw = turnTowards(location.getYaw(), mob.targetYaw, 1.6F);
        Vector forward = forwardFromYaw(yaw);
        double vertical = Math.sin(mob.ageTicks * 0.055) * 0.018;
        if (isNearSurface(location)) {
            vertical -= 0.018;
        }
        if (!isWaterAt(location.clone().add(0.0, -0.75, 0.0))) {
            vertical += 0.032;
        }

        Vector velocity = forward.multiply(mob.type.cruiseSpeed()).setY(vertical);
        mob.anchor.setVelocity(velocity);
        mob.anchor.setRotation(yaw, (float) clamp(-vertical * 120.0, -8.0, 8.0));
    }

    private void moveCrab(MarineMob mob, boolean inWater) {
        mob.anchor.setGravity(true);
        Location location = mob.anchor.getLocation();
        mob.turnTicks--;
        if (mob.turnTicks <= 0) {
            mob.targetYaw = normalizeYaw((float) (location.getYaw()
                    + ThreadLocalRandom.current().nextDouble(-35.0, 35.0)));
            if (ThreadLocalRandom.current().nextDouble() < 0.45) {
                mob.sideDirection *= -1;
            }
            mob.turnTicks = randomTicks(75, 155);
        }

        float yaw = turnTowards(location.getYaw(), mob.targetYaw, 1.2F);
        Vector forward = forwardFromYaw(yaw);
        Vector side = new Vector(forward.getZ(), 0.0, -forward.getX()).multiply(mob.sideDirection);
        Location next = location.clone().add(side.clone().multiply(0.75));
        if (next.getBlock().getType().isSolid()) {
            mob.sideDirection *= -1;
            side.multiply(-1.0);
        }

        double speed = inWater ? mob.type.cruiseSpeed() * 0.72 : mob.type.cruiseSpeed();
        Vector velocity = side.normalize().multiply(speed);
        velocity.setY(inWater ? Math.sin(mob.ageTicks * 0.10) * 0.008 : (isSolidBelow(location) ? 0.0 : -0.11));
        mob.anchor.setVelocity(velocity);
        mob.anchor.setRotation(yaw, 0.0F);
    }

    private void handleWaterTransition(MarineMob mob, boolean inWater) {
        if (inWater == mob.wasInWater) {
            return;
        }
        mob.wasInWater = inWater;
        Location location = mob.anchor.getLocation();
        World world = location.getWorld();
        if (world == null) {
            return;
        }

        int splashCount = switch (mob.type) {
            case ORCA -> 72;
            case SHARK -> 38;
            case CRAB -> 16;
        };
        double spread = mob.type == MarineMobType.ORCA ? 1.85 : mob.type == MarineMobType.SHARK ? 1.0 : 0.45;
        world.spawnParticle(Particle.SPLASH, location, splashCount, spread, 0.60, spread, 0.18);
        if (inWater) {
            world.spawnParticle(Particle.BUBBLE, location, Math.max(6, splashCount / 3), spread * 0.7, 0.45, spread * 0.7, 0.08);
        }
        world.playSound(location, Sound.ENTITY_GENERIC_SPLASH,
                mob.type == MarineMobType.ORCA ? 1.55F : 0.9F,
                mob.type == MarineMobType.CRAB ? 1.35F : 0.85F);
    }

    private void emitWakeAndBreath(MarineMob mob, boolean inWater) {
        if (!inWater) {
            return;
        }
        Location base = mob.anchor.getLocation();
        World world = base.getWorld();
        if (world == null) {
            return;
        }

        Vector velocity = mob.anchor.getVelocity();
        double horizontalSpeed = Math.hypot(velocity.getX(), velocity.getZ());
        if (horizontalSpeed > 0.08 && isNearSurface(base) && mob.ageTicks % 5L == 0L) {
            Vector forward = forwardFromYaw(base.getYaw());
            Location wake = base.clone().subtract(forward.multiply(mob.type == MarineMobType.ORCA ? 3.8 : 2.2));
            int count = mob.type == MarineMobType.ORCA ? 14 : mob.type == MarineMobType.SHARK ? 7 : 3;
            world.spawnParticle(Particle.SPLASH, wake, count, 0.62, 0.20, 0.62, 0.06);
        }

        if (mob.type == MarineMobType.ORCA && isNearSurface(base) && mob.ageTicks >= mob.nextBreathTick) {
            Vector forward = forwardFromYaw(base.getYaw());
            Location blowhole = base.clone().add(forward.multiply(2.0)).add(0.0, 2.15, 0.0);
            world.spawnParticle(Particle.CLOUD, blowhole, 16, 0.25, 0.60, 0.25, 0.025);
            world.spawnParticle(Particle.SPLASH, blowhole, 9, 0.30, 0.25, 0.30, 0.05);
            mob.nextBreathTick = mob.ageTicks + randomTicks(150, 260);
        }
    }

    private void updateFollowers(MarineMob mob) {
        Location base = mob.anchor.getLocation();
        mob.interaction.teleport(base.clone().add(0.0, 0.15, 0.0));
        mob.interaction.setRotation(base.getYaw(), 0.0F);

        List<MarineMobType.SeatOffset> offsets = mob.type.seats();
        for (int index = 0; index < mob.passengerSeats.size(); index++) {
            ArmorStand seat = mob.passengerSeats.get(index);
            if (!seat.isValid()) {
                continue;
            }
            MarineMobType.SeatOffset offset = offsets.get(index + 1);
            Location target = relative(base, offset.forward(), offset.up(), offset.right());
            if (seat.getPassengers().isEmpty()) {
                seat.teleport(target);
                seat.setVelocity(mob.anchor.getVelocity());
            } else {
                Vector correction = target.toVector().subtract(seat.getLocation().toVector()).multiply(0.38);
                Vector desired = mob.anchor.getVelocity().clone().add(correction);
                if (desired.lengthSquared() > MAX_SEAT_SPEED * MAX_SEAT_SPEED) {
                    desired.normalize().multiply(MAX_SEAT_SPEED);
                }
                seat.setVelocity(desired);
            }
            seat.setRotation(base.getYaw(), 0.0F);
        }
    }

    private void updateDisplays(MarineMob mob) {
        Location base = mob.anchor.getLocation();
        float yaw = base.getYaw();
        double verticalVelocity = mob.anchor.getVelocity().getY();
        float pitch = mob.type.movementStyle() == MarineMobType.MovementStyle.AQUATIC
                ? (float) clamp(-verticalVelocity * 38.0, -16.0, 16.0)
                : 0.0F;
        double horizontalSpeed = Math.hypot(mob.anchor.getVelocity().getX(), mob.anchor.getVelocity().getZ());
        double motionScale = clamp(horizontalSpeed / Math.max(0.08, mob.type.cruiseSpeed()), 0.18, 1.35);
        List<MarineMobType.ModelPart> parts = mob.type.parts();

        for (int index = 0; index < parts.size(); index++) {
            MarineMobType.ModelPart part = parts.get(index);
            BlockDisplay display = mob.displays.get(index);
            double wave = animationWave(mob.ageTicks, part) * motionScale;
            double forward = part.forward();
            double up = part.up();
            double right = part.right();

            switch (part.animation()) {
                case SHARK_TAIL -> right += wave * 0.22;
                case ORCA_FLUKE -> up += wave * 0.14;
                case CRAB_LEG_A, CRAB_LEG_B -> up += Math.abs(wave) * 0.055;
                case STATIC -> { }
            }

            Location target = relative(base, forward, up, right);
            display.teleport(target);
            display.setRotation(yaw, pitch);
            display.setTransformation(transformation(part, wave));
        }
    }

    private static double animationWave(long ageTicks, MarineMobType.ModelPart part) {
        return switch (part.animation()) {
            case SHARK_TAIL -> Math.sin(ageTicks * 0.42 + part.phase());
            case ORCA_FLUKE -> Math.sin(ageTicks * 0.30 + part.phase());
            case CRAB_LEG_A -> Math.sin(ageTicks * 0.55 + part.phase());
            case CRAB_LEG_B -> -Math.sin(ageTicks * 0.55 + part.phase());
            case STATIC -> 0.0;
        };
    }

    private static Transformation transformation(MarineMobType.ModelPart part, double wave) {
        float dynamicDegrees = switch (part.animation()) {
            case SHARK_TAIL -> (float) (wave * 28.0);
            case ORCA_FLUKE -> (float) (wave * 20.0);
            case CRAB_LEG_A, CRAB_LEG_B -> (float) (wave * 18.0);
            case STATIC -> 0.0F;
        };
        AxisAngle4f rotation = rotation(part.rotationAxis(), part.baseDegrees() + dynamicDegrees);
        return new Transformation(
                new Vector3f(-part.scaleX() / 2.0F, -part.scaleY() / 2.0F, -part.scaleZ() / 2.0F),
                rotation,
                new Vector3f(part.scaleX(), part.scaleY(), part.scaleZ()),
                new AxisAngle4f(0.0F, 0.0F, 1.0F, 0.0F)
        );
    }

    private static AxisAngle4f rotation(MarineMobType.RotationAxis axis, float degrees) {
        float radians = (float) Math.toRadians(degrees);
        return switch (axis) {
            case X -> new AxisAngle4f(radians, 1.0F, 0.0F, 0.0F);
            case Y -> new AxisAngle4f(radians, 0.0F, 1.0F, 0.0F);
            case Z -> new AxisAngle4f(radians, 0.0F, 0.0F, 1.0F);
            case NONE -> new AxisAngle4f(0.0F, 0.0F, 1.0F, 0.0F);
        };
    }

    private List<BlockDisplay> createDisplays(World world, Location spawn, MarineMobType type) {
        List<BlockDisplay> displays = new ArrayList<>(type.parts().size());
        for (MarineMobType.ModelPart part : type.parts()) {
            BlockDisplay display = world.spawn(spawn, BlockDisplay.class, entity -> {
                entity.setBlock(Bukkit.createBlockData(part.material()));
                entity.setGravity(false);
                entity.setPersistent(false);
                entity.setInvulnerable(true);
                entity.setShadowRadius(0.0F);
                entity.setShadowStrength(0.0F);
                entity.setInterpolationDelay(0);
                entity.setInterpolationDuration(2);
                entity.setTeleportDuration(2);
                entity.setViewRange(2.5F);
                entity.setDisplayWidth(Math.max(1.0F, type.interactionWidth()));
                entity.setDisplayHeight(Math.max(1.0F, type.interactionHeight()));
                entity.setTransformation(transformation(part, 0.0));
            });
            displays.add(display);
        }
        return displays;
    }

    private static LivingEntity createAnchor(World world, Location spawn, MarineMobType type) {
        if (type.rideable()) {
            return world.spawn(spawn, Horse.class, horse -> {
                horse.setTamed(true);
                horse.setAdult();
                horse.setDomestication(horse.getMaxDomestication());
                horse.setInvisible(true);
                horse.setSilent(true);
                horse.setCollidable(false);
                horse.setPersistent(false);
                horse.setRemoveWhenFarAway(false);
                horse.setJumpStrength(0.58);
                horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            });
        }

        return world.spawn(spawn, Slime.class, slime -> {
            slime.setSize(1);
            slime.setAI(false);
            slime.setInvisible(true);
            slime.setSilent(true);
            slime.setCollidable(false);
            slime.setPersistent(false);
            slime.setRemoveWhenFarAway(false);
        });
    }

    private static List<ArmorStand> createPassengerSeats(World world, Location spawn, MarineMobType type) {
        int extraSeatCount = Math.max(0, type.seats().size() - 1);
        List<ArmorStand> seats = new ArrayList<>(extraSeatCount);
        for (int index = 0; index < extraSeatCount; index++) {
            ArmorStand seat = world.spawn(spawn, ArmorStand.class, stand -> {
                stand.setVisible(false);
                stand.setSmall(true);
                stand.setMarker(true);
                stand.setGravity(false);
                stand.setPersistent(false);
                stand.setInvulnerable(true);
                stand.setSilent(true);
                stand.setBasePlate(false);
                stand.setArms(false);
            });
            seats.add(seat);
        }
        return seats;
    }

    private void remove(MarineMob mob) {
        byAnchor.remove(mob.anchor.getUniqueId());
        byEntity.remove(mob.anchor.getUniqueId());
        byEntity.remove(mob.interaction.getUniqueId());
        removeEntities(mob);
    }

    private static void removeEntities(MarineMob mob) {
        for (BlockDisplay display : mob.displays) {
            if (display.isValid()) {
                display.remove();
            }
        }
        for (ArmorStand seat : mob.passengerSeats) {
            if (seat.isValid()) {
                seat.eject();
                seat.remove();
            }
        }
        if (mob.interaction.isValid()) {
            mob.interaction.remove();
        }
        if (mob.anchor.isValid()) {
            mob.anchor.eject();
            mob.anchor.remove();
        }
    }

    private static Player nativePilot(LivingEntity anchor) {
        for (Entity passenger : anchor.getPassengers()) {
            if (passenger instanceof Player player) {
                return player;
            }
        }
        return null;
    }

    private static boolean isWaterContact(Location location) {
        return isWaterAt(location)
                || isWaterAt(location.clone().add(0.0, 0.75, 0.0))
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

    private static boolean isNearSurface(Location location) {
        return isWaterAt(location) && !isWaterAt(location.clone().add(0.0, 1.35, 0.0));
    }

    private static boolean isSolidBelow(Location location) {
        return location.clone().add(0.0, -0.58, 0.0).getBlock().getType().isSolid();
    }

    private static Vector findNearbyWaterDirection(Location origin, int radius) {
        double bestDistance = Double.MAX_VALUE;
        Vector best = null;
        World world = origin.getWorld();
        if (world == null) {
            return null;
        }

        for (int x = -radius; x <= radius; x += 2) {
            for (int z = -radius; z <= radius; z += 2) {
                for (int y = -2; y <= 1; y++) {
                    Location probe = origin.clone().add(x, y, z);
                    if (!isWaterAt(probe)) {
                        continue;
                    }
                    double distance = x * x + z * z + y * y;
                    if (distance < bestDistance && (x != 0 || z != 0)) {
                        bestDistance = distance;
                        best = new Vector(x, 0.0, z);
                    }
                }
            }
        }
        return best;
    }

    private static Location relative(Location base, double forward, double up, double right) {
        Vector forwardVector = forwardFromYaw(base.getYaw()).multiply(forward);
        Vector unitForward = forwardFromYaw(base.getYaw());
        Vector rightVector = new Vector(unitForward.getZ(), 0.0, -unitForward.getX()).multiply(right);
        return base.clone().add(forwardVector).add(rightVector).add(0.0, up, 0.0);
    }

    private static Vector forwardFromYaw(float yaw) {
        double radians = Math.toRadians(yaw);
        return new Vector(-Math.sin(radians), 0.0, Math.cos(radians));
    }

    private static float yawFromVector(Vector vector) {
        return (float) Math.toDegrees(Math.atan2(-vector.getX(), vector.getZ()));
    }

    private static float turnTowards(float current, float target, float maxStep) {
        float difference = normalizeYaw(target - current);
        if (difference > 180.0F) {
            difference -= 360.0F;
        }
        difference = (float) clamp(difference, -maxStep, maxStep);
        return normalizeYaw(current + difference);
    }

    private static float normalizeYaw(float yaw) {
        float normalized = yaw % 360.0F;
        return normalized < 0.0F ? normalized + 360.0F : normalized;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int randomTicks(int minInclusive, int maxExclusive) {
        return ThreadLocalRandom.current().nextInt(minInclusive, maxExclusive);
    }

    public static final class MarineMob {
        private final MarineMobType type;
        private final LivingEntity anchor;
        private final Interaction interaction;
        private final List<BlockDisplay> displays;
        private final List<ArmorStand> passengerSeats;
        private double health;
        private long ageTicks;
        private float targetYaw;
        private int turnTicks;
        private int sideDirection;
        private long nextBreathTick;
        private boolean wasInWater;
        private Vector cachedWaterDirection;

        private MarineMob(MarineMobType type, LivingEntity anchor, Interaction interaction,
                          List<BlockDisplay> displays, List<ArmorStand> passengerSeats,
                          double health, float targetYaw, int turnTicks,
                          int sideDirection, long nextBreathTick) {
            this.type = type;
            this.anchor = anchor;
            this.interaction = interaction;
            this.displays = List.copyOf(displays);
            this.passengerSeats = List.copyOf(passengerSeats);
            this.health = health;
            this.targetYaw = targetYaw;
            this.turnTicks = turnTicks;
            this.sideDirection = sideDirection;
            this.nextBreathTick = nextBreathTick;
        }

        public MarineMobType type() { return type; }
        public double health() { return health; }
        public int seatCount() { return type.seats().size(); }
    }
}
