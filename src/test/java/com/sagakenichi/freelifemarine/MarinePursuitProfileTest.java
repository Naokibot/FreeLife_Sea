package com.sagakenichi.freelifemarine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MarinePursuitProfileTest {

    @Test
    void farFoodUsesHigherSpeedLevels() {
        assertEquals(MarineSpeedLevel.LEVEL_9,
                MarinePursuitProfile.speedLevel(MarineMobType.ORCA, 14.0));
        assertEquals(MarineSpeedLevel.LEVEL_8,
                MarinePursuitProfile.speedLevel(MarineMobType.SHARK, 10.0));
    }

    @Test
    void finalApproachSlowsToAvoidOvershoot() {
        assertEquals(MarineSpeedLevel.LEVEL_6,
                MarinePursuitProfile.speedLevel(MarineMobType.ORCA, 2.0));
        assertEquals(MarineSpeedLevel.LEVEL_5,
                MarinePursuitProfile.speedLevel(MarineMobType.SHARK, 2.0));
    }

    @Test
    void foodScanAndAccelerationAreMoreResponsive() {
        assertEquals(2L, MarinePursuitProfile.foodScanIntervalTicks());
        assertTrue(MarinePursuitProfile.accelerationMultiplier(MarineMobType.ORCA) > 2.0);
        assertTrue(MarinePursuitProfile.accelerationMultiplier(MarineMobType.SHARK) > 2.0);
    }
}
