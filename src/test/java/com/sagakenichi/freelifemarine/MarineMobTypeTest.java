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
    void modelsMeetNewDetailAndCrabScaleFloors() {
        assertTrue(MarineMobType.ORCA.parts().size() >= 60);
        assertTrue(MarineMobType.SHARK.parts().size() >= 50);
        assertTrue(MarineMobType.CRAB.parts().size() >= 16);
        assertTrue(MarineMobType.CRAB.interactionWidth() <= 1.35F);
        assertTrue(MarineMobType.CRAB.interactionHeight() <= 0.72F);
        assertTrue(MarineMobType.CRAB.parts().stream().allMatch(part -> part.scaleX() <= 1.0F));
    }

    @Test
    void speciesHaveDifferentAutonomousMovementProfiles() {
        assertTrue(MarineMobType.ORCA.foodAttractionRange() > MarineMobType.SHARK.foodAttractionRange());
        assertTrue(MarineMobType.SHARK.foodAttractionRange() > MarineMobType.CRAB.foodAttractionRange());
        assertTrue(MarineMobType.ORCA.autonomousAcceleration() > MarineMobType.SHARK.autonomousAcceleration());
        assertTrue(MarineMobType.CRAB.cruiseSpeed() < MarineMobType.SHARK.cruiseSpeed());
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
