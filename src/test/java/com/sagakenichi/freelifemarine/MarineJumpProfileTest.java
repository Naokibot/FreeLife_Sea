package com.sagakenichi.freelifemarine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MarineJumpProfileTest {

    @Test
    void orcaSupportsThreeThroughThirteenBlockBreaches() {
        assertEquals(3, MarineJumpProfile.minHeightBlocks(MarineMobType.ORCA));
        assertEquals(14, MarineJumpProfile.maxHeightExclusive(MarineMobType.ORCA));
        assertEquals(14, MarineJumpProfile.clearanceBlocks(MarineMobType.ORCA));
        assertEquals(10, MarineJumpProfile.speedLevelForHeight(MarineMobType.ORCA, 13));
    }

    @Test
    void higherTargetsReceiveHigherInitialVerticalVelocity() {
        double previous = 0.0;
        for (int height = 3; height <= 13; height++) {
            double velocity = MarineJumpProfile.initialVerticalVelocity(height);
            assertTrue(velocity > previous);
            previous = velocity;
        }
        assertEquals(1.671, MarineJumpProfile.initialVerticalVelocity(13), 1.0E-9);
    }

    @Test
    void sharkBreachesRemainSmallerButStartAtThreeBlocks() {
        assertEquals(3, MarineJumpProfile.minHeightBlocks(MarineMobType.SHARK));
        assertEquals(8, MarineJumpProfile.maxHeightExclusive(MarineMobType.SHARK));
    }
}
