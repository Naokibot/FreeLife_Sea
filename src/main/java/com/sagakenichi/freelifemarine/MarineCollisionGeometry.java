package com.sagakenichi.freelifemarine;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

/**
 * Conservative physical envelope used by the motion controllers.
 * The visible marine models are much larger than their invisible carrier entities, so
 * checking only the carrier would allow the head and body to enter walls.
 */
final class MarineCollisionGeometry {

    private static final double ORCA_EXTRA_WALL_MARGIN = 0.38;
    private static final double SHARK_EXTRA_WALL_MARGIN = 0.18;

    private MarineCollisionGeometry() {
    }

    static boolean bodyCollides(Location anchor, MarineMobType type) {
        if (type == MarineMobType.CRAB) {
            return false;
        }
        double margin = wallMargin(type);
        for (MarineHitboxProfile.Hitbox hitbox : MarineHitboxProfile.forType(type)) {
            if (segmentCollides(anchor, hitbox, margin)) {
                return true;
            }
        }
        return false;
    }

    static int bodyCollisionScore(Location anchor, MarineMobType type) {
        if (type == MarineMobType.CRAB) {
            return 0;
        }
        int score = 0;
        double margin = wallMargin(type);
        for (MarineHitboxProfile.Hitbox hitbox : MarineHitboxProfile.forType(type)) {
            if (segmentCollides(anchor, hitbox, margin)) {
                score++;
            }
        }
        return score;
    }

    private static boolean segmentCollides(Location anchor, MarineHitboxProfile.Hitbox hitbox,
                                           double margin) {
        Location center = relative(anchor, hitbox.forward(), hitbox.up(), hitbox.right());
        double radius = hitbox.width() * 0.5 + margin;
        double corner = radius * 0.70;
        double lowY = Math.max(0.12, hitbox.height() * 0.25);
        double midY = Math.max(0.20, hitbox.height() * 0.55);
        double highY = Math.max(0.28, hitbox.height() * 0.88);

        double[][] horizontal = {
                {0.0, 0.0},
                {radius, 0.0}, {-radius, 0.0},
                {0.0, radius}, {0.0, -radius},
                {corner, corner}, {corner, -corner},
                {-corner, corner}, {-corner, -corner}
        };
        double[] heights = {lowY, midY, highY};

        for (double y : heights) {
            for (double[] offset : horizontal) {
                if (solidCollision(center.clone().add(offset[0], y, offset[1]))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static double wallMargin(MarineMobType type) {
        return switch (type) {
            case ORCA -> ORCA_EXTRA_WALL_MARGIN;
            case SHARK -> SHARK_EXTRA_WALL_MARGIN;
            case CRAB -> 0.0;
        };
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

    private static boolean solidCollision(Location location) {
        Block block = location.getBlock();
        return block.getType().isSolid() && !block.isPassable();
    }
}
