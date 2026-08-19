package com.sagakenichi.freelifemarine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MarineMotionTuningTest {

    @Test
    void riddenOrcaSpeedIsExactlyFourTimesPreviousMaximum() {
        assertEquals(56.0, MarineMotionTuning.ORCA_RIDDEN_BLOCKS_PER_SECOND, 1.0E-9);
        assertEquals(2.8, MarineMotionTuning.ORCA_RIDDEN_BLOCKS_PER_TICK, 1.0E-9);
    }

    @Test
    void unsupportedAirExcludesWaterAndGround() {
        assertTrue(MarineMotionTuning.isUnsupportedAir(false, false));
        assertFalse(MarineMotionTuning.isUnsupportedAir(true, false));
        assertFalse(MarineMotionTuning.isUnsupportedAir(false, true));
    }

    @Test
    void hoverIsKickedDownButNormalJumpAndFallAreUntouched() {
        assertTrue(MarineMotionTuning.needsFallKick(70.0, 70.0, 0.0));
        assertTrue(MarineMotionTuning.needsFallKick(70.0, 70.0, 0.11));
        assertFalse(MarineMotionTuning.needsFallKick(Double.NaN, 70.0, 0.0));
        assertFalse(MarineMotionTuning.needsFallKick(70.0, 70.2, 0.45));
        assertFalse(MarineMotionTuning.needsFallKick(70.0, 69.8, -0.20));
        assertEquals(-0.18, MarineMotionTuning.fallKickVelocity(0.0), 1.0E-9);
    }
}
