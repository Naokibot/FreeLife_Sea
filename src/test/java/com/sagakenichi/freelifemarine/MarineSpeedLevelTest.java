package com.sagakenichi.freelifemarine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MarineSpeedLevelTest {

    @Test
    void exposesTenOrderedSpeedLevelsUpToTwentyBlocksPerSecond() {
        assertEquals(10, MarineSpeedLevel.values().length);
        for (int level = 1; level <= 10; level++) {
            MarineSpeedLevel speed = MarineSpeedLevel.of(level);
            assertEquals(level, speed.level());
            assertEquals(level * 2.0, speed.blocksPerSecond(), 0.000001);
            assertEquals(level * 0.1, speed.blocksPerTick(), 0.000001);
        }
        assertEquals(20.0, MarineSpeedLevel.LEVEL_10.blocksPerSecond(), 0.000001);
        assertEquals(1.0, MarineSpeedLevel.LEVEL_10.blocksPerTick(), 0.000001);
    }

    @Test
    void rejectsOutOfRangeLevels() {
        assertThrows(IllegalArgumentException.class, () -> MarineSpeedLevel.of(0));
        assertThrows(IllegalArgumentException.class, () -> MarineSpeedLevel.of(11));
    }

    @Test
    void quantizesExistingShowSpeedsToNearestTier() {
        assertEquals(MarineSpeedLevel.LEVEL_2, MarineSpeedLevel.nearestBlocksPerTick(0.24));
        assertEquals(MarineSpeedLevel.LEVEL_5, MarineSpeedLevel.nearestBlocksPerTick(0.48));
        assertEquals(MarineSpeedLevel.LEVEL_10, MarineSpeedLevel.nearestBlocksPerTick(1.2));
    }
}
