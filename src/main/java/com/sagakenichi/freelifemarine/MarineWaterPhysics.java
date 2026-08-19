package com.sagakenichi.freelifemarine;

/**
 * Small, explicit movement constants for gravity-enabled marine motion.
 *
 * The plugin keeps Minecraft gravity enabled for living movement carriers. While an animal
 * actively swims, it contributes a small upward swimming force rather than disabling gravity.
 */
final class MarineWaterPhysics {

    static final double SWIM_LIFT_PER_TICK = 0.035;
    static final double SHALLOW_DIVE_VERTICAL = -0.020;
    static final double DEEP_DIVE_VERTICAL = -0.095;
    static final double CHARGE_VERTICAL = 0.180;
    static final int SHALLOW_DIVE_TICKS = 4;
    static final int SHALLOW_CHARGE_TICKS = 32;
    static final int DEEP_CHARGE_TICKS = 48;

    private MarineWaterPhysics() {
    }

    static boolean supportsTwoBlockPool(boolean currentLayerWater, boolean oneBlockBelowWater) {
        return currentLayerWater && oneBlockBelowWater;
    }

    static boolean isShallowTwoBlockPool(boolean currentLayerWater,
                                         boolean oneBlockBelowWater,
                                         boolean twoBlocksBelowWater) {
        return supportsTwoBlockPool(currentLayerWater, oneBlockBelowWater) && !twoBlocksBelowWater;
    }

    static int diveTicks(boolean shallow, MarineMobType type) {
        if (shallow) {
            return SHALLOW_DIVE_TICKS;
        }
        return type == MarineMobType.ORCA ? 24 : 18;
    }

    static int chargeTicks(boolean shallow) {
        return shallow ? SHALLOW_CHARGE_TICKS : DEEP_CHARGE_TICKS;
    }

    static double diveVertical(boolean shallow) {
        return shallow ? SHALLOW_DIVE_VERTICAL : DEEP_DIVE_VERTICAL;
    }

    static double swimmingVertical(double intendedVertical) {
        return intendedVertical + SWIM_LIFT_PER_TICK;
    }
}
