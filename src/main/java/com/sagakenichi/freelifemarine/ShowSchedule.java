package com.sagakenichi.freelifemarine;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ShowSchedule {

    private static final Pattern CLOCK = Pattern.compile("^([0-9]{1,2}):([0-9]{2})$");

    private ShowSchedule() {
    }

    static ZoneId parseZone(String value, ZoneId fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return ZoneId.of(value.trim());
        } catch (DateTimeException ignored) {
            return fallback;
        }
    }

    static List<LocalTime> parseTimes(List<String> values) {
        Set<LocalTime> unique = new LinkedHashSet<>();
        if (values == null) {
            return List.of();
        }
        for (String value : values) {
            LocalTime time = parseTime(value);
            if (time != null) {
                unique.add(time);
            }
        }
        List<LocalTime> result = new ArrayList<>(unique);
        result.sort(LocalTime::compareTo);
        return List.copyOf(result);
    }

    static LocalTime parseTime(String value) {
        if (value == null) {
            return null;
        }
        Matcher matcher = CLOCK.matcher(value.trim());
        if (!matcher.matches()) {
            return null;
        }
        int hour = Integer.parseInt(matcher.group(1));
        int minute = Integer.parseInt(matcher.group(2));
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            return null;
        }
        return LocalTime.of(hour, minute);
    }
}
