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
    void naturalVariationsStayGentleAndBounded() {
        double previousOrcaWeave = MarineNaturalMotionProfile.headingWeaveDegrees(
                MarineMobType.ORCA, 0L, 1.3);
        double previousSharkWeave = MarineNaturalMotionProfile.headingWeaveDegrees(
                MarineMobType.SHARK, 0L, 2.1);

        for (long tick = 1; tick < 2_000; tick++) {
            double orcaPulse = MarineNaturalMotionProfile.pacePulse(MarineMobType.ORCA, tick, 1.3);
            double sharkPulse = MarineNaturalMotionProfile.pacePulse(MarineMobType.SHARK, tick, 2.1);
            double orcaWeave = MarineNaturalMotionProfile.headingWeaveDegrees(
                    MarineMobType.ORCA, tick, 1.3);
            double sharkWeave = MarineNaturalMotionProfile.headingWeaveDegrees(
                    MarineMobType.SHARK, tick, 2.1);

            assertTrue(orcaPulse >= 0.955 && orcaPulse <= 1.045);
            assertTrue(sharkPulse >= 0.965 && sharkPulse <= 1.035);
            assertTrue(Math.abs(orcaWeave) <= 4.5);
            assertTrue(Math.abs(sharkWeave) <= 3.2);
            assertTrue(Math.abs(orcaWeave - previousOrcaWeave) <= 0.113);
            assertTrue(Math.abs(sharkWeave - previousSharkWeave) <= 0.068);
            assertTrue(Math.abs(MarineNaturalMotionProfile.verticalWave(
                    MarineMobType.ORCA, tick, 1.3)) <= 0.018);
            assertTrue(Math.abs(MarineNaturalMotionProfile.verticalWave(
                    MarineMobType.SHARK, tick, 2.1)) <= 0.012);

            previousOrcaWeave = orcaWeave;
            previousSharkWeave = sharkWeave;
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
