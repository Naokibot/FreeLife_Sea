package com.sagakenichi.freelifemarine;

/**
 * Species-specific pacing and roaming ranges for autonomous marine animals.
 * Long-lived roaming targets keep movement exploratory instead of turning in a local circle.
 */
final class MarineNaturalMotionProfile {

    private MarineNaturalMotionProfile() {
    }

    static int continuousCruiseLevel(MarineMobType type) {
        return switch (type) {
            case ORCA -> 5;
            case SHARK -> 4;
            case CRAB -> 1;
        };
    }

    static double minPace(MarineMobType type) {
        return switch (type) {
            case ORCA -> 0.88;
            case SHARK -> 0.90;
            case CRAB -> 1.0;
        };
    }

    static double maxPace(MarineMobType type) {
        return switch (type) {
            case ORCA -> 1.06;
            case SHARK -> 1.05;
            case CRAB -> 1.0;
        };
    }

    static int minPaceHoldTicks(MarineMobType type) {
        return switch (type) {
            case ORCA -> 70;
            case SHARK -> 90;
            case CRAB -> Integer.MAX_VALUE;
        };
    }

    static int maxPaceHoldTicksExclusive(MarineMobType type) {
        return switch (type) {
            case ORCA -> 181;
            case SHARK -> 221;
            case CRAB -> Integer.MAX_VALUE;
        };
    }

    static double pacePulse(MarineMobType type, long tick, double phase) {
        double amplitude = switch (type) {
            case ORCA -> 0.045;
            case SHARK -> 0.035;
            case CRAB -> 0.0;
        };
        double frequency = switch (type) {
            case ORCA -> 0.020;
            case SHARK -> 0.017;
            case CRAB -> 0.0;
        };
        return 1.0 + Math.sin(tick * frequency + phase) * amplitude;
    }

    static double verticalWave(MarineMobType type, long tick, double phase) {
        double amplitude = switch (type) {
            case ORCA -> 0.014;
            case SHARK -> 0.010;
            case CRAB -> 0.0;
        };
        double frequency = switch (type) {
            case ORCA -> 0.031;
            case SHARK -> 0.026;
            case CRAB -> 0.0;
        };
        return Math.sin(tick * frequency + phase * 1.17) * amplitude;
    }

    static double minRoamDistance(MarineMobType type) {
        return switch (type) {
            case ORCA -> 12.0;
            case SHARK -> 9.0;
            case CRAB -> 0.0;
        };
    }

    static double maxRoamDistance(MarineMobType type) {
        return switch (type) {
            case ORCA -> 30.0;
            case SHARK -> 24.0;
            case CRAB -> 0.0;
        };
    }

    static double maxRoamDepthChange(MarineMobType type) {
        return switch (type) {
            case ORCA -> 3.0;
            case SHARK -> 2.2;
            case CRAB -> 0.0;
        };
    }

    static int minRoamTargetTicks(MarineMobType type) {
        return switch (type) {
            case ORCA -> 100;
            case SHARK -> 120;
            case CRAB -> Integer.MAX_VALUE;
        };
    }

    static int maxRoamTargetTicksExclusive(MarineMobType type) {
        return switch (type) {
            case ORCA -> 261;
            case SHARK -> 301;
            case CRAB -> Integer.MAX_VALUE;
        };
    }

    static double collisionScanRadius(MarineMobType type) {
        return switch (type) {
            case ORCA -> 6.5;
            case SHARK -> 5.0;
            case CRAB -> 0.0;
        };
    }

    static boolean breaksBoats(MarineMobType type) {
        return type == MarineMobType.ORCA || type == MarineMobType.SHARK;
    }
}
