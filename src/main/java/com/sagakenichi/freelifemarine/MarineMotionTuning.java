package com.sagakenichi.freelifemarine;

final class MarineMotionTuning {

    static final double PREVIOUS_ORCA_RIDDEN_BLOCKS_PER_SECOND = 14.0;
    static final double ORCA_RIDDEN_SPEED_MULTIPLIER = 4.0;
    static final double ORCA_RIDDEN_BLOCKS_PER_SECOND =
            PREVIOUS_ORCA_RIDDEN_BLOCKS_PER_SECOND * ORCA_RIDDEN_SPEED_MULTIPLIER;
    static final double ORCA_RIDDEN_BLOCKS_PER_TICK = ORCA_RIDDEN_BLOCKS_PER_SECOND / 20.0;

    private static final double STALL_MINIMUM_DESCENT = -0.025;
    private static final double STALL_MAX_VERTICAL_VELOCITY = 0.15;
    private static final double FALL_KICK = 0.18;

    private MarineMotionTuning() {
    }

    static boolean isUnsupportedAir(boolean inWater, boolean onGround) {
        return !inWater && !onGround;
    }

    static boolean needsFallKick(double previousY, double currentY, double verticalVelocity) {
        if (!Double.isFinite(previousY)) {
            return false;
        }
        double deltaY = currentY - previousY;
        return deltaY > STALL_MINIMUM_DESCENT && verticalVelocity <= STALL_MAX_VERTICAL_VELOCITY;
    }

    static double fallKickVelocity(double currentVerticalVelocity) {
        return Math.min(-FALL_KICK, currentVerticalVelocity - FALL_KICK);
    }
}
