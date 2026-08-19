package com.sagakenichi.freelifemarine;

/**
 * Distance-aware pursuit tuning for marine food.
 * Far targets use fast speed tiers; the final approach intentionally slows down
 * so the animal reaches the food instead of repeatedly overshooting it.
 */
final class MarinePursuitProfile {

    private MarinePursuitProfile() {
    }

    static MarineSpeedLevel speedLevel(MarineMobType type, double horizontalDistance) {
        return switch (type) {
            case ORCA -> horizontalDistance >= 10.0 ? MarineSpeedLevel.LEVEL_9
                    : horizontalDistance >= 4.0 ? MarineSpeedLevel.LEVEL_8
                    : MarineSpeedLevel.LEVEL_6;
            case SHARK -> horizontalDistance >= 8.0 ? MarineSpeedLevel.LEVEL_8
                    : horizontalDistance >= 3.0 ? MarineSpeedLevel.LEVEL_7
                    : MarineSpeedLevel.LEVEL_5;
            case CRAB -> MarineSpeedLevel.LEVEL_1;
        };
    }

    static double accelerationMultiplier(MarineMobType type) {
        return switch (type) {
            case ORCA -> 2.35;
            case SHARK -> 2.10;
            case CRAB -> 1.35;
        };
    }

    static long foodScanIntervalTicks() {
        return 2L;
    }
}
