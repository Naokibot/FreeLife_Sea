package com.sagakenichi.freelifemarine;

/**
 * Autonomous breach targets measured from the water surface. Initial vertical
 * velocities are tuned for the deterministic airborne integrator introduced in 1.9.1.
 */
final class MarineJumpProfile {

    private MarineJumpProfile() {
    }

    static int minHeightBlocks(MarineMobType type) {
        return switch (type) {
            case ORCA, SHARK -> 3;
            case CRAB -> 0;
        };
    }

    static int maxHeightExclusive(MarineMobType type) {
        return switch (type) {
            case ORCA -> 14; // 3..13 blocks
            case SHARK -> 8; // 3..7 blocks
            case CRAB -> 1;
        };
    }

    static int clearanceBlocks(MarineMobType type) {
        return switch (type) {
            case ORCA -> 14;
            case SHARK -> 8;
            case CRAB -> 0;
        };
    }

    static int speedLevelForHeight(MarineMobType type, int heightBlocks) {
        if (type == MarineMobType.ORCA) {
            if (heightBlocks >= 10) {
                return 10;
            }
            return heightBlocks >= 6 ? 9 : 8;
        }
        if (type == MarineMobType.SHARK) {
            return heightBlocks >= 6 ? 8 : 7;
        }
        return 1;
    }

    static double initialVerticalVelocity(int heightBlocks) {
        return switch (Math.max(3, Math.min(13, heightBlocks))) {
            case 3 -> 0.779;
            case 4 -> 0.900;
            case 5 -> 1.008;
            case 6 -> 1.108;
            case 7 -> 1.201;
            case 8 -> 1.289;
            case 9 -> 1.372;
            case 10 -> 1.451;
            case 11 -> 1.527;
            case 12 -> 1.601;
            case 13 -> 1.671;
            default -> throw new IllegalStateException("unreachable");
        };
    }
}
