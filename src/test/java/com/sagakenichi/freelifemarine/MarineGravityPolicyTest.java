package com.sagakenichi.freelifemarine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MarineGravityPolicyTest {

    @Test
    void airborneAnimalsAreReleasedToMinecraftGravity() {
        assertTrue(MarineGravityPolicy.useNativeAirGravity(false, false));
        assertFalse(MarineGravityPolicy.useNativeAirGravity(false, true));
        assertFalse(MarineGravityPolicy.useNativeAirGravity(true, false));
    }

    @Test
    void scriptedSwimMotionIsLimitedToWater() {
        assertTrue(MarineGravityPolicy.allowScriptedAquaticMotion(true));
        assertFalse(MarineGravityPolicy.allowScriptedAquaticMotion(false));
    }
}
