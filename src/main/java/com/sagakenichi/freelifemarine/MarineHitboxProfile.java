package com.sagakenichi.freelifemarine;

import java.util.List;

/**
 * Overlapping segmented interaction hitboxes that approximate the visible animal body.
 * Interaction entities are used for attack/right-click targeting; physical pushing
 * remains disabled on the invisible movement carrier.
 */
final class MarineHitboxProfile {

    private MarineHitboxProfile() {
    }

    static List<Hitbox> forType(MarineMobType type) {
        return switch (type) {
            case ORCA -> List.of(
                    new Hitbox(4.25, 0.22, 0.0, 1.45F, 1.45F),
                    new Hitbox(3.25, 0.21, 0.0, 1.85F, 1.78F),
                    new Hitbox(2.15, 0.20, 0.0, 2.25F, 2.02F),
                    new Hitbox(0.95, 0.19, 0.0, 2.55F, 2.20F),
                    new Hitbox(-0.30, 0.18, 0.0, 2.70F, 2.24F),
                    new Hitbox(-1.55, 0.15, 0.0, 2.45F, 2.05F),
                    new Hitbox(-2.65, 0.11, 0.0, 2.05F, 1.72F),
                    new Hitbox(-3.55, 0.07, 0.0, 1.55F, 1.38F),
                    new Hitbox(-4.30, 0.03, 0.0, 1.10F, 1.02F),
                    new Hitbox(-4.95, 0.00, 0.0, 0.90F, 0.82F)
            );
            case SHARK -> List.of(
                    new Hitbox(3.15, 0.14, 0.0, 1.20F, 0.98F),
                    new Hitbox(2.25, 0.13, 0.0, 1.55F, 1.18F),
                    new Hitbox(1.20, 0.12, 0.0, 1.90F, 1.35F),
                    new Hitbox(0.05, 0.11, 0.0, 2.05F, 1.42F),
                    new Hitbox(-1.10, 0.09, 0.0, 1.90F, 1.32F),
                    new Hitbox(-2.10, 0.06, 0.0, 1.55F, 1.12F),
                    new Hitbox(-3.00, 0.03, 0.0, 1.15F, 0.90F),
                    new Hitbox(-3.70, 0.00, 0.0, 0.82F, 0.72F)
            );
            case CRAB -> List.of(
                    new Hitbox(-0.18, 0.26, 0.0, 1.20F, 0.62F),
                    new Hitbox(0.35, 0.25, 0.0, 0.95F, 0.58F),
                    new Hitbox(0.42, 0.20, 0.58, 0.62F, 0.50F),
                    new Hitbox(0.42, 0.20, -0.58, 0.62F, 0.50F),
                    new Hitbox(-0.15, 0.16, 0.0, 1.36F, 0.42F)
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
