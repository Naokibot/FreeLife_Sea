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
import org.bukkit.entity.Item;
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
import java.util.Collection;
import java.util.Comparator;
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
    private final MarineFood food;
    private final Map<UUID, MarineMob> byAnchor = new HashMap<>();
    private final Map<UUID, MarineMob> byEntity = new HashMap<>();
    private BukkitTask movementTask;

    public MarineMobService(JavaPlugin plugin, MarineFood food) {
        this.plugin = plugin;
        this.food = food;
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
        horizontal.normalize().multiply(type == MarineMobType.CRAB ? 1.6 : 3.0);

        double verticalOffset = type == MarineMobType.CRAB ? 0.08 : 0.40;
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
                randomTicks(100, 220),
                ThreadLocalRandom.current().nextBoolean() ? 1 : -1,
                randomTicks(160, 280)
        );
        mob.wasInWater = isWaterContact(spawn);
        refreshNaturalIntent(mob);

        byAnchor.put(anchor.getUniqueId(), mob);
        byEntity.put(anchor.getUniqueId(), mob);
        byEntity.put(interaction.getUniqueId(), mob);
        updateFollowers(mob);
        updateDisplays(mob);
        return mob;
    }

    public MarineMob find(Entity entity) {
        return entity == null ? null : byEntity.get(entity.getUniqueId());
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

    public boolean feed(Player player, Entity clicked) {
        MarineMob mob = find(clicked);
        if (mob == null || mob.showControlled || !food.isHeldBy(player)) {
            return false;
        }
        if (!food.consumeOne(player)) {
            return false;
        }
        mob.health = Math.min(mob.type.maxHealth(), mob.health + 2.0);
        Location location = mob.anchor.getLocation().add(0.0, mob.type == MarineMobType.CRAB ? 0.45 : 1.4, 0.0);
        World world = mob.anchor.getWorld();
        world.spawnParticle(Particle.HEART, location, 5, 0.35, 0.25, 0.35, 0.02);
        world.spawnParticle(Particle.BUBBLE, location, 7, 0.30, 0.25, 0.30, 0.04);
        world.playSound(mob.anchor.getLocation(), Sound.ENTITY_GENERIC_EAT, 0.9F, 1.05F);
        player.sendMessage("§b" + mob.type.displayName() + " に海の餌をあげました。§f 体力: "
                + (int) Math.ceil(mob.health) + "/" + (int) mob.type.maxHealth());
        return true;
    }

    public boolean mount(Player player, Entity clicked) {
        MarineMob mob = find(clicked);
        if (mob == null || !mob.anchor.isValid() || !mob.type.rideable()) {
            return false;
        }
        if (mob.showControlled) {
            player.sendMessage(mob.type.displayName() + " is currently performing in a show.");
            return true;
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

    public List<MarineMob> nearbyOrcas(World world, Location center, double radius, int limit) {
        if (world == null || center == null || center.getWorld() != world || radius <= 0.0 || limit <= 0) {
            return List.of();
        }
        double radiusSquared = radius * radius;
        return byAnchor.values().stream()
                .filter(this::isUsable)
                .filter(mob -> mob.type == MarineMobType.ORCA)
                .filter(mob -> mob.anchor.getWorld().equals(world))
                .filter(mob -> mob.anchor.getLocation().distanceSquared(center) <= radiusSquared)
                .sorted(Comparator.comparingDouble(mob -> mob.anchor.getLocation().distanceSquared(center)))
                .limit(limit)
                .toList();
    }

    public boolean isUsable(MarineMob mob) {
        return mob != null && mob.anchor.isValid() && !mob.anchor.isDead()
                && byAnchor.containsKey(mob.anchor.getUniqueId());
    }

    public void beginShowControl(MarineMob mob) {
        if (!isUsable(mob) || mob.type != MarineMobType.ORCA) {
            return;
        }
        mob.anchor.eject();
        for (ArmorStand seat : mob.passengerSeats) {
            if (seat.isValid()) {
                seat.eject();
            }
        }
        mob.showControlled = true;
        mob.cachedWaterDirection = null;
        mob.foodTarget = null;
        mob.anchor.setGravity(false);
        mob.anchor.setVelocity(new Vector());
        if (mob.anchor instanceof Horse horse) {
            horse.setAI(false);
        }
    }

    public void endShowControl(MarineMob mob) {
        if (!isUsable(mob)) {
            return;
        }
        mob.showControlled = false;
        mob.targetYaw = mob.anchor.getLocation().getYaw();
        mob.behaviorTicks = randomTicks(80, 170);
        mob.anchor.setGravity(!isWaterContact(mob.anchor.getLocation()));
        refreshNaturalIntent(mob);
        if (mob.anchor instanceof Horse horse) {
            horse.setAI(false);
        }
    }

    public void guideShow(MarineMob mob, Location target, double speed) {
        if (!isUsable(mob) || !mob.showControlled || target == null || target.getWorld() != mob.anchor.getWorld()) {
            return;
        }
        Location current = mob.anchor.getLocation();
        Vector delta = target.toVector().subtract(current.toVector());
        double distance = delta.length();
        if (distance < 0.35) {
            holdShow(mob);
            return;
        }

        Vector direction = delta.normalize();
        double vertical = clamp(direction.getY() * speed, -0.16, 0.16);
        direction.setY(0.0);
        if (direction.lengthSquared() < DIRECTION_EPSILON) {
            direction = forwardFromYaw(current.getYaw());
        } else {
            direction.normalize();
        }

        Vector velocity = direction.multiply(speed).setY(vertical);
        mob.anchor.setGravity(false);
        mob.anchor.setVelocity(velocity);
        mob.anchor.setRotation(yawFromVector(direction), (float) clamp(-vertical * 120.0, -14.0, 14.0));
    }

    public void holdShow(MarineMob mob) {
        if (!isUsable(mob) || !mob.showControlled) {
            return;
        }
        Vector slowed = mob.anchor.getVelocity().multiply(0.28);
        slowed.setY(0.0);
        mob.anchor.setGravity(false);
        mob.anchor.setVelocity(slowed);
    }

    public void launchShowJump(MarineMob mob, Location landing, double horizontalSpeed, double verticalVelocity) {
        if (!isUsable(mob) || !mob.showControlled || landing == null || landing.getWorld() != mob.anchor.getWorld()) {
            return;
        }
        Location current = mob.anchor.getLocation();
        Vector horizontal = landing.toVector().subtract(current.toVector()).setY(0.0);
        if (horizontal.lengthSquared() < DIRECTION_EPSILON) {
            horizontal = forwardFromYaw(current.getYaw());
        } else {
            horizontal.normalize();
        }
        mob.anchor.setGravity(true);
        mob.anchor.setVelocity(horizontal.clone().multiply(horizontalSpeed).setY(verticalVelocity));
        mob.anchor.setRotation(yawFromVector(horizontal), -12.0F);
    }

    public void emitShowBlow(MarineMob mob) {
        if (!isUsable(mob) || mob.type != MarineMobType.ORCA) {
            return;
        }
        Location base = mob.anchor.getLocation();
        World world = base.getWorld();
        Vector forward = forwardFromYaw(base.getYaw());
        Location blowhole = base.clone().add(forward.multiply(2.0)).add(0.0, 2.15, 0.0);
        world.spawnParticle(Particle.CLOUD, blowhole, 28, 0.34, 0.85, 0.34, 0.035);
        world.spawnParticle(Particle.SPLASH, blowhole, 18, 0.36, 0.30, 0.36, 0.07);
        world.playSound(base, Sound.ENTITY_GENERIC_SPLASH, 1.25F, 1.15F);
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

            if (mob.showControlled) {
                if (mob.anchor instanceof Horse horse) {
                    horse.setAI(false);
                }
            } else {
                Player pilot = nativePilot(mob.anchor);
                if (mob.anchor instanceof Horse horse) {
                    horse.setAI(pilot != null);
                }
                if (mob.type.movementStyle() == MarineMobType.MovementStyle.AQUATIC) {
                    if (pilot != null) {
                        moveWithNativeRider(mob, inWater);
                    } else {
                        Location foodTarget = scanFoodTarget(mob);
                        moveAquaticAutonomously(mob, inWater, foodTarget);
                    }
                } else {
                    Location foodTarget = scanFoodTarget(mob);
                    moveCrab(mob, inWater, foodTarget);
                }
            }

            emitWakeAndBreath(mob, inWater);
            updateFollowers(mob);
            if ((mob.ageTicks & 1L) == 0L) {
                updateDisplays(mob);
            }
        }
    }

    private Location scanFoodTarget(MarineMob mob) {
        if (mob.ageTicks < mob.nextFoodScanTick && mob.foodTarget != null) {
            return mob.foodTarget.clone();
        }
        mob.nextFoodScanTick = mob.ageTicks + 4L;
        Location origin = mob.anchor.getLocation();
        World world = origin.getWorld();
        double range = mob.type.foodAttractionRange();
        double bestScore = range * range + 1.0;
        Location best = null;
        Item bestItem = null;

        for (Player player : world.getPlayers()) {
            if (!food.isHeldBy(player)) {
                continue;
            }
            double distance = player.getLocation().distanceSquared(origin);
            if (distance <= range * range && distance < bestScore) {
                bestScore = distance;
                best = player.getLocation().clone();
                bestItem = null;
            }
        }

        Collection<Entity> nearby = world.getNearbyEntities(origin, range, range, range);
        for (Entity entity : nearby) {
            if (!(entity instanceof Item item) || !item.isValid() || !food.isMarineFood(item.getItemStack())) {
                continue;
            }
            double distance = item.getLocation().distanceSquared(origin);
            double score = distance * 0.78;
            if (score < bestScore) {
                bestScore = score;
                best = item.getLocation().clone();
                bestItem = item;
            }
        }

        if (bestItem != null) {
            double bite = mob.type == MarineMobType.CRAB ? 0.70 : mob.type == MarineMobType.SHARK ? 1.20 : 1.55;
            if (bestItem.getLocation().distanceSquared(origin) <= bite * bite && food.consumeOne(bestItem)) {
                Location mouth = origin.clone().add(0.0, mob.type == MarineMobType.CRAB ? 0.30 : 0.75, 0.0);
                world.spawnParticle(Particle.BUBBLE, mouth, mob.type == MarineMobType.CRAB ? 5 : 12,
                        0.28, 0.22, 0.28, 0.04);
                world.playSound(origin, Sound.ENTITY_GENERIC_EAT, 0.75F, mob.type == MarineMobType.ORCA ? 0.82F : 1.08F);
                mob.foodTarget = null;
                mob.behaviorTicks = 12;
                return null;
            }
        }

        mob.foodTarget = best == null ? null : best.clone();
        return best;
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

    private void moveAquaticAutonomously(MarineMob mob, boolean inWater, Location foodTarget) {
        Location location = mob.anchor.getLocation();
        mob.anchor.setGravity(!inWater);

        if (!inWater) {
            returnToWater(mob, location);
            return;
        }

        mob.cachedWaterDirection = null;
        if (foodTarget != null) {
            steerTowardFood(mob, location, foodTarget);
            return;
        }

        mob.behaviorTicks--;
        Vector currentForward = forwardFromYaw(location.getYaw());
        Location ahead = location.clone().add(currentForward.clone().multiply(mob.type == MarineMobType.SHARK ? 2.4 : 2.0));
        boolean waterAhead = isWaterAt(ahead) || isWaterAt(ahead.clone().add(0.0, -0.75, 0.0));
        if (mob.behaviorTicks <= 0) {
            refreshNaturalIntent(mob);
        }
        if (!waterAhead) {
            mob.targetYaw = normalizeYaw((float) (location.getYaw() + ThreadLocalRandom.current().nextDouble(120.0, 235.0)));
            mob.behaviorTicks = randomTicks(45, 90);
        }

        if (mob.type == MarineMobType.ORCA && mob.ageTicks + 45L >= mob.nextBreathTick) {
            mob.verticalIntent = 0.050;
            mob.cruiseFactor = Math.max(mob.cruiseFactor, 0.82);
        } else if (mob.type == MarineMobType.SHARK && isNearSurface(location)) {
            mob.verticalIntent = Math.min(mob.verticalIntent, -0.020);
        }
        if (!isWaterAt(location.clone().add(0.0, -0.85, 0.0))) {
            mob.verticalIntent = Math.max(mob.verticalIntent, 0.030);
        }
        if (!isWaterAt(location.clone().add(0.0, 1.15, 0.0)) && mob.verticalIntent > 0.0
                && mob.type != MarineMobType.ORCA) {
            mob.verticalIntent = -0.012;
        }

        float yaw = turnTowards(location.getYaw(), mob.targetYaw, mob.type.autonomousTurnRate());
        Vector desired = forwardFromYaw(yaw).multiply(mob.type.cruiseSpeed() * mob.cruiseFactor);
        desired.setY(mob.verticalIntent + Math.sin(mob.ageTicks * (mob.type == MarineMobType.ORCA ? 0.035 : 0.025)) * 0.006);
        Vector velocity = blendVelocity(mob.anchor.getVelocity(), desired, mob.type.autonomousAcceleration());
        mob.anchor.setVelocity(velocity);
        mob.anchor.setRotation(yaw, (float) clamp(-velocity.getY() * 135.0, -11.0, 11.0));
    }

    private void refreshNaturalIntent(MarineMob mob) {
        Location location = mob.anchor.getLocation();
        if (mob.type == MarineMobType.ORCA) {
            mob.targetYaw = normalizeYaw((float) (location.getYaw() + ThreadLocalRandom.current().nextDouble(-38.0, 38.0)));
            mob.cruiseFactor = ThreadLocalRandom.current().nextDouble(0.56, 1.08);
            mob.verticalIntent = ThreadLocalRandom.current().nextDouble(-0.020, 0.020);
            mob.behaviorTicks = randomTicks(95, 220);
        } else if (mob.type == MarineMobType.SHARK) {
            mob.targetYaw = normalizeYaw((float) (location.getYaw() + ThreadLocalRandom.current().nextDouble(-24.0, 24.0)));
            mob.cruiseFactor = ThreadLocalRandom.current().nextDouble(0.82, 1.08);
            mob.verticalIntent = ThreadLocalRandom.current().nextDouble(-0.012, 0.012);
            mob.behaviorTicks = randomTicks(130, 270);
        } else {
            mob.targetYaw = normalizeYaw((float) (location.getYaw() + ThreadLocalRandom.current().nextDouble(-30.0, 30.0)));
            mob.cruiseFactor = ThreadLocalRandom.current().nextDouble(0.65, 1.0);
            mob.behaviorTicks = randomTicks(70, 150);
        }
    }

    private void steerTowardFood(MarineMob mob, Location location, Location target) {
        Vector delta = target.toVector().subtract(location.toVector());
        double horizontalDistance = Math.hypot(delta.getX(), delta.getZ());
        if (horizontalDistance < 1.8) {
            Vector coast = mob.anchor.getVelocity().multiply(0.88);
            coast.setY(coast.getY() * 0.60);
            mob.anchor.setVelocity(coast);
            return;
        }

        Vector horizontal = delta.clone().setY(0.0);
        if (horizontal.lengthSquared() < DIRECTION_EPSILON) {
            horizontal = forwardFromYaw(location.getYaw());
        } else {
            horizontal.normalize();
        }
        float targetYaw = yawFromVector(horizontal);
        float yaw = turnTowards(location.getYaw(), targetYaw, mob.type.autonomousTurnRate() * 1.45F);
        double speed = mob.type.cruiseSpeed() * (mob.type == MarineMobType.ORCA ? 1.22 : 1.15);

        boolean targetInWater = isWaterAt(target) || isWaterAt(target.clone().add(0.0, -0.7, 0.0));
        double vertical = targetInWater ? clamp(delta.getY() * 0.08, -0.055, 0.055) : 0.0;
        if (!isWaterAt(location.clone().add(forwardFromYaw(yaw).multiply(1.6)))) {
            speed *= 0.35;
            vertical = Math.min(vertical, 0.0);
        }

        Vector desired = forwardFromYaw(yaw).multiply(speed).setY(vertical);
        Vector velocity = blendVelocity(mob.anchor.getVelocity(), desired, mob.type.autonomousAcceleration() * 1.35);
        mob.anchor.setVelocity(velocity);
        mob.anchor.setRotation(yaw, (float) clamp(-velocity.getY() * 135.0, -12.0, 12.0));
    }

    private void returnToWater(MarineMob mob, Location location) {
        if (mob.ageTicks % 20L == 0L || mob.cachedWaterDirection == null) {
            mob.cachedWaterDirection = findNearbyWaterDirection(location, 7);
        }
        Vector towardWater = mob.cachedWaterDirection;
        if (towardWater != null && towardWater.lengthSquared() > DIRECTION_EPSILON) {
            Vector horizontal = towardWater.clone().setY(0.0).normalize();
            float targetYaw = yawFromVector(horizontal);
            float yaw = turnTowards(location.getYaw(), targetYaw, 3.2F);
            mob.anchor.setRotation(yaw, 0.0F);
            Vector velocity = horizontal.multiply(mob.type.cruiseSpeed() * 0.42);
            velocity.setY(isSolidBelow(location) && mob.ageTicks % 14L == 0L ? 0.11 : -0.08);
            mob.anchor.setVelocity(velocity);
        } else {
            Vector velocity = mob.anchor.getVelocity().multiply(0.42);
            velocity.setY(isSolidBelow(location) ? 0.0 : -0.08);
            mob.anchor.setVelocity(velocity);
        }
    }

    private void moveCrab(MarineMob mob, boolean inWater, Location foodTarget) {
        mob.anchor.setGravity(true);
        Location location = mob.anchor.getLocation();

        if (foodTarget != null) {
            Vector toward = foodTarget.toVector().subtract(location.toVector()).setY(0.0);
            if (toward.lengthSquared() > 0.30) {
                toward.normalize();
                float travelYaw = yawFromVector(toward);
                float bodyTarget = normalizeYaw(travelYaw + (mob.sideDirection > 0 ? -90.0F : 90.0F));
                mob.targetYaw = bodyTarget;
                mob.pauseTicks = 0;
            }
        } else {
            if (mob.pauseTicks > 0) {
                mob.pauseTicks--;
                Vector coast = mob.anchor.getVelocity().multiply(0.35);
                coast.setY(isSolidBelow(location) ? 0.0 : -0.10);
                mob.anchor.setVelocity(coast);
                return;
            }
            mob.behaviorTicks--;
            if (mob.behaviorTicks <= 0) {
                mob.targetYaw = normalizeYaw((float) (location.getYaw()
                        + ThreadLocalRandom.current().nextDouble(-32.0, 32.0)));
                if (ThreadLocalRandom.current().nextDouble() < 0.35) {
                    mob.sideDirection *= -1;
                }
                if (ThreadLocalRandom.current().nextDouble() < 0.30) {
                    mob.pauseTicks = randomTicks(18, 52);
                }
                mob.behaviorTicks = randomTicks(65, 145);
                mob.cruiseFactor = ThreadLocalRandom.current().nextDouble(0.65, 1.0);
            }
        }

        float yaw = turnTowards(location.getYaw(), mob.targetYaw, mob.type.autonomousTurnRate());
        Vector forward = forwardFromYaw(yaw);
        Vector side = new Vector(forward.getZ(), 0.0, -forward.getX()).multiply(mob.sideDirection);
        Location next = location.clone().add(side.clone().multiply(0.50));
        if (next.getBlock().getType().isSolid()) {
            mob.sideDirection *= -1;
            side.multiply(-1.0);
        }

        double speed = mob.type.cruiseSpeed() * mob.cruiseFactor * (inWater ? 0.78 : 1.0);
        if (foodTarget != null) {
            speed *= 1.15;
        }
        Vector desired = side.normalize().multiply(speed);
        desired.setY(inWater ? Math.sin(mob.ageTicks * 0.08) * 0.004 : (isSolidBelow(location) ? 0.0 : -0.10));
        Vector velocity = blendVelocity(mob.anchor.getVelocity(), desired, mob.type.autonomousAcceleration());
        mob.anchor.setVelocity(velocity);
        mob.anchor.setRotation(yaw, 0.0F);
    }

    private static Vector blendVelocity(Vector current, Vector desired, double factor) {
        double t = clamp(factor, 0.01, 1.0);
        return current.clone().multiply(1.0 - t).add(desired.clone().multiply(t));
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
            case CRAB -> 7;
        };
        double spread = mob.type == MarineMobType.ORCA ? 1.85 : mob.type == MarineMobType.SHARK ? 1.0 : 0.20;
        world.spawnParticle(Particle.SPLASH, location, splashCount, spread, mob.type == MarineMobType.CRAB ? 0.18 : 0.60,
                spread, mob.type == MarineMobType.CRAB ? 0.05 : 0.18);
        if (inWater) {
            world.spawnParticle(Particle.BUBBLE, location, Math.max(3, splashCount / 3), spread * 0.7, 0.30, spread * 0.7, 0.06);
        }
        world.playSound(location, Sound.ENTITY_GENERIC_SPLASH,
                mob.type == MarineMobType.ORCA ? 1.55F : mob.type == MarineMobType.SHARK ? 0.9F : 0.32F,
                mob.type == MarineMobType.CRAB ? 1.45F : 0.85F);
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
            Location wake = base.clone().subtract(forward.multiply(mob.type == MarineMobType.ORCA ? 3.8 : mob.type == MarineMobType.SHARK ? 2.2 : 0.4));
            int count = mob.type == MarineMobType.ORCA ? 14 : mob.type == MarineMobType.SHARK ? 7 : 2;
            world.spawnParticle(Particle.SPLASH, wake, count, mob.type == MarineMobType.CRAB ? 0.12 : 0.62,
                    0.20, mob.type == MarineMobType.CRAB ? 0.12 : 0.62, 0.05);
        }

        if (mob.type == MarineMobType.ORCA && isNearSurface(base) && mob.ageTicks >= mob.nextBreathTick) {
            Vector forward = forwardFromYaw(base.getYaw());
            Location blowhole = base.clone().add(forward.multiply(2.0)).add(0.0, 2.15, 0.0);
            world.spawnParticle(Particle.CLOUD, blowhole, 16, 0.25, 0.60, 0.25, 0.025);
            world.spawnParticle(Particle.SPLASH, blowhole, 9, 0.30, 0.25, 0.30, 0.05);
            mob.nextBreathTick = mob.ageTicks + randomTicks(180, 320);
            if (!mob.showControlled) {
                mob.verticalIntent = -0.018;
                mob.behaviorTicks = Math.min(mob.behaviorTicks, 55);
            }
        }
    }

    private void updateFollowers(MarineMob mob) {
        Location base = mob.anchor.getLocation();
        mob.interaction.teleport(base.clone().add(0.0, mob.type == MarineMobType.CRAB ? 0.02 : 0.15, 0.0));
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
        double motionScale = clamp(horizontalSpeed / Math.max(0.06, mob.type.cruiseSpeed()), 0.12, 1.42);
        List<MarineMobType.ModelPart> parts = mob.type.parts();

        for (int index = 0; index < parts.size(); index++) {
            MarineMobType.ModelPart part = parts.get(index);
            BlockDisplay display = mob.displays.get(index);
            double wave = animationWave(mob.ageTicks, part) * motionScale;
            double forward = part.forward();
            double up = part.up();
            double right = part.right();

            switch (part.animation()) {
                case SHARK_BODY -> right += wave * 0.10;
                case SHARK_TAIL -> right += wave * 0.22;
                case ORCA_PEDUNCLE -> up += wave * 0.07;
                case ORCA_FLUKE -> up += wave * 0.14;
                case CRAB_LEG_A, CRAB_LEG_B -> up += Math.abs(wave) * 0.025;
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
            case SHARK_BODY -> Math.sin(ageTicks * 0.34 + part.phase());
            case SHARK_TAIL -> Math.sin(ageTicks * 0.42 + part.phase());
            case ORCA_PEDUNCLE -> Math.sin(ageTicks * 0.27 + part.phase());
            case ORCA_FLUKE -> Math.sin(ageTicks * 0.30 + part.phase());
            case CRAB_LEG_A -> Math.sin(ageTicks * 0.50 + part.phase());
            case CRAB_LEG_B -> -Math.sin(ageTicks * 0.50 + part.phase());
            case STATIC -> 0.0;
        };
    }

    private static Transformation transformation(MarineMobType.ModelPart part, double wave) {
        float dynamicDegrees = switch (part.animation()) {
            case SHARK_BODY -> (float) (wave * 8.0);
            case SHARK_TAIL -> (float) (wave * 28.0);
            case ORCA_PEDUNCLE -> (float) (wave * 7.0);
            case ORCA_FLUKE -> (float) (wave * 20.0);
            case CRAB_LEG_A, CRAB_LEG_B -> (float) (wave * 15.0);
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
        private int behaviorTicks;
        private int sideDirection;
        private long nextBreathTick;
        private boolean wasInWater;
        private boolean showControlled;
        private Vector cachedWaterDirection;
        private double cruiseFactor = 1.0;
        private double verticalIntent;
        private int pauseTicks;
        private long nextFoodScanTick;
        private Location foodTarget;

        private MarineMob(MarineMobType type, LivingEntity anchor, Interaction interaction,
                          List<BlockDisplay> displays, List<ArmorStand> passengerSeats,
                          double health, float targetYaw, int behaviorTicks,
                          int sideDirection, long nextBreathTick) {
            this.type = type;
            this.anchor = anchor;
            this.interaction = interaction;
            this.displays = List.copyOf(displays);
            this.passengerSeats = List.copyOf(passengerSeats);
            this.health = health;
            this.targetYaw = targetYaw;
            this.behaviorTicks = behaviorTicks;
            this.sideDirection = sideDirection;
            this.nextBreathTick = nextBreathTick;
        }

        public MarineMobType type() { return type; }
        public double health() { return health; }
        public int seatCount() { return type.seats().size(); }
        public UUID id() { return anchor.getUniqueId(); }
        public boolean showControlled() { return showControlled; }
        public Location location() { return anchor.getLocation().clone(); }
    }
}
