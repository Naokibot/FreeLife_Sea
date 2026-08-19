package com.sagakenichi.freelifemarine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MarineWaterPhysicsTest {

    @Test
    void twoWaterLayersAreEnoughForJumpPreparation() {
        assertTrue(MarineWaterPhysics.supportsTwoBlockPool(true, true));
        assertTrue(MarineWaterPhysics.isShallowTwoBlockPool(true, true, false));
        assertFalse(MarineWaterPhysics.supportsTwoBlockPool(true, false));
    }

    @Test
    void shallowDiveIsShortAndSmall() {
        assertEquals(4, MarineWaterPhysics.diveTicks(true, MarineMobType.ORCA));
        assertEquals(-0.020, MarineWaterPhysics.diveVertical(true), 1.0E-9);
        assertEquals(32, MarineWaterPhysics.chargeTicks(true));
    }

    @Test
    void swimmingLiftDoesNotDisableGravity() {
        assertEquals(0.035, MarineWaterPhysics.swimmingVertical(0.0), 1.0E-9);
        assertEquals(0.055, MarineWaterPhysics.swimmingVertical(0.020), 1.0E-9);
    }

    @Test
    void lowerLayerGetsStrongUpwardRecovery() {
        assertEquals(0.105,
                MarineWaterPhysics.shallowHeightHold(-0.02, true, true, false, false),
                1.0E-9);
    }

    @Test
    void upperLayerClampsVerticalOscillation() {
        assertEquals(0.055,
                MarineWaterPhysics.shallowHeightHold(0.20, false, true, true, false),
                1.0E-9);
        assertEquals(-0.018,
                MarineWaterPhysics.shallowHeightHold(-0.20, false, true, true, false),
                1.0E-9);
    }
}
