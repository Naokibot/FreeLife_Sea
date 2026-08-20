package com.sagakenichi.freelifemarine;

/**
 * Small, species-specific variations layered on top of the main autonomous controller.
 * The normal AI still decides where the animal wants to go; this profile keeps that
 * movement continuous while adding slow pace, heading, and depth changes so motion does
 * not look like a vehicle holding one exact throttle setting.
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

    static double headingWeaveDegrees(MarineMobType type, long tick, double phase) {
        double amplitude = switch (type) {
            case ORCA -> 4.5;
            case SHARK -> 3.2;
            case CRAB -> 0.0;
        };
        double frequency = switch (type) {
            case ORCA -> 0.025;
            case SHARK -> 0.021;
            case CRAB -> 0.0;
        };
        return Math.sin(tick * frequency + phase * 0.73) * amplitude;
    }

    static double verticalWave(MarineMobType type, long tick, double phase) {
        double amplitude = switch (type) {
            case ORCA -> 0.018;
            case SHARK -> 0.012;
            case CRAB -> 0.0;
        };
        double frequency = switch (type) {
            case ORCA -> 0.031;
            case SHARK -> 0.026;
            case CRAB -> 0.0;
        };
        return Math.sin(tick * frequency + phase * 1.17) * amplitude;
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
