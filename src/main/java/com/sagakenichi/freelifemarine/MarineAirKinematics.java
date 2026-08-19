package com.sagakenichi.freelifemarine;

/**
 * Deterministic airborne kinematics used when the carrier entity cannot be trusted
 * to apply vanilla gravity consistently on a live server.
 */
final class MarineAirKinematics {

    static final double GRAVITY_PER_TICK = 0.08;
    static final double VERTICAL_DRAG = 0.98;
    static final double HORIZONTAL_DRAG = 0.98;
    static final double TERMINAL_FALL_SPEED = -3.92;
    static final double MAX_SWEEP_STEP = 0.20;

    private MarineAirKinematics() {
    }

    static double nextVerticalVelocity(double current) {
        double next = (current - GRAVITY_PER_TICK) * VERTICAL_DRAG;
        return Math.max(TERMINAL_FALL_SPEED, next);
    }

    static double nextHorizontalVelocity(double current) {
        return current * HORIZONTAL_DRAG;
    }

    static int sweepSteps(double dx, double dy, double dz) {
        double largest = Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
        return Math.max(1, (int) Math.ceil(largest / MAX_SWEEP_STEP));
    }
}
