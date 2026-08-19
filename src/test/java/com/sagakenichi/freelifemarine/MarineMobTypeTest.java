package com.sagakenichi.freelifemarine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MarineMobTypeTest {

    @Test
    void parsesSupportedCommandNames() {
        assertEquals(MarineMobType.SHARK, MarineMobType.fromInput("shark"));
        assertEquals(MarineMobType.ORCA, MarineMobType.fromInput("orca"));
        assertEquals(MarineMobType.ORCA, MarineMobType.fromInput("killer-whale"));
        assertNull(MarineMobType.fromInput("dolphin"));
    }
}
