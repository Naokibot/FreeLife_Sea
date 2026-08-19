package com.sagakenichi.freelifemarine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarineHitboxProfileTest {

    @Test
    void everyCustomMobHasAtLeastOnePositiveHitbox() {
        for (MarineMobType type : MarineMobType.values()) {
            var hitboxes = MarineHitboxProfile.forType(type);
            assertTrue(!hitboxes.isEmpty(), type + " must have an interaction hitbox");
            for (MarineHitboxProfile.Hitbox hitbox : hitboxes) {
                assertTrue(hitbox.width() > 0.0F);
                assertTrue(hitbox.height() > 0.0F);
            }
        }
    }

    @Test
    void largeAnimalsUseSegmentedBodyHitboxes() {
        assertEquals(6, MarineHitboxProfile.forType(MarineMobType.ORCA).size());
        assertEquals(5, MarineHitboxProfile.forType(MarineMobType.SHARK).size());
        assertEquals(3, MarineHitboxProfile.forType(MarineMobType.CRAB).size());
    }
}
