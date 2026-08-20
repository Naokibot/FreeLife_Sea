package com.sagakenichi.freelifemarine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarineNaturalMotionProfileTest {

    @Test
    void autonomousAquaticMobsKeepAContinuousCruiseFloor() {
        assertEquals(5, MarineNaturalMotionProfile.continuousCruiseLevel(MarineMobType.ORCA));
        assertEquals(4, MarineNaturalMotionProfile.continuousCruiseLevel(MarineMobType.SHARK));
        assertTrue(MarineNaturalMotionProfile.minPace(MarineMobType.ORCA) > 0.0);
        assertTrue(MarineNaturalMotionProfile.minPace(MarineMobType.SHARK) > 0.0);
    }

    @Test
    void roamingTargetsAreFarEnoughToPreventLocalCircling() {
        assertTrue(MarineNaturalMotionProfile.minRoamDistance(MarineMobType.ORCA) >= 12.0);
        assertTrue(MarineNaturalMotionProfile.maxRoamDistance(MarineMobType.ORCA) >= 28.0);
        assertTrue(MarineNaturalMotionProfile.minRoamDistance(MarineMobType.SHARK) >= 9.0);
        assertTrue(MarineNaturalMotionProfile.maxRoamDistance(MarineMobType.SHARK) >= 20.0);
        assertTrue(MarineNaturalMotionProfile.minRoamTargetTicks(MarineMobType.ORCA) >= 80);
        assertTrue(MarineNaturalMotionProfile.minRoamTargetTicks(MarineMobType.SHARK) >= 100);
    }

    @Test
    void naturalPaceAndDepthVariationStayGentle() {
        for (long tick = 0; tick < 2_000; tick += 7) {
            double orcaPulse = MarineNaturalMotionProfile.pacePulse(MarineMobType.ORCA, tick, 1.3);
            double sharkPulse = MarineNaturalMotionProfile.pacePulse(MarineMobType.SHARK, tick, 2.1);
            assertTrue(orcaPulse >= 0.955 && orcaPulse <= 1.045);
            assertTrue(sharkPulse >= 0.965 && sharkPulse <= 1.035);
            assertTrue(Math.abs(MarineNaturalMotionProfile.verticalWave(
                    MarineMobType.ORCA, tick, 1.3)) <= 0.014);
            assertTrue(Math.abs(MarineNaturalMotionProfile.verticalWave(
                    MarineMobType.SHARK, tick, 2.1)) <= 0.010);
        }
    }

    @Test
    void largeAquaticMobsBreakBoatsButCrabsDoNot() {
        assertTrue(MarineNaturalMotionProfile.breaksBoats(MarineMobType.ORCA));
        assertTrue(MarineNaturalMotionProfile.breaksBoats(MarineMobType.SHARK));
        assertFalse(MarineNaturalMotionProfile.breaksBoats(MarineMobType.CRAB));
        assertTrue(MarineNaturalMotionProfile.collisionScanRadius(MarineMobType.ORCA) >= 6.0);
        assertTrue(MarineNaturalMotionProfile.collisionScanRadius(MarineMobType.SHARK) >= 4.5);
    }
}
