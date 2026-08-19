package com.sagakenichi.freelifemarine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarineMobTypeTest {

    @Test
    void parsesSupportedCommandNames() {
        assertEquals(MarineMobType.SHARK, MarineMobType.fromInput("shark"));
        assertEquals(MarineMobType.ORCA, MarineMobType.fromInput("orca"));
        assertEquals(MarineMobType.ORCA, MarineMobType.fromInput("killer-whale"));
        assertEquals(MarineMobType.CRAB, MarineMobType.fromInput("crab"));
        assertNull(MarineMobType.fromInput("dolphin"));
    }

    @Test
    void orcaKeepsEightSeatsAndSharkOnePilotSeat() {
        assertEquals(8, MarineMobType.ORCA.seats().size());
        assertEquals(1, MarineMobType.SHARK.seats().size());
        assertTrue(MarineMobType.ORCA.rideable());
        assertTrue(MarineMobType.SHARK.rideable());
        assertFalse(MarineMobType.CRAB.rideable());
    }

    @Test
    void refinedModelsMeetHigherDetailFloor() {
        assertTrue(MarineMobType.ORCA.parts().size() >= 48);
        assertTrue(MarineMobType.SHARK.parts().size() >= 39);
        assertTrue(MarineMobType.CRAB.parts().size() >= 16);
    }

    @Test
    void allMarineMobsKeepTenHealthAndValidPartScales() {
        for (MarineMobType type : MarineMobType.values()) {
            assertEquals(10.0, type.maxHealth());
            for (MarineMobType.ModelPart part : type.parts()) {
                assertTrue(part.scaleX() > 0.0F);
                assertTrue(part.scaleY() > 0.0F);
                assertTrue(part.scaleZ() > 0.0F);
            }
        }
    }
}
