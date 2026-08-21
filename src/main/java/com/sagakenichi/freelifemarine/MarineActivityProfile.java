package com.sagakenichi.freelifemarine;

/**
 * Species-specific autonomous activity ranges. Orcas no longer start autonomous
 * breach jumps; jumping is reserved for rider-driven surface breaches and scripted
 * show/call behavior.
 */
final class MarineActivityProfile {

    private MarineActivityProfile() {
    }

    static int minRoamLevel(MarineMobType type) {
        return switch (type) {
            case ORCA -> 7;
            case SHARK -> 6;
            case CRAB -> 1;
        };
    }

    static int maxRoamLevel(MarineMobType type) {
        return switch (type) {
            case ORCA -> 9;
            case SHARK -> 8;
            case CRAB -> 1;
        };
    }

    static int burstLevel(MarineMobType type) {
        return switch (type) {
            case ORCA -> 10;
            case SHARK -> 9;
            case CRAB -> 1;
        };
    }

    static double burstChance(MarineMobType type) {
        return switch (type) {
            case ORCA -> 0.55;
            case SHARK -> 0.35;
            case CRAB -> 0.0;
        };
    }

    static int minBehaviorTicks(MarineMobType type) {
        return switch (type) {
            case ORCA -> 20;
            case SHARK -> 30;
            case CRAB -> 55;
        };
    }

    static int maxBehaviorTicksExclusive(MarineMobType type) {
        return switch (type) {
            case ORCA -> 56;
            case SHARK -> 76;
            case CRAB -> 126;
        };
    }

    static double maxYawChange(MarineMobType type) {
        return switch (type) {
            case ORCA -> 70.0;
            case SHARK -> 50.0;
            case CRAB -> 36.0;
        };
    }

    static double verticalIntentRange(MarineMobType type) {
        return switch (type) {
            case ORCA -> 0.060;
            case SHARK -> 0.035;
            case CRAB -> 0.0;
        };
    }

    static double accelerationMultiplier(MarineMobType type) {
        return switch (type) {
            case ORCA -> 2.40;
            case SHARK -> 2.00;
            case CRAB -> 1.0;
        };
    }

    static int minJumpDelayTicks(MarineMobType type) {
        return switch (type) {
            // Effectively disabled without using Integer.MAX_VALUE, because the legacy
            // autonomous scheduler calls nextInt(min, maxExclusive).
            case ORCA -> 2_000_000_000;
            case SHARK -> 200;
            case CRAB -> Integer.MAX_VALUE;
        };
    }

    static int maxJumpDelayTicksExclusive(MarineMobType type) {
        return switch (type) {
            case ORCA -> 2_000_000_001;
            case SHARK -> 601;
            case CRAB -> Integer.MAX_VALUE;
        };
    }
}
