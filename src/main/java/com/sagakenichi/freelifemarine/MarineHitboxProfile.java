package com.sagakenichi.freelifemarine;

import java.util.List;

/**
 * Segmented interaction hitboxes that approximate the visible animal body.
 * Interaction entities are used for attack/right-click targeting; physical pushing
 * remains disabled on the invisible movement carrier.
 */
final class MarineHitboxProfile {

    private MarineHitboxProfile() {
    }

    static List<Hitbox> forType(MarineMobType type) {
        return switch (type) {
            case ORCA -> List.of(
                    new Hitbox(3.85, 0.20, 0.0, 2.00F, 1.70F),
                    new Hitbox(2.20, 0.18, 0.0, 2.65F, 2.05F),
                    new Hitbox(0.35, 0.18, 0.0, 3.05F, 2.25F),
                    new Hitbox(-1.55, 0.14, 0.0, 2.65F, 2.00F),
                    new Hitbox(-3.05, 0.08, 0.0, 1.85F, 1.50F),
                    new Hitbox(-4.20, 0.02, 0.0, 1.05F, 1.00F)
            );
            case SHARK -> List.of(
                    new Hitbox(2.95, 0.12, 0.0, 1.45F, 1.10F),
                    new Hitbox(1.35, 0.12, 0.0, 1.95F, 1.38F),
                    new Hitbox(-0.45, 0.10, 0.0, 2.00F, 1.38F),
                    new Hitbox(-2.05, 0.05, 0.0, 1.48F, 1.05F),
                    new Hitbox(-3.45, 0.02, 0.0, 0.85F, 0.78F)
            );
            case CRAB -> List.of(
                    new Hitbox(0.0, 0.26, 0.0, 1.30F, 0.64F),
                    new Hitbox(0.30, 0.20, 0.62, 0.58F, 0.48F),
                    new Hitbox(0.30, 0.20, -0.62, 0.58F, 0.48F)
            );
        };
    }

    record Hitbox(double forward, double up, double right, float width, float height) {
        Hitbox {
            if (width <= 0.0F || height <= 0.0F) {
                throw new IllegalArgumentException("Hitbox dimensions must be positive");
            }
        }
    }
}
