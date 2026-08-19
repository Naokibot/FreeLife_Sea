package com.sagakenichi.freelifemarine;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ShowScheduleTest {

    @Test
    void parsesValidClockTimesAndRejectsInvalidValues() {
        assertEquals(LocalTime.of(9, 0), ShowSchedule.parseTime("9:00"));
        assertEquals(LocalTime.of(15, 30), ShowSchedule.parseTime("15:30"));
        assertNull(ShowSchedule.parseTime("24:00"));
        assertNull(ShowSchedule.parseTime("12:60"));
        assertNull(ShowSchedule.parseTime("bad"));
    }

    @Test
    void deduplicatesAndSortsSchedules() {
        assertEquals(
                List.of(LocalTime.of(9, 0), LocalTime.of(13, 0), LocalTime.of(15, 30)),
                ShowSchedule.parseTimes(List.of("15:30", "9:00", "13:00", "9:00"))
        );
    }

    @Test
    void invalidTimeZoneFallsBack() {
        ZoneId fallback = ZoneId.of("Asia/Tokyo");
        assertEquals(ZoneId.of("UTC"), ShowSchedule.parseZone("UTC", fallback));
        assertEquals(fallback, ShowSchedule.parseZone("Not/AZone", fallback));
        assertEquals(fallback, ShowSchedule.parseZone("", fallback));
    }
}
