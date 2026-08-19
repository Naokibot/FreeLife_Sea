package com.sagakenichi.freelifemarine;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
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

public final class MarineMobService {

    private static final double MAX_HEALTH = 10.0;
    private static final double DIRECTION_EPSILON = 1.0E-6;

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
            double yaw = Math.toRadians(origin.getYaw());
            horizontal = new Vector(-Math.sin(yaw), 0.0, Math.cos(yaw));
        }
        horizontal.normalize().multiply(3.0);

        Location spawn = origin.clone().add(horizontal).add(0.0, 0.4, 0.0);
        World world = spawn.getWorld();
        if (world == null) {
            throw new IllegalStateException("Player world is unavailable");
        }

        Slime anchor = world.spawn(spawn, Slime.class, slime -> {
            slime.setSize(1);
            slime.setAI(false);
            slime.setInvisible(true);
            slime.setSilent(true);
            slime.setGravity(false);
            slime.setCollidable(false);
            slime.setPersistent(false);
            slime.setRemoveWhenFarAway(false);
        });

        Interaction interaction = world.spawn(spawn, Interaction.class, hitbox -> {
            hitbox.setInteractionWidth(type.interactionWidth());
            hitbox.setInteractionHeight(type.interactionHeight());
            hitbox.setResponsive(true);
            hitbox.setPersistent(false);
        });

        List<BlockDisplay> displays = new ArrayList<>();
        for (MarineMobType.ModelPart part : type.parts()) {
            BlockDisplay display = world.spawn(spawn, BlockDisplay.class, entity -> {
                entity.setBlock(Bukkit.createBlockData(part.material()));
                entity.setGravity(false);
                entity.setPersistent(false);
                entity.setShadowRadius(0.0F);
                entity.setShadowStrength(0.0F);
                entity.setTransformation(new Transformation(
                        new Vector3f(-part.scaleX() / 2.0F, -part.scaleY() / 2.0F, -part.scaleZ() / 2.0F),
                        new AxisAngle4f(),
                        new Vector3f(part.scaleX(), part.scaleY(), part.scaleZ()),
                        new AxisAngle4f()
                ));
            });
            displays.add(display);
        }

        MarineMob mob = new MarineMob(type, anchor, interaction, displays, MAX_HEALTH);
        byAnchor.put(anchor.getUniqueId(), mob);
        byEntity.put(anchor.getUniqueId(), mob);
        byEntity.put(interaction.getUniqueId(), mob);
        updateVisuals(mob);
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
        if (mob == null || !mob.anchor.isValid()) {
            return false;
        }
        if (player.isInsideVehicle() || !mob.anchor.getPassengers().isEmpty()) {
            return true;
        }
        mob.anchor.addPassenger(player);
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

            Player rider = firstPlayerPassenger(mob.anchor);
            if (rider != null) {
                Vector direction = rider.getLocation().getDirection();
                if (direction.lengthSquared() > DIRECTION_EPSILON) {
                    direction.normalize().multiply(mob.type.rideSpeed());
                    mob.anchor.setVelocity(direction);
                    mob.anchor.setRotation(rider.getLocation().getYaw(), rider.getLocation().getPitch() * 0.25F);
                }
            } else {
                mob.anchor.setVelocity(mob.anchor.getVelocity().multiply(0.65));
            }
            updateVisuals(mob);
        }
    }

    private void updateVisuals(MarineMob mob) {
        Location base = mob.anchor.getLocation();
        float yaw = base.getYaw();
        double radians = Math.toRadians(yaw);
        Vector forward = new Vector(-Math.sin(radians), 0.0, Math.cos(radians));
        Vector right = new Vector(forward.getZ(), 0.0, -forward.getX());

        mob.interaction.teleport(base.clone().add(0.0, 0.25, 0.0));
        List<MarineMobType.ModelPart> parts = mob.type.parts();
        for (int index = 0; index < parts.size(); index++) {
            MarineMobType.ModelPart part = parts.get(index);
            BlockDisplay display = mob.displays.get(index);
            Location target = base.clone()
                    .add(forward.clone().multiply(part.forward()))
                    .add(right.clone().multiply(part.right()))
                    .add(0.0, part.up(), 0.0);
            display.teleport(target);
            display.setRotation(yaw, 0.0F);
        }
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
        if (mob.interaction.isValid()) {
            mob.interaction.remove();
        }
        if (mob.anchor.isValid()) {
            mob.anchor.eject();
            mob.anchor.remove();
        }
    }

    private static Player firstPlayerPassenger(Slime anchor) {
        for (Entity passenger : anchor.getPassengers()) {
            if (passenger instanceof Player player) {
                return player;
            }
        }
        return null;
    }

    public static final class MarineMob {
        private final MarineMobType type;
        private final Slime anchor;
        private final Interaction interaction;
        private final List<BlockDisplay> displays;
        private double health;

        private MarineMob(
                MarineMobType type,
                Slime anchor,
                Interaction interaction,
                List<BlockDisplay> displays,
                double health
        ) {
            this.type = type;
            this.anchor = anchor;
            this.interaction = interaction;
            this.displays = List.copyOf(displays);
            this.health = health;
        }

        public MarineMobType type() {
            return type;
        }

        public double health() {
            return health;
        }
    }
}
