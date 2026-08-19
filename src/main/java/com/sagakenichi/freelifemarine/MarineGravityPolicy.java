package com.sagakenichi.freelifemarine;

/**
 * Keeps scripted swim movement from cancelling Minecraft gravity while a marine mob is airborne.
 */
final class MarineGravityPolicy {

    private MarineGravityPolicy() {
    }

    static boolean useNativeAirGravity(boolean inWater, boolean onGround) {
        return !inWater && !onGround;
    }

    static boolean allowScriptedAquaticMotion(boolean inWater) {
        return inWater;
    }
}
