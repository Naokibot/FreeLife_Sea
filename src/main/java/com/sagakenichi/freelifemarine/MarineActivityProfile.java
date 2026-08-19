package com.sagakenichi.freelifemarine;

/**
 * Species-specific autonomous activity ranges.  The values are deliberately
 * separate from the ten speed levels so natural roaming can change pace often
 * without spending every tick at top speed.
 */
final class MarineActivityProfile {

    private MarineActivityProfile() {
    }

    static int minRoamLevel(MarineMobType type) {
        return switch (type) {
            case ORCA -> 4;
            case SHARK -> 4;
            case CRAB -> 1;
        };
    }

    static int maxRoamLevel(MarineMobType type) {
        return switch (type) {
            case ORCA -> 7;
            case SHARK -> 6;
            case CRAB -> 1;
        };
    }

    static int burstLevel(MarineMobType type) {
        return switch (type) {
            case ORCA -> 8;
            case SHARK -> 7;
            case CRAB -> 1;
        };
    }

    static double burstChance(MarineMobType type) {
        return switch (type) {
            case ORCA -> 0.25;
            case SHARK -> 0.15;
            case CRAB -> 0.0;
        };
    }

    static int minBehaviorTicks(MarineMobType type) {
        return switch (type) {
            case ORCA -> 50;
            case SHARK -> 70;
            case CRAB -> 55;
        };
    }

    static int maxBehaviorTicksExclusive(MarineMobType type) {
        return switch (type) {
            case ORCA -> 121;
            case SHARK -> 151;
            case CRAB -> 126;
        };
    }

    static double maxYawChange(MarineMobType type) {
        return switch (type) {
            case ORCA -> 55.0;
            case SHARK -> 38.0;
            case CRAB -> 36.0;
        };
    }

    static double verticalIntentRange(MarineMobType type) {
        return switch (type) {
            case ORCA -> 0.035;
            case SHARK -> 0.020;
            case CRAB -> 0.0;
        };
    }

    static int minJumpDelayTicks(MarineMobType type) {
        return switch (type) {
            case ORCA -> 260;
            case SHARK -> 480;
            case CRAB -> Integer.MAX_VALUE;
        };
    }

    static int maxJumpDelayTicksExclusive(MarineMobType type) {
        return switch (type) {
            case ORCA -> 651;
            case SHARK -> 1201;
            case CRAB -> Integer.MAX_VALUE;
        };
    }
}
