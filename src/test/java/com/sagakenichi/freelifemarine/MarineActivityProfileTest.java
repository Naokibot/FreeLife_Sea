package com.sagakenichi.freelifemarine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarineActivityProfileTest {

    @Test
    void orcasUseFasterMoreVariedAutonomousSpeeds() {
        assertEquals(4, MarineActivityProfile.minRoamLevel(MarineMobType.ORCA));
        assertEquals(7, MarineActivityProfile.maxRoamLevel(MarineMobType.ORCA));
        assertEquals(8, MarineActivityProfile.burstLevel(MarineMobType.ORCA));
        assertTrue(MarineActivityProfile.burstChance(MarineMobType.ORCA) >= 0.20);
    }

    @Test
    void sharksRemainActiveButBelowOrcaBurstSpeed() {
        assertEquals(4, MarineActivityProfile.minRoamLevel(MarineMobType.SHARK));
        assertEquals(6, MarineActivityProfile.maxRoamLevel(MarineMobType.SHARK));
        assertEquals(7, MarineActivityProfile.burstLevel(MarineMobType.SHARK));
    }

    @Test
    void autonomousChangesAndBreachesOccurMoreOften() {
        assertTrue(MarineActivityProfile.maxBehaviorTicksExclusive(MarineMobType.ORCA) <= 121);
        assertTrue(MarineActivityProfile.maxBehaviorTicksExclusive(MarineMobType.SHARK) <= 151);
        assertEquals(260, MarineActivityProfile.minJumpDelayTicks(MarineMobType.ORCA));
        assertEquals(480, MarineActivityProfile.minJumpDelayTicks(MarineMobType.SHARK));
        assertTrue(MarineActivityProfile.maxYawChange(MarineMobType.ORCA) >= 50.0);
    }
}
